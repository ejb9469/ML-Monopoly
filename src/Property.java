public class Property {

    public static final int BAIL_AMOUNT = 50;
    public static final double UNMORTGAGE_INTEREST = 1.1;

    private String name;

    protected COLOR_SET color;
    protected int marketPrice;
    protected double mortgageDivisor;
    protected int baseRent;
    protected int baseHouseCost;
    protected int houseSellDivisor;
    protected int[] rentTable;

    protected int numHouses = 0;  // hotel = 5

    /**
     * Creates a 'blank' property.
     * This constructor should be used for debug purposes only.
     */
    public Property(String name, COLOR_SET color) {
        this.name = name;
        this.color = color;
    }

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
    public Property(String name, COLOR_SET color, int marketPrice, double mortgageDivisor, int baseRent, int baseHouseCost, int houseSellDivisor, int[] rentTable) {
        this(name, color);
        this.marketPrice = marketPrice;
        this.mortgageDivisor = mortgageDivisor;
        this.baseRent = baseRent;
        this.baseHouseCost = baseHouseCost;
        this.houseSellDivisor = houseSellDivisor;
        this.rentTable = rentTable;
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

    public int numHouses() {
        return numHouses;
    }

    public int calculateRent() {
        return rentTable[numHouses-1];
    }

}