import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CardStack {

    public static final List<Card> CHANCE_DEFAULT = new ArrayList<>();  // TODO
    public static final List<Card> COMMUNITY_DEFAULT = new ArrayList<>();  // TODO

    protected List<Card> stack;
    private List<Card> alreadyDrawn = new ArrayList<>();

    public CardStack() {
        this.stack = new ArrayList<>();
    }

    public CardStack(List<Card> cardStack) {
        this.stack = cardStack;
    }

    public Card drawCard() {
        Card card = stack.get(new Random().nextInt(stack.size()));
        alreadyDrawn.add(card);
        stack.remove(card);
        return card;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CardStack))
            return super.equals(object);
        CardStack cardStack = (CardStack)object;
        return this.stack.equals(cardStack.stack) && this.alreadyDrawn.equals(cardStack.alreadyDrawn);
    }

}