public class Property {

    protected String name;

    protected COLOR_SET color;
    protected int marketPrice;
    protected int mortgageDivisor;
    protected int baseRent;
    protected int baseHouseCost;
    protected int houseSellDivisor;
    protected int[] rentTable;

    protected int numHouses = 0;  // hotel = 5

    public Property(String name, COLOR_SET color) {
        this.name = name;
        this.color = color;
    }

    public Property(String name, COLOR_SET color, int marketPrice, int mortgageDivisor, int baseRent, int baseHouseCost, int houseSellDivisor, int[] rentTable) {
        this(name, color);
        this.marketPrice = marketPrice;
        this.mortgageDivisor = mortgageDivisor;
        this.baseRent = baseRent;
        this.baseHouseCost = baseHouseCost;
        this.houseSellDivisor = houseSellDivisor;
        this.rentTable = rentTable;
    }

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