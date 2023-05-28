import java.util.*;

import static java.lang.System.exit;
// ^^ Might not be a great idea to exit this way ^^

/**
 * Class that represents a game and implements Monopoly gameplay loop.
 * Player decision-making is NOT handled here.
 */
public class Game implements OutputsWarnings {

    public static final int MAX_TURNS = 100;
    public static final int MAX_TRADES = 2;  // "per player per turn", currently unused
    public static final String PROMPT_DEFAULT = "What action would you like to perform?";

    private static int ID_INCREMENT = 0;

    private final int id = ID_INCREMENT++;

    private final GameState gameState;
    private int currentTurn = 1;

    private final Player[] players;
    private final UUID[] playerUUIDs;

    private Set<GameAction> currentLegalActions = new HashSet<>();

    private final Board board;
    private final Communicator communicator;

    /**
     * Constructs a Game given a pre-existing Communicator object.
     * @param numPlayers Number of players.
     * @param names Names of each player.
     * @param communicator Pre-existing Communicator object.
     */
    public Game(int numPlayers, String[] names, Communicator communicator) {
        if (communicator == null)
            communicator = new DebugPipe(this);
        this.communicator = communicator;
        this.board = new Board(this);

        playerUUIDs = new UUID[numPlayers];
        Set<UUID> seenUUIDs = new HashSet<>();
        for (int i = 0; i < numPlayers; i++) {
            UUID randUUID = UUID.randomUUID();
            if (seenUUIDs.contains(randUUID)) {
                i--;
                continue;
            }
            playerUUIDs[i] = randUUID;
            seenUUIDs.add(randUUID);
        }

        this.players = new Player[numPlayers];
        for (int i = 0; i < numPlayers; i++)
            this.players[i] = new Player(communicator, names[i], playerUUIDs[i]);

        this.gameState = new GameState(numPlayers);
    }

    /**
     * Constructs a Game with a new Communicator object (DebugPipe).
     * Note: DebugPipe creation logic handled in big constructor.
     */
    public Game(int numPlayers, String[] names) {
        this(numPlayers, names, null);
    }


