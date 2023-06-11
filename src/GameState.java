import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Contains the complete state of a Game to be easily consumed by Agents.
 */
public class GameState {

    // (No need for a 'parent' variable.)

    public static final int MAXIMUM_GTFO_JAIL_CARDS = 2;
    public static final int STARTING_CASH = 1500;

    int numPlayers;

    public int turnIndicator;
    public boolean[] mortgages;
    public int[] ownership;
    public int[] cash;
    public int[] houses;  // '5 houses' will equal a hotel
    public int[] playerLocations;
    public int[] timesRolled;
    public boolean[] jailedPlayers;
    public int[] gtfoJailCards;
    public boolean[] playerBankruptcy;

    public int remainingHouses;
    public int remainingHotels;

    public CardStack chance;
    public CardStack communityChest;

    // Called when constructing a GameState manually.
    public GameState(int numPlayers, int turnIndicator, boolean[] mortgages, int[] ownership, int[] cash, int[] houses, int[] playerLocations, int[] timesRolled, boolean[] jailedPlayers, int[] gtfoJailCards, boolean[] playerBankruptcy, int remainingHouses, int remainingHotels, CardStack chance, CardStack communityChest) {
        this.numPlayers = numPlayers;
        this.turnIndicator = turnIndicator;
        this.mortgages = mortgages;
        this.ownership = ownership;
        this.cash = cash;
        this.houses = houses;
        this.playerLocations = playerLocations;
        this.timesRolled = timesRolled;
        this.jailedPlayers = jailedPlayers;
        this.gtfoJailCards = gtfoJailCards;
        this.playerBankruptcy = playerBankruptcy;
        this.remainingHouses = remainingHouses;
        this.remainingHotels = remainingHotels;
        this.chance = chance;
        this.communityChest = communityChest;
    }

    // Called when constructing a new / starting GameState.
    public GameState(int numPlayers) {
        this(numPlayers, numPlayers, new boolean[Board.SQUARES.size()], new int[Board.SQUARES.size()], new int[numPlayers], new int[Board.SQUARES.size()], new int[numPlayers], new int[numPlayers], new boolean[numPlayers], new int[numPlayers], new boolean[numPlayers], 32, 12, new CardStack(CardStack.CHANCE_DEFAULT), new CardStack(CardStack.COMMUNITY_DEFAULT));
        initializeStartingValues();
    }

    // Called when cloning a GameState.
    public GameState(GameState original) {
        this(original.numPlayers, original.turnIndicator, original.mortgages, original.ownership, original.cash, original.houses, original.playerLocations, original.timesRolled, original.jailedPlayers, original.gtfoJailCards, original.playerBankruptcy, original.remainingHouses, original.remainingHotels, original.chance, original.communityChest);
    }

    /**
     * Initialize fields in accordance to the Monopoly starting position.
     * Assumes fields are non-null.
     */
    private void initializeStartingValues() {
        Arrays.fill(this.cash, STARTING_CASH);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof GameState gs))
            return super.equals(object);
        return (this.turnIndicator == gs.turnIndicator && Arrays.equals(this.mortgages, gs.mortgages) && Arrays.equals(this.ownership, gs.ownership) && Arrays.equals(this.cash, gs.cash) && Arrays.equals(this.houses, gs.houses) && Arrays.equals(this.playerLocations, gs.playerLocations) && Arrays.equals(this.timesRolled, gs.timesRolled) && Arrays.equals(this.jailedPlayers, gs.jailedPlayers) && Arrays.equals(this.gtfoJailCards, gs.gtfoJailCards) && Arrays.equals(this.playerBankruptcy, gs.playerBankruptcy) && this.chance.equals(gs.chance) && this.communityChest.equals(gs.communityChest));
    }

    @Override
    public String toString() {

        StringBuilder str = new StringBuilder();
        str.append(this.getClass().getName());
        str.append(" {\n");
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            str.append("\t");
            try {
                str.append(field.getName());
                str.append(": ");
                if (field.get(this) instanceof boolean[])
                    str.append(Arrays.toString((boolean[]) field.get(this)));
                else if (field.get(this) instanceof int[])
                    str.append(Arrays.toString((int[]) field.get(this)));
                else if (field.get(this) instanceof CardStack)

                str.append(field.get(this));
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
            str.append("\n");
        }
        str.append("}");
        return str.toString();
    }

}