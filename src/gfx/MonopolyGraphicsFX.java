package gfx;

import javafx.concurrent.*;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import server.Game;

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

    private final Game game = null;  // TODO: Un-nullify

    public static void main(String[] args) throws Exception {
        //new MonopolyGraphicsFX(null).run(args);
        run(args);
    }

    /*public MonopolyGraphicsFX(Game game) {
        this.game = game;
    }*/

    public static void run(String[] args) {
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
        Scene scene = new Scene(generateGroupFromGame(game), WINDOW_DIM, WINDOW_DIM);
        scene.setFill(new ImagePattern(new Image(new FileInputStream("src/gfx/monopoly.jpg"))));
        return scene;
    }

    private Group generateGroupFromGame(Game game) {

        Group group = new Group();

        int propertySize = WINDOW_DIM/13;
        int propertyGap = propertySize/13;

        for (int i = 0; i < 11; i++) {

            // If first or last, run the whole row pass, do the rims otherwise
            if (i % 10 == 0) {
                for (int j = 0; j < 11; j++) {
                    Rectangle rect = new Rectangle();
                    rect.setHeight(propertySize);
                    rect.setWidth(propertySize);
                    int x = (propertySize * j) + (propertyGap * j) + (propertySize / 2) + (propertyGap * 3);
                    rect.setX(x);
                    int y;
                    if (i == 0)
                        y = (propertySize / 2) + (propertyGap * 3);
                    else
                        y = (propertySize * i) + (propertyGap * i) + ((propertySize / 2) + (propertyGap * 3));
                    rect.setY(y);
                    rect.setFill(PROPERTY_COLOR_MAP[i][j]);
                    Text text = new Text(MonopolySwing.PROPERTY_NAME_MAP[i][j]);
                    text.setX(x);
                    if (i == 0)
                        text.setY(y - (propertySize / (2*3)));
                    else
                        text.setY(y + (propertySize / 2 * 2.5f));
                    text.setFont(new Font("Arial Bold", 10));
                    text.setWrappingWidth(propertySize);
                    text.setTextAlignment(TextAlignment.CENTER);
                    group.getChildren().addAll(rect, text);
                }
            } else {
                for (int j = 0; j < 2; j++) {
                    Rectangle rect = new Rectangle();
                    rect.setHeight(propertySize);
                    rect.setWidth(propertySize);
                    int x;
                    if (j == 0)
                        x = (propertySize / 2) + (propertyGap * 3);
                    else
                        x = (propertySize * 10) + (propertyGap * 10) + (propertySize / 2) + (propertyGap * 3);
                    rect.setX(x);
                    int y = (propertySize * i) + (propertyGap * i) + (propertySize / 2) + (propertyGap * 3);
                    rect.setY(y);
                    rect.setFill(PROPERTY_COLOR_MAP[i][j]);
                    Text text = new Text(MonopolySwing.PROPERTY_NAME_MAP[i][j]);
                    if (j == 0)
                        text.setX(x + (propertySize));
                    else
                        text.setX(x - (propertySize));
                    text.setY(y + (propertySize / 2));
                    text.setFont(new Font("Arial Bold", 10));
                    text.setWrappingWidth(propertySize);
                    text.setTextAlignment(TextAlignment.CENTER);
                    group.getChildren().addAll(rect, text);
                }
            }
        }

        return group;

    }

}