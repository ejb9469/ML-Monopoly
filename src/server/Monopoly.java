package server;

import gfx.MonopolyGraphicsFX;

/**
 * The top-level class of the program.
 */
public class Monopoly {

    public static final int MAX_PLAYERS = 4;
    public static final int MAX_TURNS_IN_JAIL = 3;

    public static void main(String[] args) {

        // Graphics module
        MonopolyGraphicsFX.main(args);

    }

}