    /**
     * Request the Game object perform an action.
     * Called via the Communicator.
     * @param action GameAction to perform.
     * @param key Secret UUID key of requesting Player.
     * @param wrapper GameObject wrapper for necessary action data.
     * @param depth Recursion depth.
     */
    public void requestAction(GameAction action, UUID key, GameObject wrapper, int depth) {

        // Recursion base case
        if (depth == 0) return;

        // Check if valid authentication
        int keyIndex = keyExists(key);
        if (keyIndex == -1) {
            warn(1);
            return;
        }

        // Check if legal move. If not, reject it & replace with END_TURN.
        if (!currentLegalActions.contains(action)) {
            warn(2);
            action = GameAction.END_TURN;
        }

        boolean isPlayerTurn = (gameState.turnIndicator == keyIndex);

        // Used for actions that are allowed multiple times per turn.
        //      This can be used respective or irrespective of context,
        //      e.g. Mortgaging and unmortgaging have no theoretical limit,
        //           but throwing move dice can only be repeated when rolling doubles.
        boolean doNotRemoveAction = false;

        // Handle action
        switch (action) {

            case MOVE_THROW_DICE -> {

                // Sanity check the turn status
                if (!isPlayerTurn) break;

                // Player cannot roll move dice in jail.
                if (gameState.jailedPlayers[keyIndex]) break;

                // Roll dice
                Dice dice = new Dice();
                int toss = dice.toss();
                gameState.timesRolled[keyIndex]++;

                // Handle doubles
                if (dice.doubles) {
                    if (gameState.timesRolled[keyIndex] >= 3) {
                        // Three doubles in a row
                        jailPlayer(keyIndex);
                        break;  // Do not proceed with the move
                    } else {
                        // Less than three doubles in a row
                        doNotRemoveAction = true;  // Quick, add it back!
                    }
                } else if (gameState.timesRolled[keyIndex] > 1) {
                    // Double moving without double dice. Sad!
                    break;  // Do not proceed with the move
                }

                // Move token if passed checks
                moveTokenForwards(keyIndex, toss);

            }
            case TRADE_OFFER -> {}  // Trading implemented later!
            case TRADE_ACCEPT -> {}  // Trading implemented later!
            case TRADE_REJECT -> {}  // Trading implemented later!
            case PROPERTY_BUY_OR_AUCTION -> {

                // This action can theoretically be repeated.
                doNotRemoveAction = true;

                int propertyIndex = board.getSquares().indexOf(wrapper.objProperty);

                // Perform checks on Player turn and location status
                if (
                           !isPlayerTurn
                        || gameState.playerLocations[keyIndex] != propertyIndex  // Player did not land on the Property
                        || gameState.ownership[propertyIndex] != 0  // Property is already owned
                ) break;

                if (wrapper.objBool && gameState.cash[keyIndex] >= wrapper.objProperty.marketPrice) {
                    // Remember that having enough cash is a prerequisite to even reach the buyProperty() method.
                    buyProperty(keyIndex, wrapper.objProperty);
                } else {
                    auctionProperty(keyIndex, wrapper.objProperty);
                }

            }
            case PROPERTY_MORTGAGE -> {

                // This action can theoretically be repeated.
                doNotRemoveAction = true;

                int propertyIndex = board.getSquares().indexOf(wrapper.objProperty);

                // Perform checks on Player turn, ownership, and mortgage status
                if (
                           !isPlayerTurn
                        || gameState.ownership[propertyIndex] != keyIndex  // Player does not own the Property
                        || gameState.mortgages[propertyIndex]  // Property is already mortgaged
                ) break;

                mortgageProperty(wrapper.objProperty);

            }
            case PROPERTY_UNMORTGAGE -> {

                // This action can theoretically be repeated.
                doNotRemoveAction = true;

                int propertyIndex = board.getSquares().indexOf(wrapper.objProperty);

                // Perform checks on Player turn, ownership, mortgage, and cash status
                if (
                           !isPlayerTurn
                        || gameState.ownership[propertyIndex] != keyIndex  // Player does not the Property
                        || !gameState.mortgages[propertyIndex]  // The Property isn't mortgaged
                        || gameState.cash[keyIndex] < (wrapper.objProperty.marketPrice * wrapper.objProperty.mortgageDivisor * 1.1)  // Player cannot afford to unmortgage
                                                        // TODO: Make into a constant in Property.java
                ) break;

                unmortgageProperty(wrapper.objProperty);

            }
            case AUCTION_BID -> {
                // TODO: Implement auctions!
            }
            case HOUSE_BUILD -> {

                // This action can theoretically be repeated.
                doNotRemoveAction = true;

                int propertyIndex = board.getSquares().indexOf(wrapper.objProperty);

                boolean isBuyingHotel = (gameState.houses[propertyIndex] == 4);

                // Perform checks on Player turn, ownership, mortgage, houses, and cash status
                if (
                           !isPlayerTurn
                        || gameState.ownership[propertyIndex] != keyIndex  // Player does not own the Property
                        || gameState.houses[propertyIndex] >= 5  // There is already a hotel on the Property
                        || (gameState.remainingHouses <= 0 && !isBuyingHotel)  // There are no remaining houses
                        || (gameState.remainingHotels <= 0 && isBuyingHotel)  // There are no remaining hotels
                        || gameState.cash[keyIndex] < wrapper.objProperty.baseHouseCost  // Player cannot afford to build a house
                        || gameState.mortgages[propertyIndex]  // The Property is mortgaged
                ) break;

                buyHouse(wrapper.objProperty, isBuyingHotel);

            }
            case HOUSE_SELL -> {

                // This action can theoretically be repeated
                doNotRemoveAction = true;

                int propertyIndex = board.getSquares().indexOf(wrapper.objProperty);

                boolean isSellingHotel = (gameState.houses[propertyIndex] == 5);

                // Perform checks on Player turn, ownership, mortgage, and houses status
                if (
                           !isPlayerTurn
                        || gameState.ownership[propertyIndex] != keyIndex  // Player does not own the Property
                        || gameState.houses[propertyIndex] <= 0  // There are no houses to sell
                        || gameState.mortgages[propertyIndex]  // The Property is mortgaged
                ) break;

                sellHouse(wrapper.objProperty, isSellingHotel);

            }
            case JAIL_THROW_DICE -> {

                // Sanity check the turn status
                if (!isPlayerTurn) break;

                // Player cannot roll jail dice outside of jail
                if (!gameState.jailedPlayers[keyIndex]) break;

                // Roll dice
                Dice dice = new Dice();
                int toss = dice.toss();
                gameState.timesRolled[keyIndex]++;

                // Handle doubles
                if (dice.doubles) {
                    gameState.timesRolled[keyIndex] = 0;
                    freePlayer(keyIndex, false, false, false);
                    moveTokenForwards(keyIndex, toss);
                } else if (gameState.timesRolled[keyIndex] >= 3) {
                    gameState.timesRolled[keyIndex] = 0;
                    // Use card or pay bail in that order of preference
                    boolean hasCard = gameState.gtfoJailCards[keyIndex] < 1;
                    freePlayer(keyIndex, !hasCard, hasCard, true);
                }

            }
            case JAIL_PAY_BAIL -> {

                // Perform checks on Player turn, jail, and cash status.
                    // Note: we only arrive at this branch if the Player
                    //       *voluntarily* pays bail, NOT if they are forced to.
                if (
                           !isPlayerTurn
                        || !gameState.jailedPlayers[keyIndex]  // Player is not in jail
                        || gameState.cash[keyIndex] < Property.BAIL_AMOUNT  // Player does not have enough for bail
                ) break;

                freePlayer(keyIndex, true, false, false);

            }
            case JAIL_USE_CARD -> {

                // Perform checks on Player turn, jail, and card status.
                if (
                           !isPlayerTurn
                        || !gameState.jailedPlayers[keyIndex]  // Player is not in jail
                        || gameState.gtfoJailCards[keyIndex] <= 0  // Player does not have any cards
                ) break;

                freePlayer(keyIndex, false, true, false);

            }
            default -> {  // END TURN || Note: this covers 'graceful' turn ends too.
                endTurn();
                return;  // No more successive actions
            }
        }

        // Remove performed action from set of legal actions.
        // This branch will execute depending on the type of action and its context.
        if (!doNotRemoveAction)
            currentLegalActions.remove(action);

        // Action completed, either end turn or ask for another.
        if (currentLegalActions.size() == 0 || (currentLegalActions.size() == 1 && currentLegalActions.contains(GameAction.END_TURN))) {
            requestAction(GameAction.END_TURN, key, null, 1);
        } else {
            signalTurn(-1, keyIndex);
        }

    }

