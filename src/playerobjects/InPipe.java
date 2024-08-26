package playerobjects;

import gameobjects.ActionState;

public interface InPipe {
    ActionState query();
    String queryString();
    void close();
}