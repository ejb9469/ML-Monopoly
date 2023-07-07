package gfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.stage.Stage;
import server.Game;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;

public class MonopolyGraphicsFX extends Application {

    public static final int FRAMES_PER_SECOND = 60;

    public static final int WINDOW_DIM = 1000;

    public static final Color CHANCE_COLOR = Color.rgb(80, 150, 150);
    public static final Color COMMUNITY_COLOR = Color.rgb(200, 200, 120);
    public static final Color BACKGROUND_COLOR = Color.rgb(30, 30, 60, .9f);
    public static final Color OVERLAY_COLOR = Color.rgb(150, 150, 150, .4f);
    public static final Color CARD_COLOR = Color.DARKORANGE;

    public static final Color[] PLAYER_SLOT_COLOR_MAP = new Color[] {
            Color.DEEPSKYBLUE,
            Color.RED,
            Color.LAWNGREEN,
            Color.YELLOW,
            Color.PURPLE,
            Color.ORANGE
    };

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

    public static final int[] LOGIC_TO_GFX_PROP_INDEX_MAP = new int[] {
            39,  // GO
            38,  // Med. Ave
            37,
            36,
            35,
            34,  // Reading Railroad
            33,
            32,
            31,
            30,
            29,  // JAIL
            27,  // St. Charles Place
            25,
            23,
            21,  // Virginia Ave
            19,
            17,
            15,
            13,
            11,  // New York Ave
            0,  // Free Parking
            1,
            2,
            3,
            4,
            5,
            6,
            7,
            8,
            9,
            10,  // Go to Jail
            12,
            14,
            16,
            18,
            20,  // Short Line
            22,
            24,
            26,
            28  // Boardwalk
    };

    private final Game game = new Game(4, new String[]{"Car", "Thimble", "Ship", "Dog"}, null);  // TODO: Temporary!

    public static void main(String[] args) {
        //new MonopolyGraphicsFX(null).run(args);
        run(args);
    }

    public static void run(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        stage.setTitle("ML-Monopoly GUI");
        stage.setResizable(false);
        stage.setScene(generateScene());
        stage.show();


        // Animation \\
        Thread taskThread = new Thread(game::gameLoop);
        taskThread.start();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    stage.setScene(generateScene());
                    //stage.show();
                });
            }
        }, 0, 1000/FRAMES_PER_SECOND);

    }

    private Scene generateScene() {
        Scene scene = new Scene(new MonopolyGroup(game.getGameState()), WINDOW_DIM, WINDOW_DIM);
        try {
            scene.setFill(new ImagePattern(new Image(new FileInputStream("src/gfx/monopoly.jpg"))));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        //generateGroupFromGame(game);
        return scene;
    }

}