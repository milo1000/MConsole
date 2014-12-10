package pl.skifo.mconsole;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class ConsoleOutputImpl extends ImageView implements ResponseReceiver, ConsoleOutput {

    private static final float DEFAULT_FONT_SIZE = 18.0f;
    private static final int TOP_MARGIN = 4;
    private static final int MAX_LINES_IN_BUFFER = 200;
    
    
    private Paint paint;
    private Paint scratchPaint;
    private float fontSize;
    private ArrayList<AttributedLine> lineBuffer;
    private AttributedLine[] lineBufferUnwrapped;

    private void init(Context context) {
        paint = new Paint();
        paint.setARGB(0xff, 0x80, 0x80, 0x80);
        float scale = context.getResources().getDisplayMetrics().density;
        paint.setTextSize(DEFAULT_FONT_SIZE * scale + 0.5f);
        paint.setTypeface(Typeface.MONOSPACE);
        scratchPaint = new Paint(paint);
        fontSize = DEFAULT_FONT_SIZE;
        lineBuffer = new ArrayList<AttributedLine>();
        lineBufferUnwrapped = new AttributedLine[MAX_LINES_IN_BUFFER];
        if (context instanceof ServerConsole) {
            ((ServerConsole)context).registerDefaultConsoleOutput(this);
        }
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(MConsoleActivity.LOG_PREFIX, "console output init");
    }
    
    public ConsoleOutputImpl(Context context) {
        super(context);
        init(context);
    }

    public ConsoleOutputImpl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ConsoleOutputImpl(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    public void addLine(String line) {
        addLine(new AttributedLine(line));
    }

    @Override
    public void addLine(AttributedLine line) {
        synchronized (this) {
            if (lineBuffer.size() == MAX_LINES_IN_BUFFER) {
                lineBuffer.remove(lineBuffer.size() - 1);
            }
            lineBuffer.add(0, line);
        }
    }
    
    @Override
    public void addBlock(AttributedBlock block) {
        AttributedLine lines[] = block.getLines();
        synchronized(this) {
            for (AttributedLine l : lines) {
                addLine(l);
            }
        }
    }
    
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int h = getHeight();
        float indent = 4.0f;
        float maxWidth = getWidth() - indent;
        Paint.FontMetricsInt fmi = scratchPaint.getFontMetricsInt();
        float rowSize = fmi.bottom - fmi.top;
        
        if (h > 0) {
            int maxVisibleRows = (int) (h / rowSize);
            if (maxVisibleRows > 0) {
            
                synchronized (this) {

                    int rowCnt = lineBuffer.size();
                    int wrappedLinesCnt = 0;
                    int j = 0;

                    int rowLeft = maxVisibleRows;

                    while (rowLeft > 0 && j < rowCnt && wrappedLinesCnt < lineBufferUnwrapped.length) {
                        AttributedLine al = lineBuffer.get(j);
                        AttributedLine[] wrappedLines = al.wrap(scratchPaint, maxWidth);
                        for (int k = wrappedLines.length - 1; k >= 0 && rowLeft > 0 && wrappedLinesCnt < lineBufferUnwrapped.length; k--) {
                            lineBufferUnwrapped[wrappedLinesCnt] = wrappedLines[k];
                            wrappedLinesCnt++;
                            rowLeft--;
                        }
                        j++;
                    }

                    float y = h - fmi.bottom;
//                    Log.d("canvas", "maxVisibleRows = "+maxVisibleRows+", start from "+y);
//                    Paint.FontMetrics fm = scratchPaint.getFontMetrics();
//                    Log.d("canvas", "ascent = "+fm.ascent+", descent = "+fm.descent+", bottom = "+fm.bottom+", top = "+fm.top+", leading = "+fm.leading);
//                    Log.d("canvas", "ascent = "+fmi.ascent+", descent = "+fmi.descent+", bottom = "+fmi.bottom+", top = "+fmi.top+", leading = "+fmi.leading);
                    
                    for (j = 0; j < wrappedLinesCnt; j++) {
                        AttributedLine al = lineBufferUnwrapped[j];  
                        AttributedString[] str = al.getStrings();
                        float xpos = indent;
                        for (AttributedString s : str) {
                            scratchPaint.setColor(s.color);
//                            Log.d("canvas", "txt: "+s.text+" @ ["+xpos+"x"+y+"]");
                            canvas.drawText(s.text, xpos, y, scratchPaint);
//                            canvas.drawLine(xpos, y, xpos + 100.0f, y, scratchPaint);
                            xpos += scratchPaint.measureText(s.text);
                        }
                        y -= rowSize;
                    }
                }
            }
        }
    }

    @Override
    public void response(ServerResponse response) {
        addBlock(response.getResponseBlock());
        postInvalidate();
    }

    @Override
    public void refresh() {
        postInvalidate();
    }
    
}
