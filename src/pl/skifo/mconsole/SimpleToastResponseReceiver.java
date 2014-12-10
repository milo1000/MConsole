package pl.skifo.mconsole;

import android.content.Context;
import android.widget.Toast;

public class SimpleToastResponseReceiver implements ResponseReceiver {

    private String msg;
    private Context ctx;
    
    public SimpleToastResponseReceiver(Context ctx, String msg) {
        this.ctx = ctx;
        this.msg = msg;
    }
    
    @Override
    public void response(ServerResponse response) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }
    
}
