package pl.skifo.mconsole;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;

public class BanlistDialog extends DialogFragment {

    private CommandPrompt prompt;
    private String[] al;
    private boolean[] checked;

    public BanlistDialog(CommandPrompt prompt, String[] bl) {
        this.prompt = prompt;
        this.al = bl;
        checked = new boolean[bl.length];
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMultiChoiceItems(al, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("BanlistDialog", which+" "+checked[which]+" -> "+isChecked);
                checked[which] = isChecked;
            }
        });

        builder.setTitle(R.string.server_console_action_banlist);
        builder.setPositiveButton(R.string.player_console_action_unban, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String command = CommandSet.getCommand(CommandSet.PARDON);
                for (int i = 0; i < checked.length; i++) {
                    if (checked[i]) {
                        prompt.sendCommand(command+al[i], new ResponseReceiver() {
                            @Override
                            public void response(ServerResponse response) {
                            }
                        });
                    }
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
    
    private static class SayResponse implements ResponseReceiver {

        private String msg;
        private Context ctx;
        
        public SayResponse(Context ctx, String msg) {
            this.ctx = ctx;
            this.msg = msg;
        }
        
        @Override
        public void response(ServerResponse response) {
            // TODO Auto-generated method stub
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
        }
        
    }
}
