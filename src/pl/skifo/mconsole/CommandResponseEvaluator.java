package pl.skifo.mconsole;

public class CommandResponseEvaluator implements ResponseEvaluator {

    public enum EvaluatorType {
        kick,
        op,
        deop,
        ban,
        pardon,
        save,
        difficulty,
        weather,
        time,
        time_add,
        teleport,
        toggledownfall,
        unknown
    };
    
    private EvaluatorType t; 
    
    public CommandResponseEvaluator(EvaluatorType type) {
        t = type;
    }
    
    @Override
    public boolean isOK(ServerResponse response) {
        String s = response.getResponseBlock().toString();
        switch (t) {
            case kick: return s.startsWith("Kicked");
            case op: return s.startsWith("Opped");
            case deop: return s.startsWith("De-opped");
            case ban: return s.startsWith("Banned");
            case pardon: return s.startsWith("Unbanned");
            case save: return s.contains("Saved") || s.contains("complete");//s.startsWith("Saving...Saved");
            case difficulty: return s.startsWith("Set");
            case weather: return s.startsWith("Chang");
            case time: return s.startsWith("Set");
            case time_add: return s.startsWith("Added");
            case teleport: return s.startsWith("Teleported");
            case toggledownfall: return s.startsWith("Togg");
            default:
        }
        return false;
    }
}
