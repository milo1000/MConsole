package pl.skifo.mconsole;

public class AttributedString {

    public static final int DEFAULT_COLOR = MinecraftColorScheme.MINECRAFT_COLOR__DEFAULT_FG;
    
    public final String text;
    public final int color; //argb
    
    public AttributedString(String t, int c) {
        text = t;
        color = c;
    }
}
