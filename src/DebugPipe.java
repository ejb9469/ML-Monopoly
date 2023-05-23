import java.util.UUID;

/**
 * Simple implementation of the Communicator interface intended for development and debugging.
 */
public class DebugPipe implements Communicator {

    private Game server;

    public DebugPipe(Game server) {
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