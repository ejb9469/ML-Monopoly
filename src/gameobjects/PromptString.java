package gameobjects;

import playerobjects.Player;

/**
 * PromptString is a class that wraps metadata about the usage of a 'Prompt' String within the same object.
 */
public class PromptString {

    public String str;
    private final Player player;
    private final GameState gameState;

    public PromptString(String str, Player player, GameState gameState) {
        this.str = str;
        this.player = player;
        this.gameState = gameState;
    }

    public PromptString(String str, Player player) {
        this.str = str;
        this.player = player;
        this.gameState = null;
    }

    @Override
    public String toString() {
        String str = "";
        if (gameState != null) str += gameState.toString() + "\n";
        str += player.getName() + " | ";
        str += this.str;
        return str;
    }

}