    /**
     * Request the Game object perform an action, but DISALLOW RECURSION.
     */
    public void requestAction(GameAction action, UUID key, GameObject wrapper) {
        requestAction(action, key, wrapper, 1);
    }

    /**
     * Handles the process of signaling to a player that it's their turn.
     * Generates a Set of legal actions to pass to the player, ...
     *      ... and also updates this.currentLegalActions in the process.
     * @param execCodeFlow Indicator of when in the game logic the function is called.
     * @param playerIndex Index / ID of the Player.
     * @param prompt Output prompt.
     */
    private void signalTurn(int execCodeFlow, Set<GameAction> nixedActions, int playerIndex, String prompt) {
        Set<GameAction> legalActions = generateLegalActions(execCodeFlow, nixedActions);
        currentLegalActions = legalActions;
        players[playerIndex].signalTurn(legalActions, playerUUIDs[playerIndex], prompt);
    }
    private void signalTurn(int execCodeFlow, int playerIndex, String prompt) {
        signalTurn(execCodeFlow, new HashSet<>(), playerIndex, prompt);
    }
    private void signalTurn(int execCodeFlow, int playerIndex) {
        signalTurn(execCodeFlow, playerIndex, PROMPT_DEFAULT);
    }

    /**
     * Move a Player's position forwards.
     * The direction *must* be forwards to handle cards, GO, etc.
     * Should NOT be called when the Player is in Jail.
     * @param playerIndex Player index / ID.
     * @param spaces Number of spaces to move.
     */
    private void moveTokenForwards(int playerIndex, int spaces) {

        int currentLocation = gameState.playerLocations[playerIndex];
        int landingLocation = (currentLocation + spaces) % 40;
        gameState.playerLocations[playerIndex] = landingLocation;
        if (landingLocation <= currentLocation)  // GO procedure
            incrementCash(playerIndex, 200);
        Property landingProperty = board.getSquares().get(landingLocation);

        // We're dealing with e.g. Income Tax, GO, Free Parking, etc.
        if (landingProperty.isFunctionalOnly()) {
            switch (landingProperty.getName()) {
                case "GO" -> {}  // GO functionality already handled
                case "Chance" -> {  // TODO
                    Card card = gameState.chance.drawCard();
                }
                case "Community Chest" -> {  // TODO
                    Card card = gameState.communityChest.drawCard();
                }
                case "Income Tax" -> {
                    incrementCash(playerIndex, -200);
                }
                case "Luxury Tax" -> {
                    incrementCash(playerIndex, -75);
                }
                case "Jail" -> {}  // Just visiting!
                case "Go To Jail" -> {
                    jailPlayer(playerIndex);
                }
                case "Free Parking" -> {}  // Do nothing. Too bad!
            }
        }

        // We're dealing with a rented Property
        else {

            // Property is NOT OWNED - buy/auction
            if (gameState.ownership[landingLocation] == 0) {
                String prompt = "Buy / auction " + board.getSquares().get(landingLocation).getName();
                signalTurn(1, playerIndex, prompt);
            }

            // Property is OWNED - pay rent
            else {
                payRent(landingProperty, playerIndex, gameState.ownership[landingLocation]);
            }

        }


    }

