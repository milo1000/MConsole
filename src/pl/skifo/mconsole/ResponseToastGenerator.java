package pl.skifo.mconsole;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

public class ResponseToastGenerator implements ResponseReceiver {

    private Activity a;
    private String player;
    private int o, f;
    private ResponseEvaluator eval;
    
    public ResponseToastGenerator(Activity parent, String pName, ResponseEvaluator eval, int okText, int failText) {
        a = parent;
        player = pName;
        o = okText;
        f = failText;
        this.eval = eval;
    }

    public ResponseToastGenerator(Activity parent, ResponseEvaluator eval, int okText, int failText) {
        this(parent, null, eval, okText, failText);
    }
    
    @Override
    public void response(ServerResponse response) {
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("PlayerAdminMode", "response: "+response);
        Resources r = a.getResources();
//        String ok = r.getString(o);
//        String ok = r.getString(o);
        //int code = (response.getResponseBlock() == ServerResponse.EMPTY_RESPONSE) ? o:f;
        int code = (eval.isOK(response)) ? o:f;
        String txt = r.getString(code);         
        Toast.makeText(a, (player != null) ? player+" "+txt : txt, Toast.LENGTH_SHORT).show();
    }
}
