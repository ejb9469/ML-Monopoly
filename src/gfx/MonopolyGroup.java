package gfx;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import server.Game;
import server.GameState;

import java.util.*;

import static gfx.MonopolyGraphicsFX.LOGIC_TO_GFX_PROP_INDEX_MAP;
import static gfx.MonopolyGraphicsFX.PLAYER_SLOT_COLOR_MAP;

public class MonopolyGroup extends Group {

    public static final Font TEXT_FONT = new Font("Arial Bold", 11);

    public static final Font MORTGAGE_FONT = new Font("Helvetica", 14);
    public static final String MORTGAGE_TEXT = "MORTG";

    private static final double ADJUSTMENT_CONST = 10f;

    private final List<Rectangle> rectangles = new ArrayList<>();
    private final Map<Rectangle, Rectangle[]> houseRectangles = new HashMap<>();  // Hotel rectangle will be at index 4
    private final Map<Rectangle, Text> propertyTexts = new HashMap<>();
    private final Map<Rectangle, Rectangle> ownershipRectangles = new HashMap<>();
    private final Map<Rectangle, Text> mortgageTexts = new HashMap<>();

    private GameState currentGameState;

    /**
     * Constructs a blank MonopolyGroup (a template).
     */
    public MonopolyGroup() {
        super();
        initialize();
        currentGameState = null;
    }

    /**
     * Constructs a MonopolyGroup template, then uses it to express a Game.
     * @param game Game to conform template to.
     */
    public MonopolyGroup(Game game) {
        this();
        conformToGame(game);
    }

    /**
     * Initializer method for a 'default' GUI template.
     * Adds to .getChildren() and adds to custom List & Map fields.
     */
    private void initialize() {

        double propertySize = MonopolyGraphicsFX.WINDOW_DIM / 13f;
        double propertyGap = propertySize / 13f;

        // Rows
        for (int i = 0; i < 11; i++) {

            // Dealing with top or bottom row
            if (i % 10 == 0) {

                for (int j = 0; j < 11; j++) {

                    Rectangle rect = new Rectangle();
                    rect.setHeight(propertySize); rect.setWidth(propertySize);

                    double x = (propertySize * j) + (propertyGap * j) + (propertySize / 2f) + (propertyGap * 3f);
                    rect.setX(x);

                    double y;
                    if (i == 0)  // Top row
                        y = (propertySize / 2f) + (propertyGap * 3f);
                    else  // Bottom row
                        y = (propertySize * i) + (propertyGap * i) + ((propertySize / 2f) + (propertyGap * 3f));
                    rect.setY(y);

                    rect.setFill(MonopolyGraphicsFX.PROPERTY_COLOR_MAP[i][j]);

                    Text text = new Text(MonopolyGraphicsFX.PROPERTY_NAME_MAP[i][j]);
                    text.setX(x);
                    if (i == 0)  // Top row
                        text.setY(y - (propertySize / 6f) - ADJUSTMENT_CONST);  // Needs a slight adjustment upwards
                    else  // Bottom row
                        text.setY(y + (propertySize / 2f * 2.5f));

                    text.setFont(TEXT_FONT);
                    text.setWrappingWidth(propertySize);
                    text.setTextAlignment(TextAlignment.CENTER);

                    Rectangle[] houseRects = initializeHouses(x, y, propertySize);
                    Rectangle ownershipRect = initializeOwnershipRect(x, y, propertySize);
                    Text mortgageText = initializeMortgageText(x, y, propertySize);

                    rectangles.add(rect);
                    houseRectangles.put(rect, houseRects);
                    propertyTexts.put(rect, text);
                    ownershipRectangles.put(rect, ownershipRect);
                    mortgageTexts.put(rect, mortgageText);

                    this.getChildren().addAll(rect, text, ownershipRect, mortgageText);
                    this.getChildren().addAll(houseRects);

                }

            }
            // Dealing with middle rows (left & right rims)
            else {

                for (int j = 0; j < 2; j++) {

                    Rectangle rect = new Rectangle();
                    rect.setHeight(propertySize); rect.setWidth(propertySize);

                    double x;
                    if (j == 0)  // Left side
                        x = (propertySize / 2f) + (propertyGap * 3f);
                    else  // Right side
                        x = (propertySize * 10f) + (propertyGap * 10f) + (propertySize / 2f) + (propertyGap * 3f);
                    rect.setX(x);

                    double y = (propertySize * i) + (propertyGap * i) + (propertySize / 2f) + (propertyGap * 3f);
                    rect.setY(y);

                    rect.setFill(MonopolyGraphicsFX.PROPERTY_COLOR_MAP[i][j]);

                    Text text = new Text(MonopolyGraphicsFX.PROPERTY_NAME_MAP[i][j]);
                    if (j == 0)  // Left side
                        text.setX(x + (propertySize));
                    else  // Right side
                        text.setX(x - (propertySize));
                    text.setY(y + (propertySize / 2f));

                    text.setFont(TEXT_FONT);
                    text.setWrappingWidth(propertySize);
                    text.setTextAlignment(TextAlignment.CENTER);

                    Rectangle[] houseRects = initializeHouses(x, y, propertySize);
                    Rectangle ownershipRect = initializeOwnershipRect(x, y, propertySize);
                    Text mortgageText = initializeMortgageText(x, y, propertySize);

                    rectangles.add(rect);
                    houseRectangles.put(rect, houseRects);
                    propertyTexts.put(rect, text);
                    ownershipRectangles.put(rect, ownershipRect);
                    mortgageTexts.put(rect, mortgageText);

                    this.getChildren().addAll(rect, text, ownershipRect, mortgageText);
                    this.getChildren().addAll(houseRects);

                }

            }

        }

    }

