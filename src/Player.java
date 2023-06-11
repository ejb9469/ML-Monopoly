// Player objects should have the following properties:
    // 1) `name` :: Name of the player
    // 2) `id` :: Player ID logically equivalent to `turn` counter in Board
    // 3) `parentBoard` :: Parent Board object

// Player objects should have the following functionality:
    // 1) `throwMoveDice()` :: Request Dice throw for movement
            // CAN ONLY MOVE ONCE PER TURN!!
    // 3) `offerTrade(player, terms)` :: Request to offer a trade to a Player with particular Trade terms
    // 4) `buy(property, auction)` :: Request to buy a Property, or begin an auction
    // 5) `mortgage(property)` :: Request to mortgage a Property
    // 6) `unmortgage(property)`
    // 7) `bid(amount)` :: Request to bid during an auction
    // 8) `buildHouse(property)` :: Request to build a house on a Property
    // 9) `sellHouse(property)`
    // 10) `performJailAction(action)` :: Request to perform a given jail-related GameAction.
            // Jail-related actions are: throw dice, pay bail, or use card.


import java.util.*;

public class Player implements OutputsWarnings {

    private static int ID_INCREMENT = 0;

    private String name;
    private int id = ID_INCREMENT++;

    private final UUID uuid;

    public Communicator communicator;  // TODO: Temporarily public!
    private final Decider decider = new DebugDecider();  // TODO
    private final OutPipe output = new DebugOutPipe();

    public Player(Communicator communicator, String name, UUID uuid) {
        this.communicator = communicator;
        this.name = name;
        this.uuid = uuid;
    }

    /**
     * Entry method for the Game object to signal a Player object to take its turn.
     * @param legalActions Set of GameActions considered legal by the caller.
     * @param uuid Player's UUID key, supplied here to ensure only the Game object can apply the signal.
     * @param prompt Prompt displayed to the user.
     */
    public void signalTurn(Set<GameAction> legalActions, UUID uuid, PromptString prompt) {

        if (prompt == null) prompt.str = "";

        // Reject bad authentication
        if (!uuid.equals(this.uuid)) {
            warn(3);
            return;
        }

        // Sync GameState
        syncState();

        // Output prompt
        output.output(prompt);

        // Black-box decision-making
        // Also, ugly stinky extraction process
        boolean canEndTurn = legalActions.contains(GameAction.END_TURN);
        LinkedHashMap<GameAction, GameObject> actionMap = decider.decide(legalActions, gameStateCopy, this, canEndTurn);
        GameAction decidedAction = actionMap.keySet().stream().findFirst().get();
        GameObject wrapper = actionMap.get(decidedAction);

        // async param use case..?
        takeTurn(new GameAction[]{decidedAction}, new GameObject[]{wrapper}, false);
    }

    /**
     * Request the Game object to perform several actions.
     * <p>
     * As with the private methods, this is called without any input from the Game object,
     * ... but the Game object will usually reject actions taken not on the Player's turn.
     * @param gameActions Array of GameActions to perform.
     * @param wrappers Array of GameObjects (wrappers) containing GameActions' corresponding function parameters.
     * @param async Denotes call on another Player's turn. Used in situations like trade management and inability to pay rent.
     */
    private void takeTurn(GameAction[] gameActions, GameObject[] wrappers, boolean async) {
        //fieldsLocked = true;  // Race conditions?
        if (gameStateCopy.turnIndicator != id && !async)
            warn(1);
        for (int i = 0; i < gameActions.length; i++) {
            GameAction gameAction = gameActions[i];
            GameObject wrapper = wrappers[i];
            switch (gameAction) {
                case MOVE_THROW_DICE -> throwMoveDice();
                case TRADE_OFFER -> offerTrade(wrapper.objPlayer, wrapper.objTrade);
                case TRADE_ACCEPT -> acceptTrade(wrapper.objTrade);
                case TRADE_REJECT -> rejectTrade(wrapper.objTrade);
                case PROPERTY_BUY_OR_AUCTION -> buy(wrapper.objProperty, wrapper.objBool);
                case PROPERTY_MORTGAGE -> mortgage(wrapper.objProperty);
                case PROPERTY_UNMORTGAGE -> unmortgage(wrapper.objProperty);
                case AUCTION_BID -> bid(wrapper.objInt);
                case HOUSE_BUILD -> buildHouse(wrapper.objProperty);
                case HOUSE_SELL -> sellHouse(wrapper.objProperty);
                case JAIL_THROW_DICE -> performJailAction(GameAction.JAIL_THROW_DICE);
                case JAIL_PAY_BAIL -> performJailAction(GameAction.JAIL_PAY_BAIL);
                case JAIL_USE_CARD -> performJailAction(GameAction.JAIL_USE_CARD);
                case END_TURN -> endTurn();
                default -> warn(2);
            }
        }
        syncState();
    }

