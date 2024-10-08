package gfx;

import gameobjects.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static gfx.MonopolyGraphicsFX.*;
import static main.Monopoly.MAX_PLAYERS;

// TODO: Allow for creation of a MonopolyGroup, based on a previous instantiated MonopolyGroup;
//          ... Stop creating a new one each time. (08-25-24)

public class MonopolyGroup extends Group {

    public static final Font TEXT_FONT = new Font("Arial Bold", 11);
    public static final Font MORTGAGE_TEXT_FONT = new Font("Arial Black", 11);  // formerly Helvetica
    public static final Font TOKEN_TEXT_FONT = new Font("Arial Bold", 16);
    public static final Font TURN_INDICATOR_FONT = new Font("Arial Bold", 36);

    public static final String BUTTON_COLOR_HEX = "#5b5b5b";
    public static final String MORTGAGE_TEXT = "MTG";

    public static final double ADJUSTMENT_CONST = 10f;

    private final List<Rectangle> rectangles = new ArrayList<>();
    private final Map<Rectangle, Rectangle[]> houseRectangles = new HashMap<>();  // Hotel rectangle will be at index 4
    private final Map<Rectangle, Text> propertyTexts = new HashMap<>();
    private final Map<Rectangle, Rectangle> ownershipRectangles = new HashMap<>();
    private final Map<Rectangle, Text> mortgageTexts = new HashMap<>();

    private final List<Button> buttons = new ArrayList<>();

    private final List<Text> playerCashTexts = new ArrayList<>();
    private final List<Text> turnIndicatorText = new ArrayList<>();
    private final List<Text> gtfoJailTexts = new ArrayList<>();

    private Map<Text, Rectangle> tokenPlacements = new HashMap<>();

    GameState currentGameState;
    ActionState currentActionState;

    /**
     * Constructs a blank MonopolyGroup (a template).
     */
    public MonopolyGroup() {
        super();
        initialize();
        currentGameState = null;
        currentActionState = null;
    }

    /**
     * Constructs a MonopolyGroup template, then uses it to express a Game.
     * @param gameState GameState to conform template to.
     */
    public MonopolyGroup(GameState gameState, ActionState actionState) {
        this();
        currentGameState = gameState;
        currentActionState = actionState;
        conformToGameState(gameState, actionState);
    }



    ///////////////////////////////////////////////////////////////////////////////

