import javax.swing.JPanel;
import java.awt.*;

public class MonopolyPanel extends JPanel {

    private final int x;
    private final int y;

    public MonopolyPanel(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    public MonopolyPanel() {
        super();
        this.x = 0;
        this.y = 0;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(this.x, this.y);
    }

}