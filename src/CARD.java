import java.util.ArrayList;
import java.util.List;

// List of Community Chest cards:
// 1. Advance to Go
// 2. Bank error in your favor, collect $200.
// 3. Doctor's fee pay $50
// 4. From sale of stock you get $50
// 5. GTFO Jail Free
// 6. Go to Jail
// 7. Holiday fund matures, receive $100.
// 8. Income tax refund, collect $20.
// 9. It is your birthday, collect $100.
// 10. Life insurance matures, collect $100.
// 11. Pay hospital fees of $100.
// 12. Pay school fees of $50.
// 13. Receive $25 consultancy fee
// 14. Street repairs. $40 per house and $115 per hotel.
// 15. Second prize in a beauty contest, collect $10.
// 16. You inherit $100.

public enum CARD {

    ADVANCE_TO_BOARDWALK(true, "Advance to Boardwalk"),
    ADVANCE_TO_GO(true, "Advance to GO (Collect $200)"),
    ADVANCE_TO_ILLINOIS(true, "Advance to Illinois Avenue", "If you pass GO, collect $200."),
    ADVANCE_TO_ST_CHARLES(true, "Advance to St. Charles Place", "If you pass GO, collect $200."),
    ADVANCE_TO_NEAREST_RAILROAD(true, "Advance to the nearest Railroad", "If unowned, you may buy it from the Bank. If owned, pay owner twice the rent to which they are otherwise entitled."),
    ADVANCE_TO_NEAREST_RAILROAD_2(CARD.ADVANCE_TO_NEAREST_RAILROAD),
    ADVANCE_TO_NEAREST_UTILITY(true, "Advance to the nearest Utility", "If unowned, you may buy it from the Bank. If owned, throw dice and pay owner 10x amount thrown."),
    COLLECT_50(true, "Bank pays you a dividend of $50"),
    GTFO_JAIL(true, "Get Out of Jail Free", "This card may be kept until needed, or sold."),
    RETREAT_3_SPACES(true, "Go back 3 spaces"),
    GO_TO_JAIL(true, "Go to Jail", "Do not pass GO, do not collect $200."),
    GENERAL_REPAIRS(true, "Make general repairs on all your property", "For each house, pay $25. For each hotel, pay $100."),
    PAY_15(true, "Pay Poor Tax of $15"),
    ADVANCE_TO_READING_RAILROAD(true, "Advance to Reading Railroad", "If you pass GO, collect $200."),
    PAY_50_EACH_PLAYER(true, "You have been elected Chairman of the Board", "Pay each player $50."),
    COLLECT_150(true, "Your building and loan matures", "Collect $150."),

    ADVANCE_TO_GO_2(false, "Advance to GO (Collect $200)"),
    COLLECT_200(false, "Bank error in your favor, collect $200."),
    PAY_50(false, ""),
    COLLECT_50_2(false, ""),
    GTFO_JAIL_2(false, ""),
    GO_TO_JAIL_2(false, ""),
    COLLECT_100(false, ""),
    COLLECT_20(false, ""),
    COLLECT_100_2(false, ""),
    COLLECT_100_3(false, ""),
    PAY_100(false, ""),
    PAY_50_2(false, ""),
    COLLECT_25(false, ""),
    STREET_REPAIRS(false, ""),
    COLLECT_10(false, ""),
    COLLECT_100_4(false, "");

    private final boolean chance;
    private final String fullName;
    private final String description;

    private CARD(boolean chance, String fullName, String description) {
        this.chance = chance;
        this.fullName = fullName;
        this.description = description;
    }

    private CARD(boolean chance, String fullName) {
        this(chance, fullName, "");
    }

    private CARD(CARD duplicate) {
        this(duplicate.chance, duplicate.fullName, duplicate.description);
    }

    /**
     * @param communityChest If true, grab from the Community Chest set. If false, grab from the Chance set.
     * @return List of Cards of either the Chance or Community Chest set.
     */
    public static List<CARD> getCardSet(boolean communityChest) {
        List<CARD> chanceCards = new ArrayList<>();
        for (CARD card : CARD.values()) {
            // Cool XOR trick - adds the card if only one of card.chance & communityChest are true, but not both
            if (card.chance ^ communityChest)
                chanceCards.add(card);
        }
        return chanceCards;
    }

    public boolean isChance() {
        return chance;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDescription() {
        return description;
    }

}

// List of Chance cards:
    // 1. Advance to Boardwalk
    // 2. Advance to Go
    // 3. Advance to Illinois. (if you pass Go, collect $200)
    // 4. Advance to St. Charles.
    // 5. Advance to the nearest railroad. If owned, pay owner 2x.
    // 6. Duplicate railroad card.
    // 7. Advance to the nearest utility. Ditto.
    // 8. Bank gives you $50.
    // 9. GTFO Jail
    // 10. Go back three spaces
    // 11. Go to Jail
    // 12. Pay $25 / house and $100 / hotel.
    // 13. Speeding fine $15
    // 14. Advance to Reading Railroad
    // 15. Chairman of the board, pay each player $50.
    // 16. Building loan matures. Collect $150.