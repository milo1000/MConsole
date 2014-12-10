package pl.skifo.mconsole;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;

public class ConsoleOutputImpl extends ImageView implements ResponseReceiver, ConsoleOutput {

    private static final float DEFAULT_FONT_SIZE = 18.0f;
    private static final int MAX_LINES_IN_BUFFER = 200;
    private static final int MAX_LINES_IN_SCROLLBACK = 100;
    private static final String LOG_PREFIX = "ConOut";
    
    
    private Paint paint;
    private Paint scratchPaint;
    private float fontSize;
    private ArrayList<AttributedLine> lineBuffer;
    private AttributedLine[] lineBufferUnwrapped;
    private ScrollView parent; 
    

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
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "console output init");
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

    
    public void setParent(ScrollView p) {
        parent = p;
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "onMeasure w = "+View.MeasureSpec.toString(widthMeasureSpec)+", h = "+View.MeasureSpec.toString(heightMeasureSpec));
        
        int mode = View.MeasureSpec.getMode(heightMeasureSpec);
        if (mode == View.MeasureSpec.EXACTLY) {
            int h = View.MeasureSpec.getSize(heightMeasureSpec);
            
            Paint.FontMetricsInt fmi = scratchPaint.getFontMetricsInt();
            float rowSize = fmi.bottom - fmi.top;
            int maxVisibleRows = (int) (h / rowSize);
            if (maxVisibleRows < MAX_LINES_IN_SCROLLBACK) {
                heightMeasureSpec = View.MeasureSpec.makeMeasureSpec((int)(MAX_LINES_IN_SCROLLBACK * rowSize), mode);
            }
        }
        
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int h = getHeight();
        float indent = 4.0f;
        float maxWidth = getWidth() - indent;
        Paint.FontMetricsInt fmi = scratchPaint.getFontMetricsInt();
        float rowSize = fmi.bottom - fmi.top;
        
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "h = "+h+", w = "+getWidth());
        
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
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        if (parent != null) {
            parent.fullScroll(View.FOCUS_DOWN);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void response(ServerResponse response) {
        addBlock(response.getResponseBlock());
        if (parent != null) {
            parent.fullScroll(View.FOCUS_DOWN);
        }
        postInvalidate();
    }

    @Override
    public void refresh() {
        postInvalidate();
    }
}
