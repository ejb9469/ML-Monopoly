package gfx;

import gameobjects.ActionState;
import gameobjects.GameAction;
import gameobjects.GameState;
import playerobjects.InPipe;
import playerobjects.Judge;
import playerobjects.OutPipe;

import java.util.Set;

import static playerobjects.DebugJudge.generateActionsSet;

public class GFXJudge implements Judge {

    private final InPipe input = new GFXInPipe(MonopolyGraphicsFX.actionState);

    @Override
    public ActionState decide(Set<GameAction> possibleActions, OutPipe outPipe, GameState gameState, boolean canEndTurn) {

        // Display possible actions
        outPipe.output("Possible actions: \n" + generateActionsSet(possibleActions));

        return input.query();

    }

}