    private Rectangle[] initializeHouses(double x, double y, double propertySize) {

        Rectangle[] houseRects = new Rectangle[5];
        for (int k = 0; k < 4; k++) {
            Rectangle houseRect = new Rectangle();
            houseRect.setHeight(propertySize / 9f);
            houseRect.setWidth(propertySize / 9f);
            houseRect.setX(x + ((k+.5f)*2) * (propertySize / 9f));
            houseRect.setY(y + (propertySize / 9f));
            houseRect.setFill(Color.LIMEGREEN);
            houseRect.setVisible(false);
            houseRects[k] = houseRect;
        }

        Rectangle hotelRect = new Rectangle();
        hotelRect.setHeight(propertySize / 6f);
        hotelRect.setWidth(propertySize / 6f);
        hotelRect.setX(x + (propertySize / 2f) - (propertySize / 12f));
        hotelRect.setY(y + (propertySize/ 9f));
        hotelRect.setFill(Color.ORANGERED);
        hotelRect.setVisible(false);
        houseRects[4] = hotelRect;

        return houseRects;

    }

    private Rectangle initializeOwnershipRect(double x, double y, double propertySize) {

        Rectangle ownershipRect = new Rectangle();

        ownershipRect.setHeight(propertySize / 8f);
        ownershipRect.setWidth(propertySize - (propertySize / 4f));
        ownershipRect.setX(x + (propertySize / 8f));
        ownershipRect.setY(y + (propertySize - (propertySize / 8f)));
        ownershipRect.setFill(Color.WHITESMOKE);
        ownershipRect.setVisible(false);

        return ownershipRect;

    }

    private Text initializeMortgageText(double x, double y, double propertySize) {

        Text mortgageText = new Text(MORTGAGE_TEXT);

        mortgageText.setX(x);
        mortgageText.setY(y + (propertySize / 2f));
        mortgageText.setFont(MORTGAGE_FONT);
        mortgageText.setFill(Color.WHITE);
        mortgageText.setWrappingWidth(propertySize);
        mortgageText.setTextAlignment(TextAlignment.CENTER);
        mortgageText.setVisible(false);

        return mortgageText;

    }

    /**
     * Conforms the constructed template to a specified Game object,
     * ... and sets the `currentGameState` field in the process.
     * @param game Game object to conform template to.
     */
    // This method will error if initialize() isn't called first,
    // ... but initialize() is called in all constructors.
    private void conformToGame(Game game) {

        GameState gameState = game.getGameState();

        // Houses
        if (!Arrays.equals(gameState.houses, currentGameState.houses)) {

            for (int i = 0; i < gameState.houses.length; i++) {

                if (gameState.houses[i] == currentGameState.houses[i]) continue;

                int iT = LOGIC_TO_GFX_PROP_INDEX_MAP[i];  // "i, Translated"
                Rectangle propRect = rectangles.get(iT);
                Rectangle[] houseRects = houseRectangles.get(propRect);

                // Removing a preexisting hotel, house count indeterminate
                if (currentGameState.houses[i] == 5) {
                    houseRects[4].setVisible(false);
                }

                // Adding a hotel, houses must all be invisible
                if (gameState.houses[i] == 5) {
                    houseRects[4].setVisible(true);
                    for (int j = 0; j < 4; j++)
                        houseRects[j].setVisible(false);
                }
                // Handle all cases where we're not adding a hotel
                else {
                    for (int j = 0; j < gameState.houses[i]; j++)
                        houseRects[j].setVisible(true);
                    for (int j = gameState.houses[i]; j < 4; j++)
                        houseRects[j].setVisible(false);
                }

            }

        }

        // Mortgages
        if (!Arrays.equals(gameState.mortgages, currentGameState.mortgages)) {

            for (int i = 0; i < gameState.mortgages.length; i++) {

                if (gameState.mortgages[i] == currentGameState.mortgages[i]) continue;

                int iT = LOGIC_TO_GFX_PROP_INDEX_MAP[i];
                Rectangle propRect = rectangles.get(iT);
                Text mortgageText = mortgageTexts.get(propRect);

                // Mortgage text should be visible if the Property is mortgaged, invisible otherwise
                mortgageText.setVisible(gameState.mortgages[i]);

            }

        }

        // Ownership
        if (!Arrays.equals(gameState.ownership, currentGameState.ownership)) {

            for (int i = 0; i < gameState.ownership.length; i++) {

                if (gameState.ownership[i] == currentGameState.ownership[i]) continue;

                int iT = LOGIC_TO_GFX_PROP_INDEX_MAP[i];
                Rectangle propRect = rectangles.get(iT);
                Rectangle ownershipRect = ownershipRectangles.get(propRect);

                // Banner should be invisible if nobody owns the Property,
                // ... and should be visible & of the owner's color otherwise.
                if (gameState.ownership[i] == 0) {
                    ownershipRect.setVisible(false);
                } else {
                    ownershipRect.setVisible(true);
                    ownershipRect.setFill(PLAYER_SLOT_COLOR_MAP[gameState.ownership[i]-1]);
                }

            }

        }

        // Update `currentGameState`
        this.currentGameState = gameState;

    }

}