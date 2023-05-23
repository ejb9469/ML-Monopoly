import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Class to handle gameplay loop.
 */
public class Game implements OutputsWarnings {

    private static int ID_INCREMENT = 0;

    private int id = ID_INCREMENT++;

    private GameState gameState;

    private Player[] players;
    private UUID[] playerUUIDs;

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
        if (depth == 0) return;
        int keyIndex = keyExists(key);
        if (keyIndex == -1) {
            warn(1);
            return;
        }
        Player player = players[keyIndex];
        switch (action) {
            case MOVE_THROW_DICE -> {
                Dice dice = new Dice();
                int toss = dice.toss();
                moveTokenForwards(keyIndex, toss);
                // TODO: doubles
            }
        }
    }

    /**
     * Request the Game object perform an action, but DISALLOW RECURSION.
     */
    public void requestAction(GameAction action, UUID key, GameObject wrapper) {
        requestAction(action, key, wrapper, 1);
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
                    jailPlayer(playerIndex, board.indexOf("Jail"));
                }
                case "Free Parking" -> {}  // Do nothing. Too bad!
            }
        }

        // We're dealing with a rented Property
        else {

            // Property is NOT OWNED
            if (gameState.ownership[landingLocation] == 0) {
                // TODO
            }

            // Property is OWNED
            else {
                payRent(landingProperty, playerIndex, gameState.ownership[landingLocation]);
            }

        }


    }

    private void endTurn() {
        gameState.turnIndicator = (gameState.turnIndicator + 1) % players.length;
    }

    ////////////////////////////////////////
    // TODO: Handle race conditions

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

    ////////////////////////////////////////

    /**
     * @param execFlowCode Indicator of when in the game logic the function is called.
     * @return An array of legal actions for a given point in execution.
     */
    private GameAction[] generateLegalActions(int execFlowCode) {
        // TODO!!
        return GameAction.values();
    }

    private int keyExists(UUID key) {
        for (int i = 0; i < playerUUIDs.length; i++) {
            if (playerUUIDs[i].equals(key))
                return i;
        }
        return -1;
    }

    ////////////////////////////////////////


    // Getters
    public Player[] getPlayers() {
        return players;
    }

    /**
     * @return A *copy* of the Game State object.
     * We turn a copy because GameState is highly mutable.
     */
    public GameState getGameState() {
        return new GameState(gameState);
    }


    public void warn(int code) {
        System.err.println("Warning called for Game id=" + id + ", CODE " + code + " || " + Calendar.getInstance());
    }

}