    /**
     * Initializer method for a 'default' GUI template.
     * Adds to .getChildren() and adds to custom List & Map fields.
     * Called (eventually) in all constructors, and before any conforming actions.
     */
    private void initialize() {

        double propertySize = WINDOW_DIM / 13f;
        double propertyGap = propertySize / 13f;

        List<Node[]> playerInfoOverlay = initializePlayerInfoOverlay(3.5f * WINDOW_DIM / 6f, 1.2f * WINDOW_DIM / 6f - (ADJUSTMENT_CONST*3), WINDOW_DIM / 6f);
        Text turnIndicatorText = initializeTurnIndicatorText(1.5f * WINDOW_DIM / 6f, 1.2f * WINDOW_DIM / 6f);
        HBox buttonPanel = initializeButtonPanel(3f * WINDOW_DIM / 6f, 4.175f * WINDOW_DIM / 6f, 50);  // Buttons added to `this.buttons` in `initializeButtonPanel()`!

        this.turnIndicatorText.add(turnIndicatorText);
        // The below cast is safe because we know for a fact we're only dealing with Text objects.
        List<Text> playerCashTexts = (ArrayList)(new ArrayList<>(Arrays.asList(playerInfoOverlay.get(2))));
        List<Text> gtfoJailTexts = (ArrayList)(new ArrayList<>(Arrays.asList(playerInfoOverlay.get(3))));
        this.playerCashTexts.addAll(playerCashTexts);
        this.gtfoJailTexts.addAll(gtfoJailTexts);

        this.getChildren().add(turnIndicatorText);
        this.getChildren().add(buttonPanel);
        this.getChildren().addAll(playerInfoOverlay.get(0));  // Background rectangle
        this.getChildren().addAll(playerInfoOverlay.get(1));  // Player-specific background rectangles
        this.getChildren().addAll(playerInfoOverlay.get(2));  // Player cash text indicators
        this.getChildren().addAll(playerInfoOverlay.get(3));  // Gtfo jail indicators

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

    /**
     * Conforms the constructed template to a specified Game object.
     * Sets the `currentGameState` field in the process.
     * Called in GameState-specific constructors and as an update procedure.
     * Will necessarily be performed after the execution of `initialize()`.
     * @param gameState GameState object to conform template to.
     */
    // This method will error if initialize() isn't called first,
    // ... but initialize() is called in all constructors.
    // TODO: Un-comment if statements, but store `currentGameState` somewhere down the callstack and pass it in here,
    //          rather than storing it as a field.
    public void conformToGameState(GameState gameState, ActionState actionState) {

        // Tokens
        spawnTokens(gameState.playerLocations);

        // Turn indicator
        //if (gameState.turnIndicator != currentGameState.turnIndicator) {

            this.getChildren().remove(turnIndicatorText.get(0));

            turnIndicatorText.get(0).setText("Turn: Player " + (gameState.turnIndicator + 1));
            if (gameState.turnIndicator >= 0)
                turnIndicatorText.get(0).setFill(PLAYER_SLOT_COLOR_MAP[gameState.turnIndicator]);

            this.getChildren().add(turnIndicatorText.get(0));

        //}

        // Cash
        //if (!Arrays.equals(gameState.cash, currentGameState.cash)) {

            this.getChildren().removeAll(playerCashTexts);

            for (int i = 0; i < gameState.cash.length; i++) {

                if (gameState.playerBankruptcy[i])
                    playerCashTexts.get(i).setText("BANKRUPT");
                else
                    playerCashTexts.get(i).setText("$" + gameState.cash[i]);

            }

            this.getChildren().addAll(playerCashTexts);

        //}

        // GTFO Jail Cards
        //if (!Arrays.equals(gameState.gtfoJailCards, currentGameState.gtfoJailCards)) {

            for (int i = 0; i < gameState.gtfoJailCards.length; i++) {

                this.getChildren().remove(gtfoJailTexts.get(i));

                gtfoJailTexts.get(i).setText(" G".repeat(gameState.gtfoJailCards[i]));

                this.getChildren().add(gtfoJailTexts.get(i));

            }

        //}

        // Houses
        //if (!Arrays.equals(gameState.houses, currentGameState.houses)) {

            for (int i = 0; i < gameState.houses.length; i++) {

                //if (gameState.houses[i] == currentGameState.houses[i]) continue;

                int iT = LOGIC_TO_GFX_PROP_INDEX_MAP[i];  // "i, Translated"
                Rectangle propRect = rectangles.get(iT);
                Rectangle[] houseRects = houseRectangles.get(propRect);

                this.getChildren().removeAll(houseRects);

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

                this.getChildren().addAll(houseRects);

            }

        //}

        // Mortgages
        //if (!Arrays.equals(gameState.mortgages, currentGameState.mortgages)) {

            for (int i = 0; i < gameState.mortgages.length; i++) {

                //if (gameState.mortgages[i] == currentGameState.mortgages[i]) continue;

                int iT = LOGIC_TO_GFX_PROP_INDEX_MAP[i];
                Rectangle propRect = rectangles.get(iT);
                Text mortgageText = mortgageTexts.get(propRect);

                this.getChildren().remove(mortgageText);

                // Mortgage text should be visible if the Property is mortgaged, invisible otherwise
                mortgageText.setVisible(gameState.mortgages[i]);

                this.getChildren().add(mortgageText);

            }

        //}

        // Ownership
        //if (!Arrays.equals(gameState.ownership, currentGameState.ownership)) {

            for (int i = 0; i < gameState.ownership.length; i++) {

                //if (gameState.ownership[i] == currentGameState.ownership[i]) continue;

                int iT = LOGIC_TO_GFX_PROP_INDEX_MAP[i];
                Rectangle propRect = rectangles.get(iT);
                Rectangle ownershipRect = ownershipRectangles.get(propRect);

                this.getChildren().remove(ownershipRect);

                // Banner should be invisible if nobody owns the Property,
                // ... and should be visible & of the owner's color otherwise.
                if (gameState.ownership[i] == -1) {
                    ownershipRect.setVisible(false);
                } else {
                    ownershipRect.setVisible(true);
                    ownershipRect.setFill(PLAYER_SLOT_COLOR_MAP[gameState.ownership[i]]);
                }

                this.getChildren().add(ownershipRect);

            }

        //}

        // Update `currentGameState`
        this.currentGameState = gameState;

    }

    ///////////////////////////////////////////////////////////////////////////////

    /**
     * Child function of `initialize()` responsible for initializing house & hotel indicators.
     * @return An array of Rectangles (house & hotel indicators) fixed to a given position.
     */
    private Rectangle[] initializeHouses(double x, double y, double propertySize) {

        Rectangle[] houseRects = new Rectangle[5];
        for (int k = 0; k < 4; k++) {
            Rectangle houseRect = new Rectangle();
            houseRect.setHeight(propertySize / 9f);
            houseRect.setWidth(propertySize / 9f);
            houseRect.setX(x + ((k+.5f)*2f) * (propertySize / 9f));
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

    /**
     * Child function of `initialize()` responsible for initializing ownership indicators.
     * @return An array of Rectangles (ownership indicators) fixed to a given position.
     */
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

    /**
     * Child function of `initialize()` responsible for initializing one Property's mortgage status indicator.
     * @return A Text object (mortgage status indicator) fixed to a given position.
     */
    private Text initializeMortgageText(double x, double y, double propertySize) {

        Text mortgageText = new Text(MORTGAGE_TEXT);

        mortgageText.setX(x);
        mortgageText.setY(y + (propertySize * .9f));
        mortgageText.setFont(MORTGAGE_TEXT_FONT);
        mortgageText.setFill(Color.DARKGRAY);
        mortgageText.setWrappingWidth(propertySize);
        mortgageText.setTextAlignment(TextAlignment.CENTER);
        mortgageText.setVisible(false);

        return mortgageText;

    }

    /**
     * Child function of `initialize()` responsible for initializing the entire 'player status' overlay.
     * This overlay contains info like player cash, get out of jail free cards, etc.
     * @return A List of arrays of Nodes (e.g. Rectangles, Texts, etc.) that make up the 'player status' overlay, fixed to a given position.
     */
    private List<Node[]> initializePlayerInfoOverlay(double x, double y, double size) {

        List<Node[]> overlayComponents = new ArrayList<>();

        Rectangle backgroundRect = new Rectangle();
        backgroundRect.setX(x);
        backgroundRect.setY(y);
        backgroundRect.setWidth(size);
        backgroundRect.setHeight(size + (ADJUSTMENT_CONST * (MAX_PLAYERS-1)));
        backgroundRect.setFill(BACKGROUND_COLOR);
        overlayComponents.add(new Node[]{backgroundRect});


        Node[] playerRects = new Node[MAX_PLAYERS];
        Node[] playerCashTexts = new Node[MAX_PLAYERS];
        Node[] gtfoJailTexts = new Node[MAX_PLAYERS];
        for (int i = 0; i < MAX_PLAYERS; i++) {

            Rectangle playerRect = new Rectangle();
            playerRect.setX(x);
            double yAdj = (y + i * (size/MAX_PLAYERS)) + (ADJUSTMENT_CONST * i);
            playerRect.setY(yAdj);
            playerRect.setWidth(size);
            double heightAdj = size / MAX_PLAYERS;
            playerRect.setHeight(heightAdj);
            playerRect.setFill(OVERLAY_COLOR);
            playerRects[i] = playerRect;

            Text playerCashText = new Text("$" + GameState.STARTING_CASH);
            playerCashText.setX(x + ADJUSTMENT_CONST);
            playerCashText.setY(yAdj + (heightAdj/2f));
            playerCashText.setWrappingWidth(x / 4f);
            if (i >= PLAYER_SLOT_COLOR_MAP.length)
                playerCashText.setFill(Color.WHITE);
            else
                playerCashText.setFill(PLAYER_SLOT_COLOR_MAP[i]);
            playerCashText.setFont(TEXT_FONT);
            playerCashTexts[i] = playerCashText;

            Text gtfoJailText = new Text();
            gtfoJailText.setX(x + (size/2f));
            gtfoJailText.setY(yAdj + (heightAdj/2f));
            gtfoJailText.setWrappingWidth(x / 4f);
            gtfoJailText.setFill(CARD_COLOR);
            gtfoJailText.setFont(MORTGAGE_TEXT_FONT);
            gtfoJailTexts[i] = gtfoJailText;

        }
        overlayComponents.add(playerRects);
        overlayComponents.add(playerCashTexts);
        overlayComponents.add(gtfoJailTexts);

        return overlayComponents;

    }

    /**
     * Child function of `initialize()` responsible for initializing the global turn indicator.
     * @return The Text object of the global turn indicator.
     */
    private Text initializeTurnIndicatorText(double x, double y) {

        Text turnIndicatorText = new Text("Turn: Player 1");
        turnIndicatorText.setFont(TURN_INDICATOR_FONT);
        turnIndicatorText.setFill(BACKGROUND_COLOR);
        turnIndicatorText.setX(x);
        turnIndicatorText.setY(y);

        return turnIndicatorText;

    }

    /**
     * Child function of `initialize()` responsible for initializing the button panel.
     * @return The HBox object (button panel) containing all button objects, separated into VBox columns.
     */
    private HBox initializeButtonPanel(double x, double y, double size) {

        TextField propertyNameField = new TextField("<Enter Property Name>");
        propertyNameField.setPrefHeight(2 * size / 3);
        propertyNameField.setPrefWidth(size * 3);

        Button diceButton = new Button();
        diceButton.setPrefWidth(size);
        diceButton.setPrefHeight(size);
        diceButton.setStyle(String.format("-fx-background-color: %s;", BUTTON_COLOR_HEX));
        try {
            FileInputStream fileInputStream = new FileInputStream("src/gfx/assets/dice.png");
            ImageView diceImage = new ImageView(new Image(fileInputStream));
            diceButton.setGraphic(diceImage);
            fileInputStream.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.err.println("dice.png not found!");
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Error closing FileInputStream :: dice.png !!");
        }
        diceButton.setOnMousePressed(e-> {
            GameObject context = new GameObject();
            context.objInt = 1;
            context.objBool = false;
            context.objProperty = Board.SQUARES.get(0);  // GO
            if (currentGameState.jailedPlayers[currentGameState.turnIndicator])
                MonopolyGraphicsFX.actionState = new ActionState(GameAction.JAIL_THROW_DICE, context);
            else
                MonopolyGraphicsFX.actionState = new ActionState(GameAction.MOVE_THROW_DICE, context);
        });

        Button endButton = new Button();
        //endButton.setFont(Font.font("Arial Bold", 8));
        endButton.setPrefWidth(size);
        endButton.setPrefHeight(size);
        endButton.setStyle(String.format("-fx-background-color: %s;", BUTTON_COLOR_HEX));
        try {
            FileInputStream fileInputStream = new FileInputStream("src/gfx/assets/rightarrow.png");
            ImageView arrowImage = new ImageView(new Image(fileInputStream));
            endButton.setGraphic(arrowImage);
            fileInputStream.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.err.println("rightarrow.png not found!");
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Error closing FileInputStream :: rightarrow.png !!");
        }
        endButton.setOnMousePressed(e-> {
            GameObject context = new GameObject();
            context.objInt = 1;
            context.objBool = false;
            context.objProperty = Board.SQUARES.get(0);  // GO
            MonopolyGraphicsFX.actionState = new ActionState(GameAction.END_TURN, context);
            System.out.println("End Button Pressed");
        });

        VBox col1 = new VBox(diceButton, endButton);
        col1.setSpacing(ADJUSTMENT_CONST);

        Button buyButton = new Button();
        buyButton.setPrefWidth(size);
        buyButton.setPrefHeight(size);
        buyButton.setStyle(String.format("-fx-background-color: %s;", BUTTON_COLOR_HEX));
        try {
            FileInputStream fileInputStream = new FileInputStream("src/gfx/assets/moneybag.png");
            ImageView moneyImage = new ImageView(new Image(fileInputStream));
            buyButton.setGraphic(moneyImage);
            fileInputStream.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.err.println("moneybag.png not found!");
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Error closing FileInputStream :: moneybag.png !!");
        }
        buyButton.setOnMousePressed(e-> {
            GameObject context = new GameObject();
            context.objInt = 1;
            context.objBool = true;
            context.objProperty = Board.SQUARES.get(currentGameState.playerLocations[currentGameState.turnIndicator]);
            MonopolyGraphicsFX.actionState = new ActionState(GameAction.PROPERTY_BUY_OR_AUCTION, context);
        });

        Button gavelButton = new Button();
        gavelButton.setPrefWidth(size);
        gavelButton.setPrefHeight(size);
        gavelButton.setStyle(String.format("-fx-background-color: %s;", BUTTON_COLOR_HEX));
        try {
            FileInputStream fileInputStream = new FileInputStream("src/gfx/assets/gavel.png");
            ImageView gavelImage = new ImageView(new Image(fileInputStream));
            gavelButton.setGraphic(gavelImage);
            fileInputStream.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.err.println("gavel.png not found!");
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Error closing FileInputStream :: gavel.png !!");
        }
        gavelButton.setOnMousePressed(e-> {
            GameObject context = new GameObject();
            context.objInt = 1;
            context.objBool = false;
            context.objProperty = Board.SQUARES.get(currentGameState.playerLocations[currentGameState.turnIndicator]);
            MonopolyGraphicsFX.actionState = new ActionState(GameAction.PROPERTY_BUY_OR_AUCTION, context);
        });

        VBox col2 = new VBox(buyButton, gavelButton);
        col2.setSpacing(ADJUSTMENT_CONST);

        Button bet10Button = new Button("BET\n 10");
        bet10Button.setFont(new Font("Arial Bold", 9));
        bet10Button.setTextFill(Color.GREENYELLOW);
        bet10Button.setPrefWidth(size);
        bet10Button.setPrefHeight(size);
        bet10Button.setStyle(String.format("-fx-background-color: %s;", BUTTON_COLOR_HEX));
        bet10Button.setOnMousePressed(e -> {
            GameObject context = new GameObject();
            context.objInt = currentGameState.getMaximumBid() + 10;
            context.objBool = false;
            context.objProperty = Board.SQUARES.get(currentGameState.biddingProperty);
            MonopolyGraphicsFX.actionState = new ActionState(GameAction.AUCTION_BID, context);
        });

        Button bet100Button = new Button("BET\n100");
        bet100Button.setFont(bet10Button.getFont());
        bet100Button.setTextFill(Color.GREENYELLOW);
        bet100Button.setPrefWidth(size);
        bet100Button.setPrefHeight(size);
        bet100Button.setStyle(String.format("-fx-background-color: %s;", BUTTON_COLOR_HEX));
        bet100Button.setOnMousePressed(e -> {
            GameObject context = new GameObject();
            context.objInt = currentGameState.getMaximumBid() + 100;
            context.objBool = false;
            context.objProperty = Board.SQUARES.get(currentGameState.biddingProperty);
            MonopolyGraphicsFX.actionState = new ActionState(GameAction.AUCTION_BID, context);
        });

        Button concedeButton = new Button("DROP\n OUT");
        concedeButton.setFont(bet10Button.getFont());
        concedeButton.setTextFill(Color.LIGHTPINK);
        concedeButton.setPrefWidth(size);
        concedeButton.setPrefHeight(size);
        concedeButton.setStyle(String.format("-fx-background-color: %s;", BUTTON_COLOR_HEX));
        concedeButton.setOnMousePressed(e -> {
            GameObject context = new GameObject();
            context.objInt = -1;
            context.objBool = false;
            context.objProperty = Board.SQUARES.get(currentGameState.biddingProperty);
            MonopolyGraphicsFX.actionState = new ActionState(GameAction.AUCTION_BID, context);
        });

        VBox col3 = new VBox(bet10Button, bet100Button, concedeButton);
        col3.setSpacing(ADJUSTMENT_CONST);

        Button mortgageButton = new Button();
        mortgageButton.setPrefWidth(size);
        mortgageButton.setPrefHeight(size);
        mortgageButton.setStyle(String.format("-fx-background-color: %s;", BUTTON_COLOR_HEX));
        try {
            FileInputStream fileInputStream = new FileInputStream("src/gfx/assets/mortgage.png");
            ImageView mortgageImage = new ImageView(new Image(fileInputStream));
            mortgageButton.setGraphic(mortgageImage);
            fileInputStream.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.err.println("mortgage.png not found!");
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Error closing FileInputStream :: mortgage.png !!");
        }
        mortgageButton.setOnMousePressed(e -> {
            GameObject context = new GameObject();
            context.objInt = -1;
            context.objBool = true;
            if (MonopolyGraphicsFX.selectedProperty != null) {
                context.objProperty = MonopolyGraphicsFX.selectedProperty;
                MonopolyGraphicsFX.actionState = new ActionState(GameAction.PROPERTY_MORTGAGE, context);
            } else {
                System.err.println("No property selected to mortgage!");
            }
        });

        Button unmortgageButton = new Button();
        unmortgageButton.setPrefWidth(size);
        unmortgageButton.setPrefHeight(size);
        unmortgageButton.setStyle(String.format("-fx-background-color: %s;", BUTTON_COLOR_HEX));
        try {
            FileInputStream fileInputStream = new FileInputStream("src/gfx/assets/unmortgage.png");
            ImageView unmortgageImage = new ImageView(new Image(fileInputStream));
            unmortgageButton.setGraphic(unmortgageImage);
            fileInputStream.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.err.println("unmortgage.png not found!");
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Error closing FileInputStream :: unmortgage.png !!");
        }
        unmortgageButton.setOnMousePressed(e -> {
            GameObject context = new GameObject();
            context.objInt = -1;
            context.objBool = true;
            if (MonopolyGraphicsFX.selectedProperty != null) {
                context.objProperty = MonopolyGraphicsFX.selectedProperty;
                MonopolyGraphicsFX.actionState = new ActionState(GameAction.PROPERTY_UNMORTGAGE, context);
            } else {
                System.err.println("No property selected to unmortgage!");
            }
        });

        VBox col4 = new VBox(mortgageButton, unmortgageButton);
        col4.setSpacing(ADJUSTMENT_CONST);

        HBox panel = new HBox(col1, col2, col3, col4);
        panel.setLayoutX(x);
        panel.setLayoutY(y);
        panel.setSpacing(ADJUSTMENT_CONST);

        buttons.add(diceButton);
        buttons.add(buyButton);
        buttons.add(endButton);
        buttons.add(gavelButton);
        buttons.add(bet10Button);
        buttons.add(bet100Button);
        buttons.add(concedeButton);
        buttons.add(mortgageButton);
        buttons.add(unmortgageButton);

        return panel;

    }

    /**
     * Child function of `conformToGame(GameState, ActionState)` responsible for spawning player tokens at the Players' respective positions on the board.
     * @param playerLocations The GameState object's playerLocations[] array.
     */
    private void spawnTokens(int[] playerLocations) {

        // Simply wipe the tokens map before performing operations.
        // TODO: The better way to do this is to check if a token is already spawned, ...
        //          ... and update its position instead if so.
        tokenPlacements = new HashMap<>();

        for (int i = 0; i < playerLocations.length; i++) {

            int iT = LOGIC_TO_GFX_PROP_INDEX_MAP[playerLocations[i]];
            Rectangle rect = rectangles.get(iT);
            double x = rect.getX();
            double y = rect.getY();
            double propertySize = rect.getWidth();

            Text token = new Text(""+(i+1));
            token.setX(x + (propertySize / (playerLocations.length) * (i-1.5f)));
            token.setY(y + (propertySize / 2f));
            token.setFont(TOKEN_TEXT_FONT);
            token.setFill(PLAYER_SLOT_COLOR_MAP[i]);
            token.setWrappingWidth(propertySize);
            token.setTextAlignment(TextAlignment.CENTER);
            //token.setVisible(false);

            tokenPlacements.put(token, rect);

        }

        this.getChildren().addAll(tokenPlacements.keySet());

    }

}