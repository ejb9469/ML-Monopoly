import java.util.*;

import static java.lang.System.exit;

/**
 * Class to handle gameplay loop.
 */
public class Game implements OutputsWarnings {

    public static final int MAX_TURNS = 100;
    public static final String PROMPT_DEFAULT = "What action would you like to perform?";

    private static int ID_INCREMENT = 0;

    private int id = ID_INCREMENT++;

    private GameState gameState;
    private int currentTurn = 1;

    private Player[] players;
    private UUID[] playerUUIDs;

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

        Player player = players[keyIndex];
        boolean isPlayerTurn = (gameState.turnIndicator == keyIndex);
        boolean doNotRemoveAction = false;  // Used for e.g. double-dice

        // Handle action
        switch (action) {

            case MOVE_THROW_DICE -> {

                // Sanity check the turn status
                if (!isPlayerTurn) break;

                // Roll dice
                Dice dice = new Dice();
                int toss = dice.toss();
                gameState.timesRolled[keyIndex]++;
                currentLegalActions.remove(GameAction.MOVE_THROW_DICE);

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
            case PROPERTY_BUY_OR_AUCTION -> {

                // This action can theoretically be repeated.
                doNotRemoveAction = true;

                // Perform checks on Player turn and location status
                if (
                        !isPlayerTurn
                        || gameState.playerLocations[keyIndex] != board.getSquares().indexOf(wrapper.objProperty)
                        || gameState.ownership[board.getSquares().indexOf(wrapper.objProperty)] != 0
                ) break;

                if (wrapper.objBool && gameState.cash[keyIndex] >= wrapper.objProperty.marketPrice) {
                    // Remember that having enough cash is a prerequisite to even reach the buyProperty() method.
                    buyProperty(keyIndex, wrapper.objProperty);
                } else {
                    auctionProperty(keyIndex, wrapper.objProperty);
                }

            }
            default -> {  // END TURN || Note: this covers 'graceful' turn ends too.
                endTurn();  // TODO
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
    private void signalTurn(int execCodeFlow, int playerIndex, String prompt) {
        Set<GameAction> legalActions = generateLegalActions(execCodeFlow);
        currentLegalActions = legalActions;
        players[playerIndex].signalTurn(legalActions, playerUUIDs[playerIndex], prompt);
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
        currentLegalActions = generateLegalActions(0);  // resets to all legal actions
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

    private void buyProperty(int playerIndex, Property property) {};

    private void auctionProperty(int playerIndex, Property property) {};

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

    ////////////////////////////////////////

    /**
     * @param execFlowCode Indicator of when in the game logic the function is called.
     *                     Case 0 is used to refill the Set to 'all actions'.
     *                     Case -1 (or any invalid case) is used to simply return `currentLegalActions`.
     * @return An array of legal actions for a given point in execution.
     */
    private Set<GameAction> generateLegalActions(int execFlowCode) {
        Set<GameAction> legalActions = Set.copyOf(currentLegalActions);
        switch (execFlowCode) {
            // Case 0 used to refill the Set.
            case 0 -> legalActions = Set.copyOf(List.of(GameAction.values()));
            // Case 1 is used for landing on a Property.
            case 1 -> legalActions = Set.copyOf(List.of(GameAction.PROPERTY_BUY_OR_AUCTION));
            // TODO: In subsequent cases, nix all invalid actions to avoid pseudo-infinite loops.
            //  Although the Decider should handle this in theory, it's good to cull certain actions server-side.
            //  Examples of nix-able actions: trading more than X (20?) times / turn, rejecting trade offers that don't exist, buying a house without the funds, etc.
            //  ENDED HERE @ 05-25-23 12:45pm EST
        }
        //currentLegalActions = legalActions;
        return legalActions;
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