    ////////////////////////////////////////

    private void throwMoveDice() {
        GameAction action = GameAction.MOVE_THROW_DICE;
        communicator.requestAction(action, uuid, null);
    }

    // TODO: Trading implementation is for later!
    private void offerTrade(Player toPlayer, Trade terms) {

    }
    private void acceptTrade(Trade trade) {

    }
    private void rejectTrade(Trade trade) {

    }
    // offerTrade() is used for counter-offers

    private void buy(Property property, boolean auction) {
        GameAction action = GameAction.PROPERTY_BUY_OR_AUCTION;
        GameObject gameObj = new GameObject();
        gameObj.objProperty = property;
        gameObj.objBool = auction;
        communicator.requestAction(action, uuid, gameObj);
    }

    private void mortgage(Property property) {
        GameAction action = GameAction.PROPERTY_MORTGAGE;
        GameObject gameObj = new GameObject();
        gameObj.objProperty = property;
        communicator.requestAction(action, uuid, gameObj);
    }
    private void unmortgage(Property property) {
        GameAction action = GameAction.PROPERTY_UNMORTGAGE;
        GameObject gameObj = new GameObject();
        gameObj.objProperty = property;
        communicator.requestAction(action, uuid, gameObj);
    }

    // bidding a negative number will cause the player to drop out of the auction
    private void bid(int amount) {
        GameAction action = GameAction.AUCTION_BID;
        GameObject gameObj = new GameObject();
        gameObj.objInt = amount;
        communicator.requestAction(action, uuid, gameObj);
    }

    private void buildHouse(Property property) {
        GameAction action = GameAction.HOUSE_BUILD;
        GameObject gameObj = new GameObject();
        gameObj.objProperty = property;
        communicator.requestAction(action, uuid, gameObj);
    }
    private void sellHouse(Property property) {
        GameAction action = GameAction.HOUSE_BUILD;
        GameObject gameObj = new GameObject();
        gameObj.objProperty = property;
        communicator.requestAction(action, uuid, gameObj);
    }

    private void performJailAction(GameAction action) {
        communicator.requestAction(action, uuid, null);
    }

    private void endTurn() {
        communicator.requestAction(GameAction.END_TURN, uuid, null);
    }

    ////////////////////////////////////////

    // Below state variables should be used as *indicators* on if certain actions are valid.
    // These are client-side variables ONLY; here for optimization.
    // The Game object may still reject any request.
    private GameState gameStateCopy;
    private void syncState() {
        gameStateCopy = communicator.requestCopyOfGameState();
    }


    // Getters
    public String getName() {
        return name;
    }
    public int getId() {
        return id;
    }


    public void output(UUID uuid, String content) {
        if (uuid.equals(this.uuid))
            output.output(content);
    }

    public void warn(int code) {
        System.err.println("Warning called for Player id=" + id + ", CODE " + code + " || " + Calendar.getInstance());
    }

}