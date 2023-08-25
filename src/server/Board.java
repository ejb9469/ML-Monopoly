package server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Board {

    public static final List<Property> SQUARES = Arrays.asList(
            new Property("GO", COLOR_SET.FUNCTION),
            new Property("Mediterranean Avenue", COLOR_SET.BROWN, 60, .5, 2, 50, .5, new int[]{10, 30, 90, 160, 250}),
            new Property("Community Chest", COLOR_SET.FUNCTION),
            new Property("Baltic Avenue", COLOR_SET.BROWN, 60, .5, 4, 50, .5, new int[]{20, 60, 180, 320, 450}),
            new Property("Income Tax", COLOR_SET.FUNCTION),
            new Property("Reading Railroad", COLOR_SET.RAILROAD, 200, .5, 50),
            new Property("Oriental Avenue", COLOR_SET.LIGHT_BLUE, 100, .5, 6, 50, .5, new int[]{30, 90, 270, 400, 550}),
            new Property("Chance", COLOR_SET.FUNCTION),
            new Property("Vermont Avenue", COLOR_SET.LIGHT_BLUE, 100, .5, 6, 50, .5, new int[]{30, 90, 270, 400, 550}),
            new Property("Connecticut Avenue", COLOR_SET.LIGHT_BLUE, 120, .5, 8, 50, .5, new int[]{40, 100, 300, 450, 600}),
            new Property("Jail", COLOR_SET.FUNCTION),
            new Property("St. Charles Place", COLOR_SET.PURPLE, 140, .5, 10, 100, .5, new int[]{50, 150, 450, 625, 750}),
            new Property("Electric Company", COLOR_SET.UTILITY, 150, .5),
            new Property("States Avenue", COLOR_SET.PURPLE, 140, .5, 10, 100, .5, new int[]{50, 150, 450, 625, 750}),
            new Property("Virginia Avenue", COLOR_SET.PURPLE, 160, .5, 12, 100, .5, new int[]{60, 180, 500, 700, 900}),
            new Property("Pennsylvania Railroad", COLOR_SET.RAILROAD, 200, .5, 50),
            new Property("St. James Place", COLOR_SET.ORANGE, 180, .5, 14, 100, .5, new int[]{70, 200, 550, 750, 950}),
            new Property("Community Chest", COLOR_SET.FUNCTION),
            new Property("Tennessee Avenue", COLOR_SET.ORANGE, 180, .5, 14, 100, .5, new int[]{70, 200, 550, 750, 950}),
            new Property("New York Avenue", COLOR_SET.ORANGE, 200, .5, 16, 100, .5, new int[]{80, 220, 600, 800, 1000}),
            new Property("Free Parking", COLOR_SET.FUNCTION),
            new Property("Kentucky Avenue", COLOR_SET.RED, 220, .5, 18, 150, .5, new int[]{90, 250, 700, 875, 1050}),
            new Property("Chance", COLOR_SET.FUNCTION),
            new Property("Indiana Avenue", COLOR_SET.RED, 220, .5, 18, 150, .5, new int[]{90, 250, 700, 875, 1050}),
            new Property("Illinois Avenue", COLOR_SET.RED, 240, .5, 20, 150, .5, new int[]{100, 300, 750, 925, 1100}),
            new Property("B & O Railroad", COLOR_SET.RAILROAD, 200, .5, 50),
            new Property("Atlantic Avenue", COLOR_SET.YELLOW, 260, .5, 22, 150, .5, new int[]{110, 330, 800, 975, 1150}),
            new Property("Ventnor Avenue", COLOR_SET.YELLOW, 260, .5, 22, 150, .5, new int[]{110, 330, 800, 975, 1150}),
            new Property("Water Works", COLOR_SET.UTILITY, 150, .5),
            new Property("Marvin Gardens", COLOR_SET.YELLOW, 280, .5, 24, 150, .5, new int[]{120, 360, 850, 1025, 1200}),
            new Property("Go To Jail", COLOR_SET.FUNCTION),
            new Property("Pacific Avenue", COLOR_SET.GREEN, 300, .5, 26, 200, .5, new int[]{130, 390, 900, 1100, 1275}),
            new Property("North Carolina Avenue", COLOR_SET.GREEN, 300, .5, 26, 200, .5, new int[]{130, 390, 900, 1100, 1275}),
            new Property("Community Chest", COLOR_SET.FUNCTION),
            new Property("Pennsylvania Avenue", COLOR_SET.GREEN, 320, .5, 28, 200, .5, new int[]{150, 450, 1000, 1200, 1400}),
            new Property("Short Line", COLOR_SET.RAILROAD, 200, .5, 50),
            new Property("Chance", COLOR_SET.FUNCTION),
            new Property("Park Place", COLOR_SET.DARK_BLUE, 350, .5, 35, 200, .5, new int[]{175, 500, 1100, 1300, 1500}),
            new Property("Luxury Tax", COLOR_SET.FUNCTION),
            new Property("Boardwalk", COLOR_SET.DARK_BLUE, 400, .5, 50, 200, .5, new int[]{200, 600, 1400, 1700, 2000})
    );

    public static int indexOf(String propertyName) {

        for (int i = 0; i < SQUARES.size(); i++) {
            if (SQUARES.get(i).getName().equalsIgnoreCase(propertyName))
                return i;
        }
        return -1;

    }

    public static List<Property> getSquaresOfColorSet(COLOR_SET colorSet) {

        List<Property> squares = new ArrayList<>();

        for (Property property : SQUARES) {
            if (property.color == colorSet)
                squares.add(property);
        }

        return squares;

    }

    public static List<Integer> getIndexesOfColorSet(COLOR_SET colorSet) {

        List<Integer> indexes = new ArrayList<>();

        for (Property property : SQUARES) {
            if (property.color == colorSet)
                indexes.add(SQUARES.indexOf(property));
        }

        return indexes;

    }

}