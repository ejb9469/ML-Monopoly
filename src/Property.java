public class Property {

    public static final int BAIL_AMOUNT = 50;
    public static final double UNMORTGAGE_INTEREST = 1.1;

    private final String name;

    protected COLOR_SET color;
    protected int marketPrice;
    protected double mortgageDivisor;
    protected int baseRent;
    protected int baseHouseCost;
    protected double houseSellDivisor;
    protected int[] rentTable;

    /**
     * Constructs a Property object with given specifications.
     * @param name Property name.
     * @param color Property 'Set'.
     * @param marketPrice Market (initial purchase) price, if applicable.
     * @param mortgageDivisor Reduction of market price for mortgaging.
     * @param baseRent Base rent amount without buildings or sets.
     * @param baseHouseCost Base cost per house.
     * @param houseSellDivisor Reduction of house price for selling.
     * @param rentTable Table of rent amounts given 1-5 houses.
     */
    public Property(String name, COLOR_SET color, int marketPrice, double mortgageDivisor, int baseRent, int baseHouseCost, double houseSellDivisor, int[] rentTable) {
        this(name, color);
        this.marketPrice = marketPrice;
        this.mortgageDivisor = mortgageDivisor;
        this.baseRent = baseRent;
        this.baseHouseCost = baseHouseCost;
        this.houseSellDivisor = houseSellDivisor;
        this.rentTable = rentTable;
    }

    // Constructor used for Railroads.
    public Property(String name, COLOR_SET color, int marketPrice, double mortgageDivisor, int baseRent) {
        this(name, color);
        this.marketPrice = marketPrice;
        this.mortgageDivisor = mortgageDivisor;
        this.baseRent = baseRent;
    }

    // Constructor used for Utilities.
    public Property(String name, COLOR_SET color, int marketPrice, double mortgageDivisor) {
        this(name, color);
        this.marketPrice = marketPrice;
        this.mortgageDivisor = mortgageDivisor;
    }

    // Creates a 'blank' property.
    // This constructor should be used for debug purposes only.
    public Property(String name, COLOR_SET color) {
        this.name = name;
        this.color = color;
    }

    /**
     * @return True if a space like Jail, Taxes, Free Parking, etc. False otherwise.
     */
    public boolean isFunctionalOnly() {
        return this.color == COLOR_SET.FUNCTION;
    }

    public String getName() {
        return name;
    }

    /**
     * Calculate & return rent value for a [non-functional] Property.
     * @param gameState Game State context.
     * @param board Associated Board object, only referenced when calculating rent for Railroads and Utilities.
     * @param roll Dice roll value, only referenced when calculating rent for Utilities.
     * @return Rent value for Property & context.
     */
    public int calculateRent(GameState gameState, Board board, int roll) {
        int[] ownership = gameState.ownership;
        switch (color) {
            case RAILROAD -> {
                int railroadsOwned = 1;
                int pIndex = board.indexOf(this.name);
                for (int i = 0; i < board.getSquares().size(); i++) {
                    Property property = board.getSquares().get(i);
                    if (i == pIndex || property.color != COLOR_SET.RAILROAD) continue;
                    if (ownership[i] == ownership[pIndex])
                        railroadsOwned++;
                }
                return (baseRent * railroadsOwned);
            }
            case UTILITY -> {
                boolean multipleUtilities = false;
                int pIndex = board.indexOf(this.name);
                for (int i = 0; i < board.getSquares().size(); i++) {
                    Property property = board.getSquares().get(i);
                    if (i == pIndex || property.color != COLOR_SET.UTILITY) continue;
                    if (ownership[i] == ownership[pIndex]) {
                        multipleUtilities = true;
                        break;
                    }
                }
                if (multipleUtilities)
                    return (roll * 10);
                else
                    return (roll * 4);
            }
            default -> {
                return rentTable[gameState.houses[board.indexOf(this.name)]];
            }
        }
    }

    // Wrapper function used EXCLUSIVELY for non-Railroad and non-Utility Properties.
    // Included for posterity's sake.
    public int calculateRent() {
        return calculateRent(null, null, 0);
    }

}