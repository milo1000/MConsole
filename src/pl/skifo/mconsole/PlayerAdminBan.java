package pl.skifo.mconsole;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class PlayerAdminBan extends DialogFragment {

    private String player;
    private CommandPrompt prompt;
    private ResponseEvaluator eval;
    private EditText et;

    public PlayerAdminBan(CommandPrompt prompt, String name, ResponseEvaluator responseEvaluator) {
        this.prompt = prompt;
        player = name;
        eval = responseEvaluator;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.player_admin_ban);
        builder.setIcon(R.drawable.ic_action_warning);
        //builder.setTitle(R.string.player_admin_ban);
        View v = getActivity().getLayoutInflater().inflate(R.layout.ban_dialog, null);
        builder.setView(v);
        TextView tv = (TextView) v.findViewById(R.id.ban_dialog_player_name);
        et = (EditText) v.findViewById(R.id.ban_reason_text);
        tv.setText(player);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String command = CommandSet.getCommand(CommandSet.BAN)+player;
                if (et != null) {
                    String reason = et.getText().toString();
                    if (reason != null && reason.length() > 0)
                        command += " "+reason;
                }
                prompt.sendCommand(command, 
                        new ResponseToastGenerator(getActivity(), player,
                                eval,
                                R.string.player_admin_ban_ok,
                                R.string.player_admin_ban_failed));
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
        if (dialog instanceof PlayerAdminBan) {
            PlayerAdminBan d = (PlayerAdminBan)dialog;
            d.et.setText("");
        }
    }
    
    
}
