import java.util.UUID;

// Intending to create two main implementations of this interface:
    // 1) "AgentCommunicator" for Machine Learning
    // 2) "DebugCommunicator" for development & debugging

/**
 * Represents a class of classes which communicate between Player objects and the Game object.
 */
public interface Communicator {
    void requestAction(GameAction action, UUID key, GameObject wrapper);  // Client to server
    GameState requestCopyOfGameState();
}