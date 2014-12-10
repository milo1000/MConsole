package pl.skifo.mconsole;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ConfirmDialog extends DialogFragment {

    private CommandPrompt prompt;

    public ConfirmDialog(CommandPrompt prompt) {
        this.prompt = prompt;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.server_console_action_shutdown_warning);
        builder.setIcon(R.drawable.ic_action_warning);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String command = CommandSet.getCommand(CommandSet.STOP);
                prompt.sendCommand(command, new ResponseReceiver() {
                    @Override
                    public void response(ServerResponse response) {
                    }
                }); 
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        return builder.create();
    }
}
