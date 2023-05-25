import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Defines a class of classes which decide upon an action given a Game State context.
 */
public interface Decider {
    LinkedHashMap<GameAction, GameObject> decide(Set<GameAction> possibleActions, GameState gameState, Player playerObject, boolean canEndTurn);
}