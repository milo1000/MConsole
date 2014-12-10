package pl.skifo.mconsole;
import pl.skifo.mconsole.CommandResponseEvaluator.EvaluatorType;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;


public class UserAdminDialog extends DialogFragment {

    private String player;
    private CommandPrompt prompt;
    
    public UserAdminDialog(CommandPrompt prompt, String name) {
        this.prompt = prompt;
        player = name;
    }

    private void handleMode() {
        DialogFragment dialog = new PlayerAdminMode(prompt, player);
        dialog.show(getActivity().getSupportFragmentManager(), "PlayerAdminModeDialog");
    }

    private void handleKick() {
        prompt.sendCommand(CommandSet.getCommand(CommandSet.KICK)+player,
                            new ResponseToastGenerator(getActivity(), player,
                                    new CommandResponseEvaluator(EvaluatorType.kick),
                                    R.string.player_admin_kick_ok,
                                    R.string.player_admin_kick_fail));
    }

    private void handleOp(boolean op) {
        if (op)
            prompt.sendCommand(CommandSet.getCommand(CommandSet.OP)+player,
                    new ResponseToastGenerator(getActivity(), player,
                            new CommandResponseEvaluator(EvaluatorType.op),
                            R.string.player_admin_op_ok,
                            R.string.player_admin_op_failed));
        else
            prompt.sendCommand(CommandSet.getCommand(CommandSet.DEOP)+player,
                    new ResponseToastGenerator(getActivity(), player,
                            new CommandResponseEvaluator(EvaluatorType.deop),
                            R.string.player_admin_deop_ok,
                            R.string.player_admin_deop_failed));
    }

    private void handleBan() {
        DialogFragment dialog = new PlayerAdminBan(prompt, player, new CommandResponseEvaluator(EvaluatorType.ban));
        dialog.show(getActivity().getSupportFragmentManager(), "PlayerAdminBanDialog");
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //builder.setTitle(R.string.player_admin);
        builder.setItems(new String[]{"game mode", "kick", "op", "deop", "ban"}, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                   // The 'which' argument contains the index position
                   // of the selected item
                       switch (which) {
                           case 0: handleMode(); break;
                           case 1: handleKick(); break;
                           case 2: handleOp(true); break;
                           case 3: handleOp(false); break;
                           case 4: handleBan(); break;
                           default: Toast.makeText(getActivity(), "not implemented", Toast.LENGTH_SHORT).show();
                       }
               }
        });
        return builder.create();
    }    
}
