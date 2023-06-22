package gfx;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MonopolySwing {

    public static final int SIZE = 1000;

    public static final Color CHANCE_COLOR = new Color(80, 150, 150);
    public static final Color COMMUNITY_COLOR = new Color(200, 200, 120);

    public static final Color[][] PROPERTY_COLOR_MAP = new Color[][] {
            {Color.LIGHT_GRAY, Color.RED, CHANCE_COLOR, Color.RED, Color.RED, Color.BLACK, Color.YELLOW, Color.YELLOW, Color.PINK, Color.YELLOW, Color.LIGHT_GRAY},
            {Color.ORANGE, Color.GREEN},
            {Color.ORANGE, Color.GREEN},
            {COMMUNITY_COLOR, COMMUNITY_COLOR},
            {Color.ORANGE, Color.GREEN},
            {Color.BLACK, Color.BLACK},
            {Color.MAGENTA, CHANCE_COLOR},
            {Color.MAGENTA, Color.BLUE},
            {Color.PINK, Color.LIGHT_GRAY},
            {Color.MAGENTA, Color.BLUE},
            {Color.LIGHT_GRAY, Color.CYAN, Color.CYAN, CHANCE_COLOR, Color.CYAN, Color.BLACK, Color.LIGHT_GRAY, new Color(139, 69, 19), COMMUNITY_COLOR, new Color(139, 69, 19), Color.LIGHT_GRAY}
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

    private static void createAndDisplayGUI() {

        JFrame jFrame = new JFrame("Monopoly");
        jFrame.setLayout(new FlowLayout());
        jFrame.setSize(SIZE, SIZE);
        jFrame.setBackground(Color.DARK_GRAY);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel jPanel = new MonopolyPanel(SIZE, SIZE);
        jPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 6));
        jFrame.add(jPanel);

        int propertyPanelSize = SIZE/12;

        //Random random = new Random();

        // TODO: Update text on Properties

        for (int i = 0; i < 11; i++) {
            // If first or last, fill all cells with an opaque Property. Only do the rims otherwise.
            if (i % 10 == 0) {
                for (int j = 0; j < 11; j++) {
                    JPanel propertyPanel = new MonopolyPanel(propertyPanelSize, propertyPanelSize);
                    propertyPanel.setLayout(new FlowLayout());
                    propertyPanel.setBackground(PROPERTY_COLOR_MAP[i][j]);
                    JLabel text = new JLabel(PROPERTY_NAME_MAP[i][j]);
                    text.setBorder(new EmptyBorder(15, 15, 15, 15));
                    text.setFont(new Font("Arial", Font.BOLD, 10));
                    //text.setLineWrap(true);
                    text.setBackground(new Color(0, 0, 0, 0));
                    propertyPanel.add(text);
                    jPanel.add(propertyPanel);
                }
            } else {
                int a = -1;
                for (int j = 0; j < 11; j++) {
                    JPanel propertyPanel = new MonopolyPanel(propertyPanelSize, propertyPanelSize);
                    // If first or last, color in. Otherwise, make it transparent.
                    if (j % 10 == 0) {
                        propertyPanel.setBackground(PROPERTY_COLOR_MAP[i][++a]);
                        propertyPanel.setLayout(new FlowLayout());
                        JLabel text = new JLabel(PROPERTY_NAME_MAP[i][a]);
                        text.setBorder(new EmptyBorder(15, 15, 15, 15));
                        text.setFont(new Font("Arial", Font.BOLD, 10));
                        //text.setLineWrap(true);
                        text.setBackground(new Color(0, 0, 0, 0));
                        propertyPanel.add(text);
                    } else {
                        propertyPanel.setBackground(new Color(0, 0, 0, 0));
                        if (i == 3 && j == 3)
                            propertyPanel.setBackground(COMMUNITY_COLOR);
                        else if (i == 7 && j == 7)
                            propertyPanel.setBackground(CHANCE_COLOR);
                    }
                    jPanel.add(propertyPanel);
                }
            }
        }

        jPanel.setVisible(true);
        jFrame.pack();
        jFrame.setVisible(true);

    }

    // Temporary
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(MonopolySwing::createAndDisplayGUI);
    }

}