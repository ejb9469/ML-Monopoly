package client;

import server.PromptString;

/**
 * Defines a class of classes which will output prompt data.
 */
public interface OutPipe {
    void output(String prompt);
    void output(PromptString prompt);
}