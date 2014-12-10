package pl.skifo.mconsole;

import java.util.ArrayList;

public class AttributedBlock {

    public static final AttributedBlock EMPTY_BLOCK = new AttributedBlock();  
     
    
    
    private AttributedLine[] lines;
    
    public AttributedBlock(ArrayList<AttributedLine> lineList) {
        lines = new AttributedLine[lineList.size()]; 
        lines = lineList.toArray(lines);
    }

    public AttributedBlock() {
        lines = new AttributedLine[0]; 
    }

    public AttributedBlock(String line, int color) {
        lines = new AttributedLine[1];
        lines[0] = new AttributedLine(line, color);
    }

    public AttributedBlock(String line) {
        this(line, AttributedString.DEFAULT_COLOR); 
    }
    
    public AttributedLine[] getLines() {
        return lines;
    }
    
    public String toString() {
        StringBuilder out = new StringBuilder();
        int i = 0;
        for (; i < (lines.length - 1); i++) {
            out.append(lines[i]);
            out.append("\n");
        }
        if (lines.length > 0)
            out.append(lines[i]);
        return out.toString();
    }

    
    public static AttributedBlock createAttributedBlock(byte[] buff, int cnt) {
        ArrayList<AttributedLine> block = new ArrayList<AttributedLine>(); 
        int idx = 0;
        int i = 0;
        for (; i < cnt; i++) { // tokenize
            if ((buff[i] & 0xff) == 0x0a) {
                AttributedLine line = AttributedLine.createAttributedLine(buff, idx, (i - idx));
                block.add(line);
                idx = i + 1;
            }
        }
        if (idx < i) {
            AttributedLine line = AttributedLine.createAttributedLine(buff, idx, (i - idx));
            block.add(line);
        }
        return new AttributedBlock(block);
    }
    
}
