package pl.skifo.mconsole;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class PlayersFragment extends Fragment implements ResponseReceiver {

    private ActionBarActivity ctx;
    private CommandPrompt prompt;
    private TextView online_total;
    private PlayersFragment myInstance;
    private PlayerListAdapter pAdapter;
    
    private boolean lazyRefresh = false;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("PlayerFrag", "onCreateView: "+savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_players, container, false);
        online_total = (TextView)rootView.findViewById(R.id.online_total_number);
        ImageButton refreshButton = (ImageButton)rootView.findViewById(R.id.refresh_players_list);
        
        refreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                prompt.sendCommand(CommandSet.getCommand(CommandSet.LIST), new PlayerListResponse(myInstance));
            }
        });
        
        ListView lv = (ListView) rootView.findViewById(R.id.player_list);
        lv.setAdapter(pAdapter);
        lv.setItemsCanFocus(false);
        //lv.setOnItemClickListener(this);
        //prompt.sendCommand(CommandSet.getCommand(CommandSet.LIST), new PlayerListResponse(this));
        return rootView;
    }
    
    public void refreshPlayersList() {
        synchronized(this) {
            if (pAdapter != null) {
                prompt.sendCommand(CommandSet.getCommand(CommandSet.LIST), new PlayerListResponse(this));
            }
            else {
                lazyRefresh = true;
            }
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("PlayerFrag", "onCreate: "+savedInstanceState);
        myInstance = this;
        synchronized(this) {
            pAdapter = new PlayerListAdapter(ctx, this);
            if (lazyRefresh) {
                refreshPlayersList();
            }
        }
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ctx = (ActionBarActivity) activity;
        if (activity instanceof CommandPrompt) {
            prompt = (CommandPrompt)activity;
        }
    }

    @Override
    public void response(ServerResponse response) {
        // TODO Auto-generated method stub
        
    }

    private static class PlayerListInfo {
        public int total;
        public int online;
        public ArrayList<String> list = new ArrayList<String>();
    }
    
    private static class PlayerListResponse implements ResponseReceiver {
        
        private PlayersFragment parent;
        
        public PlayerListResponse(PlayersFragment ctx) {
            parent = ctx;
        }
        
        @Override
        public void response(ServerResponse response) {
            AttributedBlock r = response.getResponseBlock();
            if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("PlayerFrag", "response: "+response);
            if (r != ServerResponse.EMPTY_RESPONSE) {
                final PlayerListInfo ret = parseResponse(r.toString());
                if (ret != null) {
                    if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("PlayerFrag", "online: "+ret.online+", total: "+ret.total);
                    parent.pAdapter.updateSet(ret.list);
                    parent.ctx.runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            parent.online_total.setText(ret.online+"/"+ret.total);
                            parent.pAdapter.notifyDataSetChanged();
                        }});
                }
                else {
                    if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("PlayerFrag", "response parse error");
                }
            }
        }

        private PlayerListInfo parseResponse(String string) {
            PlayerListInfo ret = null;
            int idx = string.indexOf("There are ");
            if (idx >= 0) {
                int bIdx = "There are ".length() + idx;
                String rest = string.substring(bIdx).trim();
                idx = rest.indexOf(" ");
                String online_total = (idx > 0) ? rest.substring(0, idx) : rest;  
                idx =  online_total.indexOf("/");
                if (idx > 0) {
                    ret = new PlayerListInfo();
                    try {
                        ret.online = Integer.decode(online_total.substring(0, idx));
                    }
                    catch (NumberFormatException ignore){}
                    try {
                        ret.total = Integer.decode(online_total.substring(idx + 1));
                    }
                    catch (NumberFormatException ignore){}
                    idx = string.indexOf("online:");
                    if (idx > 0) {
                        String pList[] = string.substring(idx + "online:".length()).split(",");
                        for (String s : pList) {
//                            Log.d("PlayerFrag","add to list <"+s+">, trimmed <"+s.trim()+">");
                            String sTrim = s.trim();
                            if (sTrim.length() > 0)
                                ret.list.add(sTrim);
                        }
                        
                        // for testing, fake list
//                        ret.list.add("zenek_1_ten_bedzie_mial_dluga_nazwe_litwo_ojczyzno_moja");
//                        ret.list.add("zenek_2");
//                        ret.list.add("zenek_3");
//                        ret.list.add("zenek_4");
//                        ret.list.add("zenek_5");
//                        ret.list.add("zenek_6");
//                        ret.list.add("zenek_7");
//                        ret.list.add("zenek_8");
//                        ret.list.add("zenek_9");
//                        ret.list.add("zenek_10");
//                        ret.list.add("zenek_11");
//                        ret.list.add("zenek_12");
//                        ret.list.add("zenek_13");
//                        ret.list.add("zenek_14");
//                        ret.list.add("zenek_15");
//                        ret.list.add("zenek_16");
                        
                    }
                    
                }
            }
            return ret;
        }
    }

    void showUserAdminDialog(String name) {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new UserAdminDialog(prompt, name);
        dialog.show(ctx.getSupportFragmentManager(), "UserAdminDialog");
    }        
    
    void showUserActionDialog(String name) {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new UserActionDialog(prompt, pAdapter, name);
        dialog.show(ctx.getSupportFragmentManager(), "UserActionDialog");
    }        
}
