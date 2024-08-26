package playerobjects;

import gameobjects.ActionState;
import gfx.MonopolyGraphicsFX;

public class GFXInPipe implements InPipe {

    public ActionState lastActionState;

    public GFXInPipe(ActionState lastActionState) {
        this.lastActionState = lastActionState;
    }

    public ActionState query() {
        return waitForGameAction();
    }

    public String queryString() {
        return null;  // Bad polymorphism, but queryString() is useless in GFXInPipe
    }

    public void close() {}

    private ActionState waitForGameAction() {

        try {
            MonopolyGraphicsFX.actionState = null;
            while (true) {
                Thread.sleep(1);
                if (MonopolyGraphicsFX.actionState == null) continue;
                if (this.lastActionState == MonopolyGraphicsFX.actionState) continue;
                this.lastActionState = MonopolyGraphicsFX.actionState;
                break;
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        return MonopolyGraphicsFX.actionState;

    }

}