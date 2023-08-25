package server;

import client.Communicator;
import client.DebugPipe;
import client.Player;

import java.util.*;

import static java.lang.System.exit;
// ^^ Might not be a great idea to exit this way ^^

/**
 * Class that represents a game and implements Monopoly gameplay loop.
 * Player decision-making is NOT handled here.
 */
public class Game implements OutputsWarnings {

    public static final int MAX_TURNS = 100;
    public static final int MAX_ACTIONS = 10;
    public static final int MAX_DEPTH = 20;  // Used in requestAction()
    public static final int MAX_TRADES = 2;  // "per player per turn", currently unused
    public static final int STARTING_BID_AMOUNT = 10;
    public static final String PROMPT_DEFAULT = "What action would you like to perform?";

    private static int ID_INCREMENT = 0;
    private final int id = ID_INCREMENT++;

    private boolean gameOverFlag = false;
    private boolean endTurnFlag = false;
    private int currentTurn = 0;
    private int depth = 0;  // Used in requestAction()

    private Set<GameAction> currentLegalActions = new HashSet<>();

    private Property biddingProperty = null;
    private List<Integer> auctionBids = null;

    private Trade currentTrade = null;

    private Dice lastDiceRoll = null;

    private final GameState gameState;
    private final Player[] players;
    private final UUID[] playerUUIDs;

