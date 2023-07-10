package server;

import java.security.SecureRandom;


public class Dice {

    public static final boolean PRINTS_ROLL = true;

    private final SecureRandom random;

    public boolean doubles = false;

    public Dice() {
        random = new SecureRandom();
    }

    public int toss() {
        int r1 = random.nextInt(1, 7);
        int r2 = random.nextInt(1, 7);
        if (PRINTS_ROLL)
            System.out.println("" + r1 + " + " + r2 + " = " + (r1+r2));
        doubles = (r1 == r2);
        return (r1+r2);
    }

}