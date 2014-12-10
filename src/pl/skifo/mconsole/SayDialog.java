package pl.skifo.mconsole;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

public class SayDialog extends DialogFragment {

    private CommandPrompt prompt;
    private EditText et;

    public SayDialog(CommandPrompt prompt) {
        this.prompt = prompt;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //builder.setTitle(R.string.player_admin_ban);
        //builder.setIcon(R.drawable.ic_action_warning);
        builder.setTitle(R.string.say_title);
        View v = getActivity().getLayoutInflater().inflate(R.layout.say_dialog, null);
        builder.setView(v);
        et = (EditText) v.findViewById(R.id.say_text);
        builder.setPositiveButton(R.string.say, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String command = CommandSet.getCommand(CommandSet.SAY);
                if (et != null) {
                    String msg = et.getText().toString();
                    if (msg != null && msg.length() > 0)
                        command += msg;
                    prompt.sendCommand(command, new SimpleToastResponseReceiver(getActivity(), msg));
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
        if (dialog instanceof SayDialog) {
            SayDialog d = (SayDialog)dialog;
            d.et.setText("");
        }
    }
}
