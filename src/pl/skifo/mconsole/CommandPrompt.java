package pl.skifo.mconsole;

public interface CommandPrompt {
    public void sendCommand(String command, ResponseReceiver receiver);
}
