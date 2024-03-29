package gameobjects;

/**
 * Enum class representing all possible Player actions in a standard game of Monopoly.
 * Includes groups of actions used in frequently-occurring contexts.
 */
public enum GameAction {

    MOVE_THROW_DICE,
    TRADE_OFFER,
    TRADE_RESPOND,
    PROPERTY_BUY_OR_AUCTION,
    PROPERTY_MORTGAGE,
    PROPERTY_UNMORTGAGE,
    AUCTION_BID,
    HOUSE_BUILD,
    HOUSE_SELL,
    JAIL_THROW_DICE,
    JAIL_PAY_BAIL,
    JAIL_USE_CARD,
    END_TURN,
    DECLARE_BANKRUPTCY;

    public static final GameAction[] START_ACTIONS = new GameAction[]{MOVE_THROW_DICE, TRADE_OFFER, PROPERTY_MORTGAGE, PROPERTY_UNMORTGAGE, HOUSE_BUILD, HOUSE_SELL};
    public static final GameAction[] END_ACTIONS = new GameAction[]{TRADE_OFFER, PROPERTY_MORTGAGE, PROPERTY_UNMORTGAGE, HOUSE_BUILD, HOUSE_SELL, END_TURN};
    public static final GameAction[] JAIL_ACTIONS = new GameAction[]{JAIL_THROW_DICE, JAIL_PAY_BAIL, JAIL_USE_CARD};
    public static final GameAction[] SELL_ACTIONS = new GameAction[]{TRADE_OFFER, TRADE_RESPOND, PROPERTY_MORTGAGE, HOUSE_SELL, DECLARE_BANKRUPTCY};

}