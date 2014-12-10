package pl.skifo.mconsole;

import pl.skifo.mconsole.CommandResponseEvaluator.EvaluatorType;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class PlayerActionTp extends DialogFragment {

    private String player;
    private String[] players;
    private CommandPrompt prompt;
    private String destPlayer;
    
    public PlayerActionTp(CommandPrompt prompt, String name, String[] players) {
        this.prompt = prompt;
        player = name;
        this.players = players;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.player_action_teleport_to);
        builder.setSingleChoiceItems(players, -1, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface parent, int which) {
                destPlayer = players[which];
            }});
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (destPlayer != null) {
                    prompt.sendCommand(CommandSet.getCommand(CommandSet.TP)+player+" "+destPlayer, 
                                        new ResponseToastGenerator(getActivity(), player,
                                            new CommandResponseEvaluator(EvaluatorType.teleport),
                                            R.string.teleport_ok,
                                            R.string.teleport_failed));
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
}
