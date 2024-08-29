package gfx;

import gameobjects.ActionState;
import playerobjects.InPipe;
import playerobjects.ScannerInPipe;

public class GFXInPipe implements InPipe {

    public ActionState lastActionState;

    public GFXInPipe(ActionState lastActionState) {
        this.lastActionState = lastActionState;
    }

    public ActionState query() {
        return waitForGameAction();
    }

    public String queryString() {
        return new ScannerInPipe().queryString();  // Bad polymorphism, but queryString() is useless for GFXInPipe
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