    /**
     * End the Player's turn and either...
     * A) End the game if we've reached any end condition.
     * B) Proceed with the next Player's turn.
     */
    private void endTurn() {
        if (gameState.turnIndicator == players.length)
            currentTurn++;
        gameState.turnIndicator = (gameState.turnIndicator + 1) % players.length;
        gameState.timesRolled[gameState.turnIndicator] = 0;
        currentLegalActions = generateLegalActions(0);  // resets to start-of-turn actions
        if (isGameOver()) {
            endGame(0);
        } else {
            signalTurn(0, gameState.turnIndicator, "It's your turn! " + PROMPT_DEFAULT);
        }
    }

    /**
     * @return True under the following conditions, false otherwise:
     *              1) If the Game has exceeded the maximum number of turns.
     *              2) If all other players have left or been eliminated.
     */
    private boolean isGameOver() {
        return (currentTurn > MAX_TURNS || gameState.numPlayers <= 1);
    }

    /**
     * Exits the game with a given status code.
     */
    private void endGame(int statusCode) {
        exit(statusCode);
    }

    ////////////////////////////////////////
    // TODO: Handle race conditions
    // TODO: IMPLEMENTATION!!!

    private void buyProperty(int playerIndex, Property property) {};

    private void auctionProperty(int playerIndex, Property property) {};

    private void mortgageProperty(Property property) {};

    private void unmortgageProperty(Property property) {};

    private void buyHouse(Property property, boolean hotel) {};

    private void sellHouse(Property property, boolean hotel) {};

