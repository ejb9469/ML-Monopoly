package client;

import server.*;

import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Set;

public class DebugDecider implements Decider {

    private static final Scanner sc = new Scanner(System.in);

    @Override
    public LinkedHashMap<GameAction, GameObject> decide(Set<GameAction> possibleActions, GameState gameState, Player playerObject, boolean canEndTurn) {
        System.out.println("Possible actions: \n" + printActionsSet(possibleActions));
        //Scanner sc = new Scanner(System.in);
        // All below try-catch blocks are to avoid stupid unchecked exceptions killing play tests
        GameAction action;
        try {
            action = GameAction.valueOf(sc.nextLine().strip().toUpperCase());
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid game action, defaulting to MOVE_THROW_DICE.");
            action = GameAction.MOVE_THROW_DICE;
        }
        GameObject wrapper = new GameObject();
        System.out.print("objInt: ");
        try {
            wrapper.objInt = Integer.parseInt(sc.nextLine().strip());
        } catch (NumberFormatException ex) {
            System.out.println("Invalid int, defaulting to -1.");
            wrapper.objInt = -1;
        }
        System.out.print("objBool: ");
        wrapper.objBool = Boolean.parseBoolean(sc.nextLine().strip().toLowerCase());
        // Skip objPlayer b/c it's only used in trades
        System.out.print("objProperty: ");
        String objPropertyStr = sc.nextLine().strip().toLowerCase();
        boolean validProperty = false;
        for (Property property : Board.SQUARES) {
            if (objPropertyStr.equals(property.getName().toLowerCase())) {
                wrapper.objProperty = property;
                validProperty = true;
                break;
            }
        }
        if (!validProperty) {
            System.out.println("Invalid Property name, defaulting to GO.");
            wrapper.objProperty = Board.SQUARES.get(0);  // GO
        }
        LinkedHashMap<GameAction, GameObject> out = new LinkedHashMap<>();
        out.put(action, wrapper);
        //sc.close();
        return out;
    }

    public static String printActionsSet(Set<GameAction> actions) {
        StringBuilder out = new StringBuilder();
        for (GameAction action : actions)
            out.append(action.name()).append("\n");
        out.deleteCharAt(out.length()-1);
        return out.toString();
    }

}