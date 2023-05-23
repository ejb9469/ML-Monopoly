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

    // Constructs a new Game
    public Game(int numPlayers, String[] names, Communicator communicator) {
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
     * Request the Game perform an action.
     * @param action The action to perform.
     */
    public void requestAction(GameAction action, UUID key, GameObject wrapper, int depth) {
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
    public void requestAction(GameAction action, UUID key, GameObject wrapper) {
        requestAction(action, key, wrapper, 0);
    }

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
                case "Free Parking" -> {}  // Do nothing
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


    // TODO: Handle negative balances
    // TODO: Handle race conditions
    private void incrementCash(int playerIndex, int amount) {
        gameState.cash[playerIndex] += amount;
    }

    private void payRent(Property property, int victimIndex, int renterIndex) {
        int rentCost = property.calculateRent();
        incrementCash(victimIndex, -rentCost);
        incrementCash(renterIndex, rentCost);
    }


    private void jailPlayer(int playerIndex, int jailIndex) {
        gameState.jailedPlayers[playerIndex] = true;
        gameState.playerLocations[playerIndex] = jailIndex;
    }


    private int keyExists(UUID key) {
        for (int i = 0; i < playerUUIDs.length; i++) {
            if (playerUUIDs[i].equals(key))
                return i;
        }
        return -1;
    }


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