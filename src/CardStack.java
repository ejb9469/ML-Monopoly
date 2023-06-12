import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class CardStack {

    public static final List<CARD> CHANCE_DEFAULT = CARD.getCardSet(false);
    public static final List<CARD> COMMUNITY_DEFAULT = CARD.getCardSet(true);

    protected List<CARD> stack;
    private List<CARD> alreadyDrawn = new ArrayList<>();
    private int stackLoops = 0;

    public CardStack(List<CARD> cardStack) {
        this.stack = cardStack;
    }

    public CARD drawCard() {
        if (stack.size() == 0) {
            stack = new ArrayList<>(alreadyDrawn);
            alreadyDrawn = new ArrayList<>();
            stackLoops++;
        }
        CARD card = stack.get(new SecureRandom().nextInt(stack.size()));
        alreadyDrawn.add(card);
        stack.remove(card);
        return card;
    }

    public int getNumStackLoops() {
        return stackLoops;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CardStack cardStack))
            return super.equals(object);
        // Remember, enums use ==
        return this.stack.equals(cardStack.stack) && this.alreadyDrawn.equals(cardStack.alreadyDrawn);
    }

    @Override
    public String toString() {
        try {
            return stack.get(0).toString();
        } catch (ArrayIndexOutOfBoundsException ex) {
            ex.printStackTrace();
            return super.toString();
        }
    }

}