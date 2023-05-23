public abstract class Card {

    protected String name;
    protected String description;

    public Card(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Card(String name) {
        this(name, "");
    }

    abstract void doAction();

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