    /**
     * Constructs a Game given a pre-existing Communicator object.
     * @param numPlayers Number of players.
     * @param names Names of each player.
     * @param communicator Pre-existing Communicator object.
     */
    public Game(int numPlayers, String[] names, Communicator communicator) {
        if (communicator == null)
            communicator = new DebugPipe(this);

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

    // Debug functions ////////////////////////////////
    public void gameLoop() {  // TODO: Temporarily public!
        while (!gameOverFlag) {
            depth = 0;
            processTurn();
        }
    }
    public static void main(String[] args) {
        new Game(4, new String[]{"Car", "Thimble", "Ship", "Dog"}, null).gameLoop();
    }
    ///////////////////////////////////////////////////

    /**
     * Request the Game object perform an action.
     * Called via the Communicator.
     * @param action GameAction to perform.
     * @param key Secret UUID key of requesting Player.
     * @param wrapper GameObject wrapper for necessary action data.
     */
    public void requestAction(GameAction action, UUID key, GameObject wrapper) {

        // Recursion base case (note: not direct / "true" recursion)
        if (depth == MAX_DEPTH) return;
        depth++;

        // Check if valid authentication
        int keyIndex = keyExists(key);
        if (keyIndex == -1) {
            warn(1);
            return;
        }

        // Skip the turn of a bankrupted player.
        if (gameState.playerBankruptcy[keyIndex]) return;

        // Check if legal move. If not, reject it & replace with END_TURN.
        if (!currentLegalActions.contains(action)) {
            warn(2);
            signalTurn(-1, keyIndex, "GameAction not allowed, try again.");
            return;
            //action = GameAction.END_TURN;
        }

        boolean isPlayerTurn = (gameState.turnIndicator == keyIndex);

        // Used for actions that are allowed multiple times per turn.
        //      This can be used respective or irrespective of context,
        //      e.g. Mortgaging and unmortgaging have no theoretical limit,
        //           but throwing move dice can only be repeated when rolling doubles.
        // Currently deprecated! 07-11-23
        //boolean doNotRemoveAction = false;

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

                lastDiceRoll = dice;

                boolean allowAdditionalMove = false;
                // Handle doubles
                if (dice.doubles) {
                    if (gameState.timesRolled[keyIndex] >= 3) {
                        // Three doubles in a row
                        jailPlayer(keyIndex);
                        break;  // Do not proceed with the move
                    } else {
                        // Less than three doubles in a row
                        //doNotRemoveAction = true;  // Quick, add it back!
                        allowAdditionalMove = true;
                    }
                }

                // Move token if passed checks
                moveToken(keyIndex, toss);

                if (allowAdditionalMove && !gameState.jailedPlayers[keyIndex] && !gameState.playerBankruptcy[keyIndex])
                    signalTurn(7, keyIndex,
                            "You rolled doubles last turn!\n" + PROMPT_DEFAULT +
                                    "\nYou have rolled doubles " + gameState.timesRolled[keyIndex] + " times in a row.");

            }
            case TRADE_OFFER -> {

                // Check Player turn status
                if (!isPlayerTurn) break;

                // Check if indexes are valid
                if (wrapper.objTrade.getPitcherIndex() != keyIndex
                    || wrapper.objTrade.getCatcherIndex() < 0
                    || wrapper.objTrade.getCatcherIndex() > gameState.numPlayers
                ) {
                    warn(4442);
                    break;
                }

                // Pitcher Properties
                List<Integer> cleansedPropIndexList = new ArrayList<>();
                int[][] contents = wrapper.objTrade.getContents(true);
                for (int pIndex : contents[1]) {
                    // Check bounds of index
                    if (pIndex < 0 || pIndex > Board.SQUARES.size()) {
                        warn(4443);
                        break;
                    }
                    // Check ownership of Property
                    if (gameState.ownership[pIndex] == keyIndex)
                        cleansedPropIndexList.add(pIndex);
                    else {
                        warn(44435);
                        break;
                    }
                    // Check # houses of Property (traded Properties cannot have houses)
                    if (gameState.houses[pIndex] > 0) {
                        warn(4444);
                        break;
                    }
                }
                int[] cPropIndexArr = new int[cleansedPropIndexList.size()];
                for (int i = 0; i < cleansedPropIndexList.size(); i++)
                    cPropIndexArr[i] = cleansedPropIndexList.get(i);
                contents[1] = cPropIndexArr;

                // Pitcher Cash and Cards
                if (contents[0][0] < 0)
                    contents[0][0] = 0;
                else if (contents[0][0] > gameState.cash[keyIndex])
                    contents[0][0] = gameState.cash[keyIndex];
                if (contents[2][0] < 0)
                    contents[2][0] = 0;
                else if (contents[2][0] > gameState.gtfoJailCards[keyIndex])
                    contents[2][0] = gameState.gtfoJailCards[keyIndex];

                // Update Pitcher contents
                wrapper.objTrade.counter(true, contents, false);

                // Catcher Properties
                int catcherIndex = wrapper.objTrade.getCatcherIndex();
                cleansedPropIndexList = new ArrayList<>();
                contents = wrapper.objTrade.getContents(false);
                for (int pIndex : contents[1]) {
                    if (pIndex < 0 || pIndex > Board.SQUARES.size()) {
                        warn(4443);
                        break;
                    }
                    if (gameState.ownership[pIndex] == catcherIndex)
                        cleansedPropIndexList.add(pIndex);
                    else {
                        warn(44435);
                        break;
                    }
                    if (gameState.houses[pIndex] > 0) {
                        warn(4444);
                        break;
                    }
                }
                cPropIndexArr = new int[cleansedPropIndexList.size()];
                for (int i = 0 ; i < cleansedPropIndexList.size(); i++)
                    cPropIndexArr[i] = cleansedPropIndexList.get(i);
                contents[1] = cPropIndexArr;

                // Catcher Cash and Cards
                if (contents[0][0] < 0)
                    contents[0][0] = 0;
                else if (contents[0][0] > gameState.cash[catcherIndex])
                    contents[0][0] = gameState.cash[catcherIndex];
                if (contents[2][0] < 0)
                    contents[2][0] = 0;
                else if (contents[2][0] > gameState.gtfoJailCards[catcherIndex])
                    contents[2][0] = gameState.gtfoJailCards[catcherIndex];

                // Update Catcher contents
                wrapper.objTrade.counter(false, contents, false);

                // Finalize trade
                currentTrade = wrapper.objTrade;
                signalTurn(8, catcherIndex,
                        "You've received a trade from " + players[keyIndex].getName() + "! Details below:\n" +
                                currentTrade.toString());
                currentTrade = null;

            }
            case TRADE_RESPOND -> {

                // Check null status of `currentTrade`
                if (currentTrade == null) break;

                // Check if index is valid
                if (currentTrade.getCatcherIndex() != keyIndex) break;

                if (wrapper.objBool)
                    acceptTrade(currentTrade.getPitcherIndex(), keyIndex, currentTrade);

            }
            case PROPERTY_BUY_OR_AUCTION -> {

                // This action can theoretically be repeated.
                //doNotRemoveAction = true;

                int propertyIndex = gameState.playerLocations[keyIndex];

                // Perform checks on Player turn and location status
                if (
                           !isPlayerTurn
                        || gameState.playerLocations[keyIndex] != propertyIndex  // Player did not land on the Property
                        || gameState.ownership[propertyIndex] != -1  // Property is already owned
                ) break;

                if (wrapper.objBool && gameState.cash[keyIndex] >= Board.SQUARES.get(propertyIndex).marketPrice) {
                    // Remember that having enough cash is a prerequisite to even reach the buyProperty() method.
                    buyProperty(keyIndex, Board.SQUARES.get(propertyIndex));
                } else {
                    auctionProperty(keyIndex, Board.SQUARES.get(propertyIndex));
                }

            }
            case PROPERTY_MORTGAGE -> {

                // This action can theoretically be repeated.
                //doNotRemoveAction = true;

                int propertyIndex = Board.SQUARES.indexOf(wrapper.objProperty);

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
                //doNotRemoveAction = true;

                int propertyIndex = Board.SQUARES.indexOf(wrapper.objProperty);

                // Perform checks on Player turn, ownership, mortgage, and cash status
                if (
                           !isPlayerTurn
                        || gameState.ownership[propertyIndex] != keyIndex  // Player does not the Property
                        || !gameState.mortgages[propertyIndex]  // The Property isn't mortgaged
                        || gameState.cash[keyIndex] < (wrapper.objProperty.marketPrice * wrapper.objProperty.mortgageDivisor * Property.UNMORTGAGE_INTEREST)  // Player cannot afford to unmortgage
                ) break;

                unmortgageProperty(wrapper.objProperty);

            }
            case AUCTION_BID -> {

                // Perform check on ownership status
                if (gameState.ownership[Board.SQUARES.indexOf(biddingProperty)] != -1)
                    break;

                int bid = wrapper.objInt;

                // Perform check on cash status & player bankruptcy
                if (bid > gameState.cash[keyIndex] || gameState.playerBankruptcy[keyIndex])
                    bid = -1;

                // Replace old bid with new bid (or lack thereof)
                auctionBids.set(keyIndex, bid);

            }
            case HOUSE_BUILD -> {

                // This action can theoretically be repeated.
                //doNotRemoveAction = true;

                int propertyIndex = Board.SQUARES.indexOf(wrapper.objProperty);

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

                // Populate Property set index list
                List<Property> propertySet = Board.getSquaresOfColorSet(wrapper.objProperty.color);
                int[] propertySetIndices = new int[propertySet.size()];
                for (int i = 0; i < propertySetIndices.length; i++)
                    propertySetIndices[i] = Board.SQUARES.indexOf(propertySet.get(i));

                // Perform checks on set ownership & building evenness
                boolean hasSetAndIsBuildingEvenly = true;
                for (int propertySetIndex : propertySetIndices) {
                    if (
                               gameState.ownership[propertySetIndex] != keyIndex  // Doesn't own a Property in the set
                            || gameState.houses[propertyIndex] > gameState.houses[propertySetIndex]  // Is building on the set unevenly
                    ) {
                        hasSetAndIsBuildingEvenly = false;
                        break;
                    }
                }
                if (!hasSetAndIsBuildingEvenly) break;

                buyHouse(wrapper.objProperty, isBuyingHotel);

            }
            case HOUSE_SELL -> {

                // This action can theoretically be repeated
                //doNotRemoveAction = true;

                int propertyIndex = Board.SQUARES.indexOf(wrapper.objProperty);

                boolean isSellingHotel = (gameState.houses[propertyIndex] == 5);

                // Perform checks on Player turn, ownership, mortgage, and houses status
                if (
                           !isPlayerTurn
                        || gameState.ownership[propertyIndex] != keyIndex  // Player does not own the Property
                        || gameState.houses[propertyIndex] <= 0  // There are no houses to sell
                        || gameState.mortgages[propertyIndex]  // The Property is mortgaged
                        || (gameState.remainingHouses < 4 && isSellingHotel)  // No remaining houses to replace the hotel with
                ) break;

                // Populate Property set index list
                List<Property> propertySet = Board.getSquaresOfColorSet(wrapper.objProperty.color);
                int[] propertySetIndices = new int[propertySet.size()];
                for (int i = 0; i < propertySetIndices.length; i++)
                    propertySetIndices[i] = Board.SQUARES.indexOf(propertySet.get(i));

                // Perform checks on building evenness
                boolean isBuildingEvenly = true;
                for (int propertySetIndex : propertySetIndices) {
                    // Is selling on the set unevenly
                    if (gameState.houses[propertyIndex] < gameState.houses[propertySetIndex]) {
                        isBuildingEvenly = false;
                        break;
                    }
                }
                if (!isBuildingEvenly) break;

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
                    freePlayer(keyIndex, 0);
                    moveToken(keyIndex, toss);
                } else if (gameState.timesRolled[keyIndex] >= 3) {
                    gameState.timesRolled[keyIndex] = 0;
                    // Use card or pay bail in that order of preference
                    boolean hasCard = gameState.gtfoJailCards[keyIndex] < 1;
                    if (hasCard)
                        freePlayer(keyIndex, 2);
                    else
                        freePlayer(keyIndex, 1);
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

                freePlayer(keyIndex, 1);
                signalTurn(7, keyIndex);

            }
            case JAIL_USE_CARD -> {

                // Perform checks on Player turn, jail, and card status.
                if (
                           !isPlayerTurn
                        || !gameState.jailedPlayers[keyIndex]  // Player is not in jail
                        || gameState.gtfoJailCards[keyIndex] <= 0  // Player does not have any cards
                ) break;

                freePlayer(keyIndex, 2);
                signalTurn(7, keyIndex);

            }
            case DECLARE_BANKRUPTCY -> {

                // Perform check on Player turn
                if (!isPlayerTurn) break;

                bankruptPlayer(keyIndex);

                endTurnFlag = true;
                //return;

            }
            case END_TURN -> {

                endTurnFlag = true;
                //return;

            }

        }

