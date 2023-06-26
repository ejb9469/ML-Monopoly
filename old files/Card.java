/**
 * Class representing a Chance or Community Chest card.
 */
public class Card {

    protected String name;
    protected String description;
    private int id;

    public Card(String name, String description, int id) {
        this.name = name;
        this.description = description;
        this.id = id;
    }

    public Card(String name, int id) {
        this(name, "", id);
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Card))
            return super.equals(object);
        Card card = (Card)object;
        return (this.name.equals(card.name) && this.description.equals(card.description));
    }

}