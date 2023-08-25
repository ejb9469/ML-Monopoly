package server;

import java.util.ArrayList;
import java.util.List;

public class Trade {

    private final int pitcherIndex;
    private final int catcherIndex;

    private int pitcherCash;
    private int[] pitcherPropertyIndexes;
    private int pitcherNumGTFOJailCards;

    private int catcherCash;
    private int[] catcherPropertyIndexes;
    private int catcherNumGTFOJailCards;

    public int status = 0;  // 0 = pitched, 1 = rejected, 2 = accepted
    public List<Trade> history = new ArrayList<>();


    public Trade(int pitcherIndex, int catcherIndex,
                 int pitcherCash, int[] pitcherPropertyIndexes, int pitcherNumGTFOJailCards,
                 int catcherCash, int[] catcherPropertyIndexes, int catcherNumGTFOJailCards
    ) {
        this.pitcherIndex = pitcherIndex;
        this.catcherIndex = catcherIndex;
        this.pitcherCash = pitcherCash;
        this.pitcherPropertyIndexes = pitcherPropertyIndexes;
        this.pitcherNumGTFOJailCards = pitcherNumGTFOJailCards;
        this.catcherCash = catcherCash;
        this.catcherPropertyIndexes = catcherPropertyIndexes;
        this.catcherNumGTFOJailCards = catcherNumGTFOJailCards;
    }

    public Trade(Trade trade) {
        this(trade.pitcherIndex, trade.catcherIndex,
                trade.pitcherCash, trade.pitcherPropertyIndexes, trade.pitcherNumGTFOJailCards,
                trade.catcherCash, trade.catcherPropertyIndexes, trade.catcherNumGTFOJailCards
        );
        this.status = trade.status;
        this.history = trade.history;
    }
    

    public int getPitcherIndex() {
        return pitcherIndex;
    }

    public int getCatcherIndex() {
        return catcherIndex;
    }

    /**
     * Parses a serialized 'Trade String' and returns a proper Trade object.
     * <br>Example Trade String included as a comment in the parseTradeString() method.
     * @throws NumberFormatException In cases where the method attempts to parse a non-number in a number's place.
     * @throws IndexOutOfBoundsException In cases where there are not eight args (deliminated by the pipe character).
     */
    // TODO: Add reverse function - i.e. returns Trade String from Trade object. This is probably vestigial.
    public static Trade parseTradeString(String tradeString) throws NumberFormatException, IndexOutOfBoundsException {

        // Example of a Trade String:
        // 0|1|500|1|39|0|0|1,3
        // Translation:
        // 0 - Pitcher Index (player at index 0)
        // 1 - Catcher Index (player at index 1)
        // 500 - Pitcher cash ($500)
        // 1 - Pitcher # GTFO jail cards (1)
        // 39 - Pitcher properties list (39; Boardwalk)
        // 0 - Catcher cash ($0)
        // 0 - Catcher # GTFO jail cards (0)
        // 1,3 - Catcher properties list (1; Mediterranean Ave, 3; Baltic Ave)

        String[] args = tradeString.strip().split("\\|");

        if (args.length != 8) throw new IndexOutOfBoundsException();

        int pitcherIndex = Integer.parseInt(args[0]);
        int catcherIndex = Integer.parseInt(args[1]);

        int pitcherCash = Integer.parseInt(args[2]);
        int pitcherGTFOJailCards = Integer.parseInt(args[3]);

        String[] pitcherPropertiesStr = args[4].split(",");
        int[] pitcherPropertyIndexes = new int[pitcherPropertiesStr.length];
        for (int i = 0; i < pitcherPropertiesStr.length; i++) {
            String pitcherPropStr = pitcherPropertiesStr[i].strip();
            int pitcherPropIndex = Integer.parseInt(pitcherPropStr);
            pitcherPropertyIndexes[i] = pitcherPropIndex;
        }

        int catcherCash = Integer.parseInt(args[5]);
        int catcherGTFOJailCards = Integer.parseInt(args[6]);

        String[] catcherPropertiesStr = args[7].split(",");
        int[] catcherPropertyIndexes = new int[catcherPropertiesStr.length];
        for (int i = 0; i < catcherPropertiesStr.length; i++) {
            String catcherPropStr = catcherPropertiesStr[i].strip();
            int catcherPropIndex = Integer.parseInt(catcherPropStr);
            catcherPropertyIndexes[i] = catcherPropIndex;
        }

        return new Trade(
                pitcherIndex, catcherIndex,
                pitcherCash, pitcherPropertyIndexes, pitcherGTFOJailCards,
                catcherCash, catcherPropertyIndexes, catcherGTFOJailCards
        );

    }

    public int[][] getContents(boolean pitcher) {

        int[][] contents;

        if (pitcher) {
            contents = new int[][]{
                    new int[]{pitcherCash},
                    pitcherPropertyIndexes,
                    new int[]{pitcherNumGTFOJailCards}
            };
        } else {
            contents = new int[][]{
                    new int[]{catcherCash},
                    catcherPropertyIndexes,
                    new int[]{catcherNumGTFOJailCards}
            };
        }

        return contents;

    }

    /**
     * Update method for trades.
     * @param pitcher True if dealing with pitcher's end, false if catcher's end.
     * @param contents Updated contents value.
     * @param recordCounter Adds to `history` (List) if true.
     */
    public void counter(boolean pitcher, int[][] contents, boolean recordCounter) {

        if (recordCounter)
            history.add(new Trade(this));

        if (pitcher) {
            pitcherCash = contents[0][0];
            pitcherPropertyIndexes = contents[1];
            pitcherNumGTFOJailCards = contents[2][0];
        } else {
            catcherCash = contents[0][0];
            catcherPropertyIndexes = contents[1];
            catcherNumGTFOJailCards = contents[2][0];
        }

    }

    public String toString() {

        String out = "From: " + pitcherIndex;
        out += "\nTo: " + catcherIndex + "\n";

        out += "\nOffering:\n";

        StringBuilder pitcherOffer = new StringBuilder("$" + pitcherCash);
        pitcherOffer.append("\n").append(pitcherNumGTFOJailCards).append("x GTFO Jail Cards").append("\nProperties List: {");
        for (int propIndex : pitcherPropertyIndexes) {
            if (propIndex >= 0 && propIndex < Board.SQUARES.size()) {
                pitcherOffer.append(Board.SQUARES.get(propIndex).getName());
                pitcherOffer.append(", ");
            }
        }
        if (pitcherPropertyIndexes.length != 0)
            pitcherOffer.delete(pitcherOffer.length()-2, pitcherOffer.length());
        pitcherOffer.append("}\n");
        out += pitcherOffer;

        out += "\nReceiving:\n";

        StringBuilder catcherOffer = new StringBuilder("$" + catcherCash);
        catcherOffer.append("\n").append(catcherNumGTFOJailCards).append("x GTFO Jail Cards").append("\nProperties List: {");
        for (int propIndex : catcherPropertyIndexes) {
            if (propIndex >= 0 && propIndex < Board.SQUARES.size()) {
                catcherOffer.append(Board.SQUARES.get(propIndex).getName());
                catcherOffer.append(", ");
            }
        }
        if (catcherPropertyIndexes.length != 0)
            catcherOffer.delete(catcherOffer.length()-2, catcherOffer.length());
        catcherOffer.append("}");
        out += catcherOffer;

        out += "\n";

        return out;

    }

}