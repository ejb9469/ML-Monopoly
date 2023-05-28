public class DebugOutPipe implements OutPipe {

    @Override
    public void output(String prompt) {
        System.out.println(prompt);
    }

}