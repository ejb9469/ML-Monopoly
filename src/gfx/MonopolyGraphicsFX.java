package gfx;

import javafx.collections.ObservableList;
import javafx.concurrent.*;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import server.Game;
import server.GameState;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class MonopolyGraphicsFX extends Application {

    public static final int WINDOW_DIM = 1000;

    public static final Color CHANCE_COLOR = Color.rgb(80, 150, 150);
    public static final Color COMMUNITY_COLOR = Color.rgb(200, 200, 120);

    public static final Color[][] PROPERTY_COLOR_MAP = new Color[][] {
            {Color.LIGHTGRAY, Color.RED, CHANCE_COLOR, Color.RED, Color.RED, Color.BLACK, Color.YELLOW, Color.YELLOW, Color.PINK, Color.YELLOW, Color.LIGHTGRAY},
            {Color.ORANGE, Color.GREEN},
            {Color.ORANGE, Color.GREEN},
            {COMMUNITY_COLOR, COMMUNITY_COLOR},
            {Color.ORANGE, Color.GREEN},
            {Color.BLACK, Color.BLACK},
            {Color.MAGENTA, CHANCE_COLOR},
            {Color.MAGENTA, Color.BLUE},
            {Color.PINK, Color.LIGHTGRAY},
            {Color.MAGENTA, Color.BLUE},
            {Color.LIGHTGRAY, Color.CYAN, Color.CYAN, CHANCE_COLOR, Color.CYAN, Color.BLACK, Color.LIGHTGRAY, Color.rgb(139, 69, 19), COMMUNITY_COLOR, Color.rgb(139, 69, 19), Color.LIGHTGRAY}
    };

    public static final String[][] PROPERTY_NAME_MAP = new String[][] {
            {"Free Parking", "Kentucky Avenue", "CHANCE", "Indiana Avenue", "Illinois Avenue", "B & O Railroad", "Atlantic Avenue", "Ventnor Avenue", "Water Works", "Marvin Gardens", "GO TO JAIL"},
            {"New York Avenue", "Pacific Avenue"},
            {"Tennessee Avenue", "North Carolina Avenue"},
            {"COMMUNITY CHEST", "COMMUNITY CHEST"},
            {"St. James Place", "Pennsylvania Avenue"},
            {"Pennsylvania Railroad", "Short Line"},
            {"Virginia Avenue", "CHANCE"},
            {"States Avenue", "Park Place"},
            {"Electric Company", "Luxury Tax"},
            {"St. Charles Place", "Boardwalk"},
            {"JAIL", "Connecticut Avenue", "Vermont Avenue", "CHANCE", "Oriental Avenue", "Reading Railroad", "Income Tax", "Baltic Avenue", "COMMUNITY CHEST", "Mediterranean Avenue", "GO"}
    };

    private final Game game = null;  // TODO: Un-nullify
    private static Group currentGUI = null;

    public static void main(String[] args) {
        //new MonopolyGraphicsFX(null).run(args);
        run(args);
    }

    public static void run(String[] args) {
        currentGUI = new MonopolyGroup();
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle("ML-Monopoly GUI");
        stage.setScene(generateScene(this.game));
        stage.show();

        /*Thread taskThread = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });

        taskThread.start();*/  // TODO: Implement this once animation is necessary

    }

    private Scene generateScene(Game game) throws FileNotFoundException {
        Scene scene = new Scene(new MonopolyGroup(), WINDOW_DIM, WINDOW_DIM);
        scene.setFill(new ImagePattern(new Image(new FileInputStream("src/gfx/monopoly.jpg"))));
        //generateGroupFromGame(game);
        return scene;
    }

    private Group generateGroupFromGame(Game game) {

        if (currentGUI == null)
            currentGUI = new MonopolyGroup();
        Group group = currentGUI;

        //GameState gameState = game.getGameState();
        int i = 0;
        for (Node node : group.getChildren()) {
            if (!(node instanceof Rectangle)) continue;
            Rectangle rect = (Rectangle) node;
            System.out.println("Pass #" + ++i + ": (" + rect.getX() + ", " + rect.getY() + ")");
        }

        currentGUI = group;
        return group;

    }

}