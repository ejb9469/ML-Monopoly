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

    public void counter(boolean pitcher, int[][] contents) {

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

}