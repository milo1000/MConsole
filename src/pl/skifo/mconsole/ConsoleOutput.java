package pl.skifo.mconsole;

public interface ConsoleOutput {

    public void addLine(String line);

    public void addLine(AttributedLine line);
    
    public void addBlock(AttributedBlock block);
    
    public void refresh();
}
