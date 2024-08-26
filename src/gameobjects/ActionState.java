package gameobjects;

import java.util.HashSet;
import java.util.Set;

public class ActionState {

    public GameAction selectedAction;
    public GameObject selectedContext;
    public Set<GameAction> possibleActions;

    public ActionState(ActionState clone) {
        this();
        if (clone == null)
            return;
        this.selectedAction = clone.selectedAction;
        this.possibleActions = clone.possibleActions;
        this.selectedContext = clone.selectedContext;
    }

    public ActionState(GameAction selectedAction, GameObject selectedContext) {
        this();
        this.selectedAction = selectedAction;
        this.selectedContext = selectedContext;
    }

    public ActionState() {
        this.selectedAction = null;
        this.selectedContext = null;
        this.possibleActions = new HashSet<>();
    }

}