        // Remove performed action from set of legal actions.
        // This branch will execute depending on the type of action and its context.
        //if (!doNotRemoveAction)
            //currentLegalActions.remove(action);

    }

    /**
     * Handles the process of signaling to a player that it's their turn.
     * Generates a Set of legal actions to pass to the player, ...
     *      ... and also updates `currentLegalActions` in the process.
     * When execution finally returns back, we re-set `currentLegalActions` to its previous value.
     * @param execCodeFlow Indicator of when in the game logic the function is called.
     * @param playerIndex Index / ID of the Player.
     * @param prompt Output prompt.
     */
    private void signalTurn(int execCodeFlow, Set<GameAction> nixedActions, int playerIndex, String prompt) {
        Set<GameAction> legalActions = generateLegalActions(execCodeFlow, nixedActions);
        Set<GameAction> currentLegalActions_old = new HashSet<>(currentLegalActions);
        currentLegalActions = legalActions;
        players[playerIndex].signalTurn(legalActions, playerUUIDs[playerIndex], new PromptString(prompt, players[playerIndex], this.getGameState()));
        currentLegalActions = currentLegalActions_old;
    }
    private void signalTurn(int execCodeFlow, int playerIndex, String prompt) {
        signalTurn(execCodeFlow, new HashSet<>(), playerIndex, prompt);
    }
    private void signalTurn(int execCodeFlow, int playerIndex) {
        signalTurn(execCodeFlow, playerIndex, PROMPT_DEFAULT);
    }

    /**
     * Move a Player's position either forwards or backwards.
     * Wrapper function for moveTokenForwards() and moveTokenBackwards().
     * Should NOT be called when the Player is in Jail.
     * @param playerIndex Player index / ID.
     * @param spaces Number of spaces to move. Can be positive or negative.
     * @param rentMultiplier Multiplier for rent costs of landing on a Property.
     */
    private void moveToken(int playerIndex, int spaces, double rentMultiplier) {
        if (spaces >= 0)
            moveTokenForwards(playerIndex, spaces, rentMultiplier);
        else
            moveTokenBackwards(playerIndex, -spaces, rentMultiplier);
    }
    private void moveToken(int playerIndex, int spaces) {
        moveToken(playerIndex, spaces, 1.0);
    }

    /**
     * Move a Player's position forwards.
     * The direction *must* be forwards to handle cards, GO, etc.
     * Should NOT be called when the Player is in Jail.
     * Should NOT be called in the wild.
     * @param playerIndex Player index / ID.
     * @param spaces Number of spaces to move. Must be positive.
     * @param rentMultiplier Multiplier for rent costs of landing on a Property.
     */
    private void moveTokenForwards(int playerIndex, int spaces, double rentMultiplier) {

        int currentLocation = gameState.playerLocations[playerIndex];
        int landingLocation = (currentLocation + spaces) % Board.SQUARES.size();
        gameState.playerLocations[playerIndex] = landingLocation;
        if (landingLocation < currentLocation || spaces >= Board.SQUARES.size())  // GO procedure
            incrementCash(playerIndex, 200);

        handleMoveLanding(playerIndex, spaces, landingLocation, rentMultiplier);

    }

    /**
     * Move a Player's position backwards.
     * The direction *must* be backwards to exclude "forwards functionalities" e.g. collecting money from GO.
     * Should NOT be called when the Player is in Jail.
     * Should NOT be called in the wild.
     * @param playerIndex Player index / ID.
     * @param spaces Number of spaces to move. Must be positive.
     * @param rentMultiplier Multiplier for rent costs of landing on a Property.
     */
    private void moveTokenBackwards(int playerIndex, int spaces, double rentMultiplier) {

        int currentLocation = gameState.playerLocations[playerIndex];
        int landingLocation = (currentLocation - spaces + Board.SQUARES.size()) % Board.SQUARES.size();
        gameState.playerLocations[playerIndex] = landingLocation;

        handleMoveLanding(playerIndex, spaces, landingLocation, rentMultiplier);

    }

    /**
     * Handle functionality of a Player landing after a move (forwards or backwards).
     * Probably should NOT be called in the wild. Implemented as a sub-function of moveToken().
     * @param playerIndex Player index / ID.
     * @param landingLocation Index of landing Property.
     * @param rentMultiplier Multiplier for rent costs of landing on a Property.
     */
    private void handleMoveLanding(int playerIndex, int spaces, int landingLocation, double rentMultiplier) {

        Property landingProperty = Board.SQUARES.get(landingLocation);

        // We're dealing with e.g. Income Tax, GO, Free Parking, etc.
        if (landingProperty.isFunctionalOnly()) {

            switch (landingProperty.getName()) {
                case "GO" -> {}  // GO functionality already handled
                case "Chance", "Community Chest" -> {
                    performCardAction(playerIndex, gameState.chance.drawCard());
                }
                case "Income Tax" -> {
                    incrementCash(playerIndex, -200);
                }
                case "Luxury Tax" -> {
                    incrementCash(playerIndex, -75);
                }
                case "Jail" -> {
                    players[playerIndex].output(playerUUIDs[playerIndex], "Just visiting!");
                }
                case "Go To Jail" -> {
                    jailPlayer(playerIndex);
                }
                case "Free Parking" -> {}  // Do nothing. Too bad!
            }

        }

        // We're dealing with a rented Property
        else {

            // Property is NOT OWNED - buy/auction
            if (gameState.ownership[landingLocation] == -1) {
                String prompt = "Buy / auction " + Board.SQUARES.get(landingLocation).getName();
                signalTurn(1, playerIndex, prompt);
            }

            // Property is OWNED - pay rent
            else if (gameState.ownership[landingLocation] != playerIndex) {
                payRent(landingProperty, playerIndex, gameState.ownership[landingLocation], rentMultiplier);
            }

        }

    }

    /**
     * Perform the action described on a Chance / Community Chest card.
     * @param playerIndex Player index / ID.
     * @param card Card enum object.
     */
    private void performCardAction(int playerIndex, CARD card) {

        // Output card contents
        players[playerIndex].output(playerUUIDs[playerIndex], card.getFullName() + "\n" + card.getDescription());

        switch (card) {

            // Chance \\
            case ADVANCE_TO_BOARDWALK -> {
                int boardwalkIndex = Board.indexOf("Boardwalk");
                int distanceToMove = (boardwalkIndex - gameState.playerLocations[playerIndex] + Board.SQUARES.size()) % Board.SQUARES.size();
                moveToken(playerIndex, distanceToMove);
            }
            case ADVANCE_TO_GO, ADVANCE_TO_GO_2 -> {
                int goIndex = Board.indexOf("GO");
                int distanceToMove = (goIndex - gameState.playerLocations[playerIndex] + Board.SQUARES.size()) % Board.SQUARES.size();
                moveToken(playerIndex, distanceToMove);
            }
            case ADVANCE_TO_ILLINOIS -> {
                int illinoisIndex = Board.indexOf("Illinois Avenue");
                int distanceToMove = (illinoisIndex - gameState.playerLocations[playerIndex] + Board.SQUARES.size()) % Board.SQUARES.size();
                moveToken(playerIndex, distanceToMove);
            }
            case ADVANCE_TO_ST_CHARLES -> {
                int charlesIndex = Board.indexOf("St. Charles Place");
                int distanceToMove = (charlesIndex - gameState.playerLocations[playerIndex] + Board.SQUARES.size()) % Board.SQUARES.size();
                moveToken(playerIndex, distanceToMove);
            }
            case ADVANCE_TO_NEAREST_RAILROAD, ADVANCE_TO_NEAREST_RAILROAD_2 -> {
                int railroadIndex = -1;
                for (int i = 0; i < Board.SQUARES.size(); i++) {
                    if (Board.SQUARES.get((gameState.playerLocations[playerIndex] + i) % Board.SQUARES.size()).color == COLOR_SET.RAILROAD) {
                        railroadIndex = (gameState.playerLocations[playerIndex] + i) % Board.SQUARES.size();
                        break;
                    }
                }
                int distanceToMove = (railroadIndex - gameState.playerLocations[playerIndex] + Board.SQUARES.size()) % Board.SQUARES.size();
                moveToken(playerIndex, distanceToMove, 2.0);
            }
            case ADVANCE_TO_NEAREST_UTILITY -> {

                // Locate nearest utility
                int utilityIndex = -1;
                for (int i = 1; i < Board.SQUARES.size(); i++) {
                    if (Board.SQUARES.get((gameState.playerLocations[playerIndex] + i) % Board.SQUARES.size()).color == COLOR_SET.UTILITY) {
                        utilityIndex = (gameState.playerLocations[playerIndex] + i) % Board.SQUARES.size();
                        break;
                    }
                }

                // Locate the other (remaining) utility
                int otherUtilityIndex = -1;
                for (int i = 1; i < Board.SQUARES.size(); i++) {
                    if (Board.SQUARES.get((utilityIndex + i) % Board.SQUARES.size()).color == COLOR_SET.UTILITY) {
                        otherUtilityIndex = (utilityIndex + i) % Board.SQUARES.size();
                        break;
                    }
                }

                // This card makes landing on an owned utility 10x dice.
                //      This will already be the case if both properties are owned by the same player,
                //      ... so only apply the 2.5x multiplier (10/4) if one player DOES NOT own both utilities.
                double utilityRentMultiplier = 2.5;
                // We don't need to check if the utilities are unowned because if the first one is, the rentMultiplier will never take effect.
                if (gameState.ownership[utilityIndex] == gameState.ownership[otherUtilityIndex])
                    utilityRentMultiplier = 1.0;

                // Calculate distance to move and move token forwards
                int distanceToMove = (utilityIndex - gameState.playerLocations[playerIndex] + Board.SQUARES.size()) % Board.SQUARES.size();
                moveToken(playerIndex, distanceToMove, utilityRentMultiplier);
                // TODO: Bug here - using `distanceToMove` instead of a new roll.
                //       Clarify Monopoly rules: do we just... throw another Dice?

            }
            case COLLECT_50 -> {
                incrementCash(playerIndex, 50);
            }
            case GTFO_JAIL, GTFO_JAIL_2 -> {
                if (gameState.gtfoJailCards[playerIndex] < GameState.MAXIMUM_GTFO_JAIL_CARDS)
                    gameState.gtfoJailCards[playerIndex]++;
            }
            case RETREAT_3_SPACES -> {
                moveToken(playerIndex, -3);
            }
            case GO_TO_JAIL -> {
                jailPlayer(playerIndex);
            }
            case GENERAL_REPAIRS -> {  // Too bad!
                incrementCash(playerIndex, -calculateRepairsCost(playerIndex, 25, 100));
            }
            case PAY_15 -> {
                incrementCash(playerIndex, -15);
            }
            case ADVANCE_TO_READING_RAILROAD -> {
                int readingIndex = Board.indexOf("Reading Railroad");
                int distanceToMove = (readingIndex - gameState.playerLocations[playerIndex] + Board.SQUARES.size()) % Board.SQUARES.size();
                moveToken(playerIndex, distanceToMove);
            }
            case PAY_50_EACH_PLAYER -> {
                // We remove the cash from the player *first*
                incrementCash(playerIndex, -50 * gameState.numPlayers);
                for (int i = 0; i < gameState.cash.length; i++) {
                    if (i == playerIndex) continue;
                    incrementCash(i, 50);
                }
            }
            case COLLECT_150 -> {
                incrementCash(playerIndex, 150);
            }

            // Community Chest \\
            case COLLECT_200 -> {
                incrementCash(playerIndex, 200);
            }
            case PAY_50, PAY_50_2 -> {
                incrementCash(playerIndex, -50);
            }
            case COLLECT_50_EACH_PLAYER -> {
                // We remove the cash from the players *first*
                int payingPlayers = 0;
                for (int i = 0; i < gameState.cash.length; i++) {
                    if (i == playerIndex) continue;
                    if (incrementCash(i, -50))
                        payingPlayers++;
                }
                incrementCash(playerIndex, 50 * payingPlayers);
            }
            case COLLECT_100, COLLECT_100_2, COLLECT_100_3, COLLECT_100_4 -> {
                incrementCash(playerIndex, 100);
            }
            case COLLECT_20 -> {
                incrementCash(playerIndex, 20);
            }
            case PAY_100 -> {
                incrementCash(playerIndex, -100);
            }
            case COLLECT_25 -> {
                incrementCash(playerIndex, 25);
            }
            case STREET_REPAIRS -> {
                incrementCash(playerIndex, -calculateRepairsCost(playerIndex, 40, 115));
            }
            case COLLECT_10 -> {
                incrementCash(playerIndex, 10);
            }
        }

    }

    /**
     * End the last Player's turn and either...
     * A) End the game if we've reached any end condition.
     * B) Proceed with the next Player's turn.
     */
    private void processTurn() {

        // Increment global turn counter
        if (gameState.turnIndicator == players.length - 1)
            currentTurn++;

        // Reset timesRolled values (checks for doubles, etc.)
        if (gameState.turnIndicator != -1)
            gameState.timesRolled[gameState.turnIndicator] = 0;

        // Increment turnIndicator, looping around if needed
        gameState.turnIndicator = (gameState.turnIndicator + 1) % players.length;

        currentLegalActions = generateLegalActions(0);  // resets to start-of-turn actions

        if (isGameOver()) {
            endGame(0);
        }
        // Signal the next Player's turn if not bankrupt, otherwise skip it
        else if (!gameState.playerBankruptcy[gameState.turnIndicator]) {

            // Pre-move actions
            int consecutiveActions = 0;
            while (!endTurnFlag && consecutiveActions < MAX_ACTIONS) {
                String prompt = "Would you like to perform actions before moving?" +
                        "\nInput END_TURN when finished." +
                        "\nYou have " + (MAX_ACTIONS - consecutiveActions) + " actions remaining.";
                signalTurn(2, gameState.turnIndicator, prompt);
                consecutiveActions++;
            }
            endTurnFlag = false;
            consecutiveActions = 0;

            // If Player is in Jail, use the Jail moves set and update turnsInJail accordingly (before & after the turn).
            if (gameState.jailedPlayers[gameState.turnIndicator]) {

                if (gameState.turnsInJail[gameState.turnIndicator] < Monopoly.MAX_TURNS_IN_JAIL)
                    signalTurn(3, gameState.turnIndicator, "YOU ARE IN JAIL!\nIt's your turn! " + PROMPT_DEFAULT);
                else
                    signalTurn(6, gameState.turnIndicator, "YOU ARE IN JAIL!\nIt's your turn! " + PROMPT_DEFAULT + "\nYou must leave Jail this turn.");

                if (gameState.jailedPlayers[gameState.turnIndicator])
                    gameState.turnsInJail[gameState.turnIndicator]++;
                else
                    gameState.turnsInJail[gameState.turnIndicator] = 0;

            }
            else
                signalTurn(7, gameState.turnIndicator, "It's your turn! " + PROMPT_DEFAULT);

            // Post-move actions
            while (!endTurnFlag && consecutiveActions < MAX_ACTIONS) {
                String prompt = "Would you like to perform actions before ending your turn?" +
                        "\nInput END_TURN when finished." +
                        "\nYou have " + (MAX_ACTIONS - consecutiveActions) + " actions remaining.";
                signalTurn(2, gameState.turnIndicator, prompt);
                consecutiveActions++;
            }
            endTurnFlag = false;

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
     * If status code == 0, exit gracefully.
     * Force exit otherwise.
     */
    private void endGame(int statusCode) {
        if (statusCode == 0)
            gameOverFlag = true;
        else
            exit(statusCode);
    }

    ////////////////////////////////////////

    /**
     * Finalize a Trade between two players, the 'pitcher' and 'catcher'.
     * @param pitcherIndex Index of Player 1; the Pitcher.
     * @param catcherIndex Index of Player 2; the Catcher.
     * @param trade Trade object.
     */
    private void acceptTrade(int pitcherIndex, int catcherIndex, Trade trade) {

        int[][] pitcherContents = trade.getContents(true);
        int[][] catcherContents = trade.getContents(false);

        incrementCash(pitcherIndex, catcherContents[0][0]);
        incrementCash(catcherIndex, -catcherContents[0][0]);
        incrementCash(catcherIndex, pitcherContents[0][0]);
        incrementCash(pitcherIndex, -pitcherContents[0][0]);

        gameState.gtfoJailCards[pitcherIndex] += catcherContents[2][0];
        gameState.gtfoJailCards[catcherIndex] -= catcherContents[2][0];
        gameState.gtfoJailCards[catcherIndex] += pitcherContents[2][0];
        gameState.gtfoJailCards[pitcherIndex] -= pitcherContents[2][0];

        for (int propIndex : pitcherContents[1])
            gameState.ownership[propIndex] = catcherIndex;
        for (int propIndex : catcherContents[1])
            gameState.ownership[propIndex] = pitcherIndex;

    }

    /**
     * Buy a Property for the Player referenced by `playerIndex`.
     * Pre-req: Passed all checks.
     */
    private void buyProperty(int playerIndex, Property property) {
        incrementCash(playerIndex, -property.marketPrice);  // Take cash out of account *first*!
        gameState.ownership[Board.indexOf(property.getName())] = playerIndex;
    }

    /**
     * Buy a Property for the referenced Player at a custom cost.
     * Used primarily in the auction procedure.
     */
    private void buyProperty(int playerIndex, Property property, int customCost) {
        incrementCash(playerIndex, -customCost);
        gameState.ownership[Board.indexOf(property.getName())] = playerIndex;
    }

    /**
     * Begin the auction sub-routine for a Property.
     * @param playerIndex Index / ID of the Player who started the auction.
     * @param property Property up for auction.
     */
    private void auctionProperty(int playerIndex, Property property) {

        // TODO: Fix "maximum bid" portion.
        //      ==FIXED== 1) Purchases property for wrong amount (150 vs. 1000 when 150 was never even a bid!). ==FIXED==
        //      2) Allows for bids under the current bid (e.g. 90 when max bid is 200).
        //          Note: Just implemented this: it should enforce bids as max bids. Must test.

        // Initialize relevant fields
        this.biddingProperty = property;
        this.auctionBids = new ArrayList<>();
        for (int i = 0; i < gameState.numPlayers; i++)
            auctionBids.add(STARTING_BID_AMOUNT);

        // Auction procedure
        boolean multiplePlayersRemaining = true;
        while (multiplePlayersRemaining) {
            for (int i = 0; i < gameState.numPlayers; i++) {

                int pIndex = (playerIndex + i) % gameState.numPlayers;

                // Skip turns of Players who've dropped out of the auction
                if (auctionBids.get(pIndex) < 0) continue;

                // Check # of Players remaining in the auction.
                // Exit the loop if only 1 Player remains.
                int playersRemaining = gameState.numPlayers;
                for (int bid : auctionBids) {
                    if (bid < 0)
                        playersRemaining--;
                }
                if (playersRemaining < 2) {  // This should never be <= 0, but just in case
                    multiplePlayersRemaining = false;
                    break;
                }

                int maxBid = Collections.max(auctionBids);

                // Signal Player for bid
                String prompt = "What is your bid on " + property.getName() + "?" +
                        "\nBid -1 to concede." +
                        "\nCurrent maximum bid: " + maxBid;
                signalTurn(5, pIndex, prompt);

                // Replace all invalid bids with -1
                if (auctionBids.get(pIndex) < STARTING_BID_AMOUNT || auctionBids.get(pIndex) < maxBid)
                    auctionBids.set(pIndex, -1);

            }
        }

        // Find the maximum bid and winning player
        int price = -1;  // Maximum bid amount
        int winner = 0;  // Index of the winning player
        for (int i = 0; i < auctionBids.size(); i++) {
            int bid = auctionBids.get(i);
            if (bid > -1 && bid > price) {
                price = bid;
                winner = i;
            }
        }

        // Call buyProperty() for auction winner, with the winning bid as the price
        buyProperty(winner, property, price);

        // Null relevant fields
        this.auctionBids = null;
        this.biddingProperty = null;

    }

    /**
     * Mortgage a Property.
     * Pre-req: Passed all checks.
     */
    private void mortgageProperty(Property property) {
        int propertyIndex = Board.indexOf(property.getName());
        int playerIndex = gameState.ownership[propertyIndex];
        gameState.mortgages[propertyIndex] = true;
        incrementCash(playerIndex, (int)(property.marketPrice * property.mortgageDivisor));
    }

    /**
     * Unmortgage a Property.
     * Pre-req: Passed all checks.
     */
    private void unmortgageProperty(Property property) {
        int propertyIndex = Board.indexOf(property.getName());
        int playerIndex = gameState.ownership[propertyIndex];
        gameState.mortgages[propertyIndex] = false;
        incrementCash(playerIndex, -((int)(property.marketPrice * property.mortgageDivisor * Property.UNMORTGAGE_INTEREST)));
    }

    /**
     * Buy a house on a given Property.
     * Pre-req: Passed all checks.
     */
    private void buyHouse(Property property, boolean hotel) {

        int propertyIndex = Board.indexOf(property.getName());
        int playerIndex = gameState.ownership[propertyIndex];

        gameState.houses[propertyIndex]++;
        if (hotel) {
            gameState.remainingHouses += 4;
            gameState.remainingHotels--;
        } else {
            gameState.remainingHouses--;
        }

        incrementCash(playerIndex, -property.baseHouseCost);

    }

    /**
     * Sell a house on a given Property.
     * Pre-req: Passed all checks.
     */
    private void sellHouse(Property property, boolean hotel) {

        int propertyIndex = Board.indexOf(property.getName());
        int playerIndex = gameState.ownership[propertyIndex];

        gameState.houses[propertyIndex]--;
        if (hotel) {
            gameState.remainingHouses -= 4;
            gameState.remainingHotels++;
        } else {
            gameState.remainingHouses++;
        }

        incrementCash(playerIndex, (int)(property.baseHouseCost * property.houseSellDivisor));

    }

    /**
     * Method called when a Player cannot pay a charge.
     * Will result in the Player making up the funds via secondary means (e.g. mortgages, trades),
     * ... or declaring bankruptcy.
     * @param amount Amount owed.
     * @param playerIndex Player that needs to pay.
     * @return True if the dispute was resolved, false if Player declared bankruptcy.
     */
    private boolean cannotPay(int playerIndex, int amount) {

        for (int i = MAX_ACTIONS; i > 0; i--) {
            // Give player a chance to make up the funds
            String prompt = "You need to make up the funds to pay $" + amount + ".\nYou have " + i + " actions remaining.";
            signalTurn(4, playerIndex, prompt);
            if (gameState.cash[playerIndex] >= amount)
                return true;
            if (gameState.playerBankruptcy[playerIndex])
                break;
        }

        // Bankrupt the player if they haven't raised the funds & haven't already bankrupted
        if (!gameState.playerBankruptcy[playerIndex])
            bankruptPlayer(playerIndex);
        return false;

    }

    /**
     * Transfer rent funds from Player A to Player B.
     * @param property Property landed on.
     * @param playerIndex Index / ID of Player A (lander).
     * @param renterIndex Index / ID of Player B (renter).
     */
    private void payRent(Property property, int playerIndex, int renterIndex, double rentMultiplier) {

        // We reference the results of the last dice roll for Utilities' sake.
        // Teleporting to a Utility via a Card means the distance traveled is irrelevant, so we must reference the dice roll itself.
        int rentCost = (int)(property.calculateRent(this.getGameState(), lastDiceRoll.result()) * rentMultiplier);
        incrementCash(playerIndex, -rentCost);
        incrementCash(renterIndex, rentCost);

    }

    /**
     * Increment Player's cash by a given amount.
     * This amount can be negative, but this will be handled by a sub-function decrementCash().
     * TODO: Incorporate return value where used
     * @param playerIndex Player index / ID.
     * @param amount Amount of cash to give / remove from the Player. Can be negative.
     * @return Success (t/f). Will always be true for positive values.
     */
    private boolean incrementCash(int playerIndex, int amount) {

        // If negative amount, delegate task to decrementCash() instead
        if (amount < 0)
            return decrementCash(playerIndex, -amount);

        // If positive amount...
        gameState.cash[playerIndex] += amount;
        return true;

    }

    /**
     * Decrement Player's cash by a given amount.
     * This is a sub-function of incrementCash(). Don't call this in the wild.
     * @param playerIndex Player index / ID.
     * @param amount Amount of cash to remove from the player. Always positive.
     * @return True if paid successfully, false if Player entered bankruptcy procedure.
     */
    private boolean decrementCash(int playerIndex, int amount) {

        int playerCash = gameState.cash[playerIndex];
        if (amount > playerCash)
            return cannotPay(playerIndex, amount);  // Proceed w/ charge resolving procedure. Returns false if Player bankrupted.

        gameState.cash[playerIndex] -= amount;  // Remember `amount` is positive.
        return true;

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
        jailPlayer(playerIndex, Board.indexOf("Jail"));
    }

    /**
     * Free a Player from Jail.
     * @param context 0 for dice exit, 1 for bail exit, 2 for card exit.
     */
    private void freePlayer(int playerIndex, int context) {
        switch (context) {
            case 1 -> incrementCash(playerIndex, -Property.BAIL_AMOUNT);
            case 2 -> gameState.gtfoJailCards[playerIndex]--;
        }
        gameState.jailedPlayers[playerIndex] = false;
    }

    /**
     * Mark a Player's bankruptcy flag and output the event.
     * Bankrupt Players are handled like so:
     *      1) Bankrupt Players' turns are ignored in any & all cases.
     *      2) Bankrupt Players cannot trade.
     *      3) Bankrupt Players are disqualified from winning the game.
     * @param playerIndex Player index / ID.
     */
    private void bankruptPlayer(int playerIndex) {
        gameState.playerBankruptcy[playerIndex] = true;
        for (int i = 0; i < gameState.numPlayers; i++) {
            if (gameState.ownership[i] == playerIndex) {
                gameState.ownership[i] = -1;
                gameState.mortgages[i] = false;
                gameState.houses[i] = 0;
            }
        }
        players[playerIndex].output(playerUUIDs[playerIndex], players[playerIndex].getName() + " bankrupted!!");
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
            // Case 0 is used for beginning-of-turn actions. The player must be not in Jail.
            case 0 -> legalActions = new HashSet<>(List.of(GameAction.START_ACTIONS));
            // Case 1 is used for landing on a Property.
            case 1 -> legalActions = new HashSet<>(List.of(GameAction.PROPERTY_BUY_OR_AUCTION));
            // Case 2 is used for end-of-turn actions e.g. building houses, mortgaging, trading.
                // Note for later: do we want TRADE_ACCEPT and TRADE_REJECT included in case 2? Atm I say no.
            case 2 -> legalActions = new HashSet<>(List.of(GameAction.END_ACTIONS));
            // Case 3 is used for beginning a turn in Jail.
            case 3 -> legalActions = new HashSet<>(List.of(GameAction.JAIL_ACTIONS));
            // Case 4 is used for making up funds when unable to pay.
            case 4 -> legalActions = new HashSet<>(List.of(GameAction.SELL_ACTIONS));
            // Case 5 is used for the auction sub-routine.
            case 5 -> legalActions = new HashSet<>(List.of(GameAction.AUCTION_BID));
            // Case 6 is used for exiting Jail by force.
            case 6 -> legalActions = new HashSet<>(List.of(GameAction.JAIL_PAY_BAIL, GameAction.JAIL_USE_CARD));
            // Case 7 is used for forcing a move.
            case 7 -> legalActions = new HashSet<>(List.of(GameAction.MOVE_THROW_DICE));
            // Case 8 is used for responding to trades.
            case 8 -> legalActions = new HashSet<>(List.of(GameAction.TRADE_RESPOND));
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

    /**
     * Calculate the total amount due for the general / street repairs cards.
     * @param playerIndex Index / ID of the Player who drew the card.
     * @param houseCost Cost per owned house.
     * @param hotelCost Cost per owned hotel.
     * @return Total cost
     */
    private int calculateRepairsCost(int playerIndex, int houseCost, int hotelCost) {
        int amountOwed = 0;
        for (int i = 0; i < gameState.houses.length; i++) {
            if (gameState.houses[i] > 0) {
                if (gameState.ownership[i] == playerIndex) {
                    if (gameState.houses[i] == 5)
                        amountOwed += hotelCost;
                    else
                        amountOwed += (houseCost * gameState.houses[i]);
                }
            }
        }
        return amountOwed;
    }

    ////////////////////////////////////////

    /**
     * @return A *copy* of the Game State object.
     * We turn a copy because GameState is highly mutable.
     */
    public GameState getGameState() {
        return new GameState(gameState);
    }

    public void warn(int code) {  // TODO: Update warn() (and add more uses)
        System.err.println("Warning called for Game id=" + id + ", CODE " + code + " || " + Calendar.getInstance());
    }

}