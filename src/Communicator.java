import java.util.UUID;

/**
 * Represents a class of classes which communicate between Player objects and the Game object.
 */
public interface Communicator {
    void requestAction(GameAction action, UUID key, GameObject wrapper);  // Client to server
    GameState requestCopyOfGameState();
}