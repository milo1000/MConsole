package pl.skifo.mconsole;

import java.util.ArrayList;

import android.graphics.Paint;
import android.util.Log;

public class AttributedLine {

    private AttributedString[] strings;
    private AttributedLine[] singleLine = new AttributedLine[1];
    private int totalCnt;
    
    public AttributedLine(ArrayList<AttributedString> fragmentList) {
        strings = new AttributedString[fragmentList.size()]; 
        strings = fragmentList.toArray(strings);
        singleLine[0] = this;
        for (AttributedString s : strings) {
            totalCnt += s.text.length();
        }
    }
    
    public AttributedLine(String line, int color) {
        strings = new AttributedString[1];
        strings[0] = new AttributedString(line, color);
        singleLine[0] = this;
        totalCnt = line.length();
    }

    public AttributedLine(String line) {
        strings = new AttributedString[1];
        strings[0] = new AttributedString(line, AttributedString.DEFAULT_COLOR);
        singleLine[0] = this;
        totalCnt = line.length();
    }
    
    public AttributedString[] getStrings() {
        return strings;
    }
    
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (AttributedString s : strings) {
            out.append(s.text);
        }
        return out.toString();
    }

    public AttributedLine[] wrap(Paint metrics, float maxWidth) {
        AttributedLine[] ret = singleLine;
        
        float letterWidth = metrics.measureText("M");
        int lettersPerLine = (int) (maxWidth/letterWidth);
        
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("AttrL", "wrap: lettersPerLine = "+lettersPerLine+", totalCnt = "+totalCnt);
        
        if (lettersPerLine > 0 && lettersPerLine < totalCnt) {
            ArrayList<AttributedString> lineArray = new ArrayList<AttributedString>(); 
            ret = new AttributedLine[(totalCnt/lettersPerLine) + ((totalCnt%lettersPerLine > 0) ? 1:0)];
            int currentWord = 0;
            int currentWordRemainingLetters = strings[0].text.length();
            int currentWordLen = currentWordRemainingLetters;
            int lineSpaceLeft = lettersPerLine;
            int currentLine = 0;
            while (true) {
                if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("AttrL", "currentLine = "+currentLine+", lineSpaceLeft = "+lineSpaceLeft+", currentWord = "+currentWord+", cWL = "+currentWordLen+", cWRL = "+currentWordRemainingLetters);
                if (currentWordRemainingLetters <= lineSpaceLeft) { // entire/remaining part of word fits in line
                    if (currentWordRemainingLetters == currentWordLen)
                        lineArray.add(strings[currentWord]);
                    else {
                        lineArray.add(new AttributedString(strings[currentWord].text.substring(currentWordLen - currentWordRemainingLetters),strings[currentWord].color));
                    }
                    currentWord++;
                    if (currentWord == strings.length) {
                        ret[currentLine] = new AttributedLine(lineArray);
                        break;
                    }
                    lineSpaceLeft -= currentWordRemainingLetters; 
                    currentWordRemainingLetters = strings[currentWord].text.length();
                    currentWordLen = currentWordRemainingLetters;
                }
                else { // just begining/some bytes of word fits in line
                    int bIdx = currentWordLen - currentWordRemainingLetters;
                    int eIdx = bIdx + lineSpaceLeft;
                    lineArray.add(new AttributedString(strings[currentWord].text.substring(bIdx, eIdx),strings[currentWord].color));
                    ret[currentLine] = new AttributedLine(lineArray);
                    lineArray.clear();
                    currentWordRemainingLetters -= lineSpaceLeft;
                    lineSpaceLeft = lettersPerLine;
                    currentLine++;
                }
            }
        }
        return ret;
    }
    
    private static enum State {
        Look_for_escape,
        Look_for_escape2,
        Read_control_code
    };
    
    public static AttributedLine createAttributedLine(byte[] buff, int startOffset, int cnt) {

        ArrayList<AttributedString> line = new ArrayList<AttributedString>(); 
        State state = State.Look_for_escape;
        int idx = startOffset;
        int currentColor = MinecraftColorScheme.MINECRAFT_COLOR__DEFAULT_FG;
        int i = startOffset;
        int endOffset = startOffset + cnt; 
        for (; i < endOffset; i++) {
            switch (state) {
                case Look_for_escape: {
                    if((buff[i] & 0xff) == 0xc2) {
                        state = State.Look_for_escape2;
                    }
                    else if((buff[i] & 0xff) == 0xa7) { // some bukkit servers do not send 0xc2, just 0xa7
                        state = State.Read_control_code;
                    }
                    if (state != State.Look_for_escape) {
                        if (i > idx) {
                            AttributedString as = new AttributedString(new String(buff, idx, (i - idx)), currentColor);
                            line.add(as);
                        }
                        
                    }
                    break;
                }
                case Look_for_escape2: {
                    if((buff[i] & 0xff) == 0xa7) {
                        state = State.Read_control_code;
                    }
                    else {
                        idx = i + 1;
                        state = State.Look_for_escape;
                    }
                    break;
                }
                case Read_control_code: {
                    int val = (buff[i] & 0xff);
                    if (val >= 48 && val <= 57) { // ascii 0 - 9
                        val -= 48;
                    }
                    else if (val >= 97 && val <= 102) { // ascii a - f
                        val -= 87;
                    }
                    if (val < MinecraftColorScheme.MINECRAFT_COLOR__MAPPING.length) {
                        currentColor = MinecraftColorScheme.MINECRAFT_COLOR__MAPPING[val];
                    }
                    // else - otherwise just skip control code
                    idx = i + 1;
                    state = State.Look_for_escape;
                    break;
                }
            }
        }
        if (idx < i) {
            AttributedString as = new AttributedString(new String(buff, idx, (i - idx)), currentColor);
            line.add(as);
        }
        return new AttributedLine(line);
    }
}
