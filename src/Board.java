import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Board {

    public static final List<Property> SQUARES = Arrays.asList(
            new Property("GO", COLOR_SET.FUNCTION),
            new Property("Mediterranean Avenue", COLOR_SET.BROWN),
            new Property("Community Chest", COLOR_SET.FUNCTION),
            new Property("Baltic Avenue", COLOR_SET.BROWN),
            new Property("Income Tax", COLOR_SET.FUNCTION),
            new Property("Reading Railroad", COLOR_SET.RAILROAD),
            new Property("Oriental Avenue", COLOR_SET.LIGHT_BLUE),
            new Property("Chance", COLOR_SET.FUNCTION),
            new Property("Vermont Avenue", COLOR_SET.LIGHT_BLUE),
            new Property("Connecticut Avenue", COLOR_SET.LIGHT_BLUE),
            new Property("Jail", COLOR_SET.FUNCTION),
            new Property("St. Charles Place", COLOR_SET.PURPLE),
            new Property("Electric Company", COLOR_SET.UTILITY),
            new Property("States Avenue", COLOR_SET.PURPLE),
            new Property("Virginia Avenue", COLOR_SET.PURPLE),
            new Property("Pennsylvania Railroad", COLOR_SET.RAILROAD),
            new Property("St. James Place", COLOR_SET.ORANGE),
            new Property("Community Chest", COLOR_SET.FUNCTION),
            new Property("Tennessee Avenue", COLOR_SET.ORANGE),
            new Property("New York Avenue", COLOR_SET.ORANGE),
            new Property("Free Parking", COLOR_SET.FUNCTION),
            new Property("Kentucky Avenue", COLOR_SET.RED),
            new Property("Chance", COLOR_SET.FUNCTION),
            new Property("Indiana Avenue", COLOR_SET.RED),
            new Property("Illinois Avenue", COLOR_SET.RED),
            new Property("B & O Railroad", COLOR_SET.RAILROAD),
            new Property("Atlantic Avenue", COLOR_SET.YELLOW),
            new Property("Ventnor Avenue", COLOR_SET.YELLOW),
            new Property("Water Works", COLOR_SET.UTILITY),
            new Property("Marvin Gardens", COLOR_SET.YELLOW),
            new Property("Go To Jail", COLOR_SET.FUNCTION),
            new Property("Pacific Avenue", COLOR_SET.GREEN),
            new Property("North Carolina Avenue", COLOR_SET.GREEN),
            new Property("Community Chest", COLOR_SET.FUNCTION),
            new Property("Pennsylvania Avenue", COLOR_SET.GREEN),
            new Property("Short Line", COLOR_SET.RAILROAD),
            new Property("Chance", COLOR_SET.FUNCTION),
            new Property("Park Place", COLOR_SET.DARK_BLUE),
            new Property("Luxury Tax", COLOR_SET.FUNCTION),
            new Property("Boardwalk", COLOR_SET.DARK_BLUE)
    );

    private List<Property> squares = new ArrayList<>(SQUARES);  // TODO

    protected Game parentGame;

    public Board(Game parentGame) {
        this.parentGame = parentGame;
    }

    public Game getParentGame() {
        return parentGame;
    }

    public List<Property> getSquares() {
        return squares;
    }

    public int indexOf(String propertyName) {
        for (int i = 0; i < squares.size(); i++) {
            if (squares.get(i).getName().equals(propertyName))
                return i;
        }
        return -1;
    }

}