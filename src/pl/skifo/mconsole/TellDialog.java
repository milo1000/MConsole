package pl.skifo.mconsole;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

public class TellDialog extends DialogFragment {

    private CommandPrompt prompt;
    private EditText et;
    private String player;

    public TellDialog(CommandPrompt prompt, String name) {
        this.prompt = prompt;
        player = name;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.tell_title);
        View v = getActivity().getLayoutInflater().inflate(R.layout.tell_dialog, null);
        builder.setView(v);
        et = (EditText) v.findViewById(R.id.tell_text);
        builder.setPositiveButton(R.string.tell, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String command = CommandSet.getCommand(CommandSet.TELL)+player+" ";
                if (et != null) {
                    String msg = et.getText().toString().trim();
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
        if (dialog instanceof TellDialog) {
            TellDialog d = (TellDialog)dialog;
            d.et.setText("");
        }
    }
}
