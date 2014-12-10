package pl.skifo.mconsole;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class PlayerAdminMode extends DialogFragment {

    private String player;
    private CommandPrompt prompt;
    
    private enum GameMode {
        survival,
        creative,
        adventure,
        unknown
    };
    
    private GameMode mode = GameMode.unknown;
    
    public PlayerAdminMode(CommandPrompt prompt, String name) {
        this.prompt = prompt;
        player = name;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //builder.setTitle(R.string.player_admin_gamemode);
        builder.setSingleChoiceItems(new String[]{"Survival", "Creative", "Adventure"}, -1, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface parent, int which) {
                switch (which) {
                    case 0: mode = GameMode.survival; break;
                    case 1: mode = GameMode.creative; break;
                    case 2: mode = GameMode.adventure; break;
                    default: mode = GameMode.unknown;
                }
            }});
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (mode != GameMode.unknown) {
                    if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("PlayerAdminMode", "mode changed to: "+mode);
                    prompt.sendCommand(CommandSet.getCommand(CommandSet.GAMEMODE)+mode+" "+player, 
                                        new ResponseToastGenerator(getActivity(), player,
                                            new ModeResponseEvaluator(),
                                            R.string.player_admin_gamemodechanged,
                                            R.string.player_admin_gamemodefailed));
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
    
    private static class ModeResponseEvaluator implements ResponseEvaluator {

        @Override
        public boolean isOK(ServerResponse response) {
            return (response.getResponseBlock() == ServerResponse.EMPTY_RESPONSE);
        }
    }
    
}
