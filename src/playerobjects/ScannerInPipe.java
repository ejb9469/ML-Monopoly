package playerobjects;

import gameobjects.ActionState;

import java.util.Scanner;

public class ScannerInPipe implements InPipe {

    private final Scanner scanner = new Scanner(System.in);

    public ActionState query() {
        return null;  // Bad polymorphism, but query() is useless in ScannerInPipe
        //return new ActionState(GameAction.valueOf(scanner.nextLine()), new GameObject());
    }

    public String queryString() {
        return scanner.nextLine();
    }

    public void close() {
        scanner.close();
    }

}