package server;

import java.security.SecureRandom;


public class Dice {

    public static final boolean PRINTS_ROLL = true;

    private final SecureRandom random;

    public boolean doubles = false;
    public int r1, r2;

    public Dice() {
        random = new SecureRandom();
    }

    public int toss() {
        r1 = random.nextInt(1, 7);
        r2 = random.nextInt(1, 7);
        if (PRINTS_ROLL)
            System.out.println("" + r1 + " + " + r2 + " = " + (r1+r2));
        doubles = (r1 == r2);
        return (r1+r2);
    }

    public int result() {
        return r1 + r2;
    }

}