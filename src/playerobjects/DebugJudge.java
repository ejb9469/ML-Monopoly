package playerobjects;

import gameobjects.*;

import java.util.Set;

public class DebugJudge implements Judge {

    private final InPipe input;

    public DebugJudge(InPipe input) {
        this.input = input;
    }

    @Override
    public ActionState decide(Set<GameAction> possibleActions, OutPipe outPipe, GameState gameState, boolean canEndTurn) {

        // TODO: Delegate (most of) this functionality to `ScannerInPipe`

        outPipe.output("Possible actions: \n" + generateActionsSet(possibleActions));
        // All below try-catch blocks are to avoid stupid unchecked exceptions killing play tests

        GameAction action;
        try {
            action = GameAction.valueOf(input.queryString().strip().toUpperCase());
        } catch (IllegalArgumentException ex) {
            outPipe.output("Invalid game action, defaulting to MOVE_THROW_DICE.");
            action = GameAction.MOVE_THROW_DICE;
        }

        GameObject wrapper = new GameObject();

        if (action != GameAction.TRADE_OFFER) {

            System.out.print("objInt: ");
            try {
                wrapper.objInt = Integer.parseInt(input.queryString().strip());
            } catch (NumberFormatException ex) {
                outPipe.output("Invalid int, defaulting to -1.");
                wrapper.objInt = -1;
            }

            System.out.print("objBool: ");
            wrapper.objBool = Boolean.parseBoolean(input.queryString().strip().toLowerCase());

            System.out.print("objProperty: ");
            String objPropertyStr = input.queryString().strip().toLowerCase();
            boolean validProperty = false;
            for (Property property : Board.SQUARES) {
                if (objPropertyStr.equals(property.getName().toLowerCase())) {
                    wrapper.objProperty = property;
                    validProperty = true;
                    break;
                }
            }
            if (!validProperty) {
                outPipe.output("Invalid Property name, defaulting to GO.");
                wrapper.objProperty = Board.SQUARES.get(0);  // GO
            }

            wrapper.objTrade = null;

        } else {

            System.out.print("objTradeStr: ");
            String objTradeStr = input.queryString().strip().toLowerCase();

            Trade objTrade;
            try {
                objTrade = Trade.parseTradeString(objTradeStr);
            } catch (Exception ex) {
                System.err.println("Parsing error, skipping...\nStack trace below:");
                ex.printStackTrace();
                objTrade = null;
            }
            wrapper.objTrade = objTrade;

            wrapper.objInt = -1;
            wrapper.objBool = false;
            wrapper.objProperty = null;  // This might cause issues, but ok for now || 08-04-23

        }

        return new ActionState(action, wrapper);

    }

    public static String generateActionsSet(Set<GameAction> actions) {
        StringBuilder out = new StringBuilder();
        for (GameAction action : actions)
            out.append(action.name()).append("\n");
        out.deleteCharAt(out.length()-1);
        return out.toString();
    }

}