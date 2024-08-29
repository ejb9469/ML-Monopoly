package playerobjects;

import gameobjects.ActionState;
import gameobjects.GameAction;
import gameobjects.GameState;

import java.util.Set;

/**
 * Defines a class of classes that request and parse user input to produce an ActionState of commands for a Player to request, given a GameState context.
 */
public interface Judge {

    /**
     * Request and parse user input to produce an ActionState of commands for a Player to request, given a GameState context.
     * @param possibleActions Set of allowed actions
     * @param gameState GameState context
     * @param outPipe OutputPipe (output mechanism)
     * @param canEndTurn Allowed to end turn
     * @return HashMap of GameActions and their linked GameObjects
     */
    ActionState decide(Set<GameAction> possibleActions, OutPipe outPipe, GameState gameState, boolean canEndTurn);

}