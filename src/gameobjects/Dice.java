package gameobjects;

import java.security.SecureRandom;

/**
 * Class representing two Monopoly dice.
 * Doubles are tracked.
 * Cannot be 'tossed' twice.
 */
public class Dice {

    public static final boolean PRINTS_ROLL = true;

    private final SecureRandom random;

    public boolean doubles = false;
    public int r1, r2;

    public Dice() {
        random = new SecureRandom();
    }

    /**
     * Throw two 6-sided dice, update the doubles field accordingly, and return the sum.
     * Prints to stdout.
     * @return Sum of two thrown 6-sided dice.
     */
    public int toss() {
        r1 = random.nextInt(1, 7);
        r2 = random.nextInt(1, 7);
        if (PRINTS_ROLL)
            System.out.println("" + r1 + " + " + r2 + " = " + (r1+r2));
        doubles = (r1 == r2);
        return this.result();
    }

    /**
     * Returns the sum of the previous / current toss.
     */
    public int result() {
        return r1 + r2;
    }

}