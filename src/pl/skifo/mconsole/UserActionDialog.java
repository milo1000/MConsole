package pl.skifo.mconsole;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;


public class UserActionDialog extends DialogFragment {

    private String player;
    private CommandPrompt prompt;
    private PlayerListAdapter pAdapter;
    
    public UserActionDialog(CommandPrompt prompt, PlayerListAdapter pAdapter, String name) {
        this.prompt = prompt;
        this.pAdapter = pAdapter;
        player = name;
    }
    
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //builder.setTitle(R.string.player_action);
        builder.setItems(new String[]{"teleport", "tell"}, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                       switch (which) {
                       case 0: handleTeleport(); break;
                       case 1: handleTell(); break;
                       //case 2: handleGiveXp(); break;
                       default: Toast.makeText(getActivity(), "not implemented", Toast.LENGTH_SHORT).show();
                   }
               }
        });
        return builder.create();
    }    

    private void handleTeleport() {
        String[] players = pAdapter.getPlayers();
        DialogFragment dialog = new PlayerActionTp(prompt, player, players);
        dialog.show(getActivity().getSupportFragmentManager(), "PlayerActionTpDialog");
    }
    
//    private void handleGiveXp() {
//    }
    
    private void handleTell() {
        DialogFragment dialog = new TellDialog(prompt, player);
        dialog.show(getActivity().getSupportFragmentManager(), "PlayerActionTellDialog");
    }
}
