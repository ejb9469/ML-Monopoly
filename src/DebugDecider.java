import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Set;

public class DebugDecider implements Decider {

    private static Scanner sc = new Scanner(System.in);

    @Override
    public LinkedHashMap<GameAction, GameObject> decide(Set<GameAction> possibleActions, GameState gameState, Player playerObject, boolean canEndTurn) {
        //System.out.println("Possible actions: \n" + printActionsSet(possibleActions));
        //Scanner sc = new Scanner(System.in);
        GameAction action = GameAction.valueOf(sc.nextLine().strip());
        GameObject wrapper = new GameObject();
        System.out.print("objInt: ");
        wrapper.objInt = Integer.parseInt(sc.nextLine().strip());
        System.out.print("objBool: ");
        wrapper.objBool = Boolean.parseBoolean(sc.nextLine().strip().toLowerCase());
        // Skip objPlayer b/c it's only used in trades
        System.out.print("objProperty: ");
        String objPropertyStr = sc.nextLine().strip().toLowerCase();
        for (Property property : ((DebugPipe)(playerObject.communicator)).server.board.getSquares()) {
            if (objPropertyStr.equals(property.getName().toLowerCase())) {
                wrapper.objProperty = property;
                break;
            }
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