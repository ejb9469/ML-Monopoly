package client;

import server.PromptString;

public class DebugOutPipe implements OutPipe {

    @Override
    public void output(String prompt) {
        System.out.println(prompt);
    }

    @Override
    public void output(PromptString prompt) {
        System.out.println(prompt);
    }

}