    /**
     * Transfer rent funds from Player A to Player B.
     * @param property Property landed on.
     * @param victimIndex Index / ID of Player A (lander).
     * @param renterIndex Index / ID of Player B (renter).
     */
    private void payRent(Property property, int victimIndex, int renterIndex) {
        int rentCost = property.calculateRent();
        incrementCash(victimIndex, -rentCost);
        incrementCash(renterIndex, rentCost);
    }

    /**
     * Increment Player's cash by a given amount.
     * This amount can be negative, but this will be handled by a sub-function decrementCash().
     * TODO: Handle negative balances
     * @param playerIndex Player index / ID.
     * @param amount Amount of cash to give / remove from the Player.
     */
    private void incrementCash(int playerIndex, int amount) {
        gameState.cash[playerIndex] += amount;
    }

    /**
     * Send Player to Jail.
     * @param playerIndex Player index / ID.
     * @param jailIndex Board position of the Jail space.
     */
    private void jailPlayer(int playerIndex, int jailIndex) {
        gameState.jailedPlayers[playerIndex] = true;
        gameState.playerLocations[playerIndex] = jailIndex;
    }
    private void jailPlayer(int playerIndex) {
        jailPlayer(playerIndex, board.indexOf("Jail"));
    }

    /**
     * Free a Player from Jail.
     * @param playerIndex Player index / ID.
     * @param bail Using bail vs. bail-less exit.
     * @param card Using card vs. card-less exit.
     * @param forced Forced into this decision.
     */
    // Note: Both `bail` and `card` are present because of the existence of roll-exits.
    private void freePlayer(int playerIndex, boolean bail, boolean card, boolean forced) {
        // TODO
    }

    ////////////////////////////////////////

    /**
     * @param execFlowCode Indicator of when in the game logic the function is called.
     *                     Case -1 (or any invalid case) is used to simply return `currentLegalActions`.
     * @param nixedActions Set of actions specified by the caller to be illegal regardless.
     * @return An array of legal actions for a given point in execution.
     */
    private Set<GameAction> generateLegalActions(int execFlowCode, Set<GameAction> nixedActions) {

        Set<GameAction> legalActions;

        // Grab defaults from case code
        switch (execFlowCode) {
            // Case 0 is used for beginning-of-turn actions. The player must be not in jail.
            case 0 -> legalActions = new HashSet<>(List.of(GameAction.START_ACTIONS));
            // Case 1 is used for landing on a Property.
            case 1 -> legalActions = new HashSet<>(List.of(GameAction.PROPERTY_BUY_OR_AUCTION));
            // Case 2 is used for end-of-turn actions e.g. building houses, mortgaging, trading.
                // Note for later: do we want TRADE_ACCEPT and TRADE_REJECT included in case 2? Atm I say no.
            case 2 -> legalActions = new HashSet<>(List.of(GameAction.END_ACTIONS));
            // Case 3 is used for beginning a turn in jail.
            case 3 -> legalActions = new HashSet<>(List.of(GameAction.JAIL_ACTIONS));
            // Default case (e.g. -1) will simply return `currentLegalActions`.
            default -> legalActions = new HashSet<>(currentLegalActions);
        }

        // Cull nixed actions
        legalActions.removeAll(nixedActions);

        return legalActions;

    }

    // Below called when no nix-able actions provided.
    private Set<GameAction> generateLegalActions(int execFlowCode) {
        return generateLegalActions(execFlowCode, new HashSet<>());
    }

    private int keyExists(UUID key) {
        for (int i = 0; i < playerUUIDs.length; i++) {
            if (playerUUIDs[i].equals(key))
                return i;
        }
        return -1;
    }

    ////////////////////////////////////////

    /**
     * @return A *copy* of the Game State object.
     * We turn a copy because GameState is highly mutable.
     */
    public GameState getGameState() {
        return new GameState(gameState);
    }

    // Getters
    public Player[] getPlayers() {
        return players;
    }


    public void warn(int code) {
        System.err.println("Warning called for Game id=" + id + ", CODE " + code + " || " + Calendar.getInstance());
    }

}