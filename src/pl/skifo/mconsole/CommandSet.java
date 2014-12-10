package pl.skifo.mconsole;

public class CommandSet {
    
    public static enum SupportedSet {
        STANDARD,
        ESSENTIALS
    };
    
    public static final int TIME       = 0; 
    public static final int KICK       = 1;
    public static final int OP         = 2;
    public static final int DEOP       = 3;
    public static final int PARDON     = 4;
    public static final int STOP       = 5;
    public static final int TP         = 6;
    public static final int BAN        = 7;
    public static final int GAMEMODE   = 8;
    public static final int SAY        = 9;
    public static final int TELL       = 10;
    public static final int LIST       = 11;
    public static final int DIFFICULTY = 12;
    public static final int BANLIST    = 13;
    public static final int SAVE_ALL   = 14;
    public static final int WEATHER    = 15;
    
    private static final String[] standardSet = {"time ", "kick ", "op ", "deop ",
                                                 "pardon ", "stop", "tp ", "ban ",
                                                 "gamemode ", "say ", "tell ", "list",
                                                 "difficulty ", "banlist", "save-all", "weather "};
    
    private static final String[] essentialsSet = {"minecraft:time ", "minecraft:kick ", "minecraft:op ", "minecraft:deop ",
                                                   "minecraft:pardon ", "stop", "minecraft:tp ", "minecraft:ban ",
                                                   "minecraft:gamemode ", "minecraft:say ", "minecraft:tell ", "minecraft:list",
                                                   "minecraft:difficulty ", "minecraft:banlist", "save-all", "minecraft:weather "};
    
    
    private static String[] currSet = standardSet;
    
    public static void setCurrentSet(SupportedSet set) {
        if (set == SupportedSet.ESSENTIALS)
            currSet = essentialsSet;
        if (set == SupportedSet.STANDARD)
            currSet = standardSet;
    }
    

    public static final String getCommand(int idx) {
        return currSet[idx];
    }
    
}
