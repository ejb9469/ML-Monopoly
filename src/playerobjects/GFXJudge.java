package playerobjects;

import gameobjects.ActionState;
import gameobjects.GameAction;
import gameobjects.GameObject;
import gameobjects.GameState;
import gfx.MonopolyGraphicsFX;

import java.util.LinkedHashMap;
import java.util.Set;

import static playerobjects.DebugJudge.generateActionsSet;

public class GFXJudge implements Judge {

    private final InPipe input = new GFXInPipe(MonopolyGraphicsFX.actionState);

    @Override
    public LinkedHashMap<GameAction, GameObject> decide(Set<GameAction> possibleActions, OutPipe outPipe, GameState gameState, boolean canEndTurn) {

        // Display possible actions
        outPipe.output("Possible actions: \n" + generateActionsSet(possibleActions));

        LinkedHashMap<GameAction, GameObject> out = new LinkedHashMap<>();
        ActionState actionState = input.query();
        out.put(actionState.selectedAction, actionState.selectedContext);

        return out;

    }

}