package playerobjects;

import gameobjects.Game;
import gameobjects.GameAction;
import gameobjects.GameObject;
import gameobjects.GameState;

import java.util.UUID;

/**
 * Simple implementation of the Communicator interface intended for development and debugging.
 */
public class DebugCommunicator implements Communicator {

    public final Game server;  // TODO: Temporarily public!

    public DebugCommunicator(Game server) {
        this.server = server;
    }

    @Override
    public void requestAction(GameAction action, UUID key, GameObject wrapper) {
        server.requestAction(action, key, wrapper);
    }

    @Override
    public GameState requestCopyOfGameState() {
        return server.getGameState();
    }

}