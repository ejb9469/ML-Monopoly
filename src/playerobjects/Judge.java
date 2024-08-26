package playerobjects;

import gameobjects.GameAction;
import gameobjects.GameObject;
import gameobjects.GameState;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Defines a class of classes that request and parse user input to produce a list of commands for a Player to request, given a GameState context.
 */
public interface Judge {

    /**
     * Request and parse user input to produce a HashMap of commands for a Player to request, given a GameState context.
     * @param possibleActions Set of allowed actions
     * @param gameState GameState context
     * @param outPipe OutputPipe (output mechanism)
     * @param canEndTurn Allowed to end turn
     * @return HashMap of GameActions and their linked GameObjects
     */
    LinkedHashMap<GameAction, GameObject> decide(Set<GameAction> possibleActions, OutPipe outPipe, GameState gameState, boolean canEndTurn);

}