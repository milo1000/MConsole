package pl.skifo.mconsole;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayerListAdapter extends BaseAdapter {

    private Activity ctx;
    private LayoutInflater inflater;
    private ArrayList<String> playerList;
    private HashMap<String, Bitmap> avatars;
    PlayersFragment parentFragment;
    private AvatarUpdater avatarUpdater;
    
    public PlayerListAdapter(Activity ctx, PlayersFragment playersFragment) {
        this.ctx = ctx;
        parentFragment = playersFragment;
        inflater = ctx.getLayoutInflater();
        playerList = new ArrayList<String>();
        avatars = new HashMap<String, Bitmap>();
    }
    
    public synchronized void updateSet(ArrayList<String> playerList) {
        this.playerList = playerList;
        ArrayList<String> avatarsToUpdate = null;

        if (avatarUpdater != null) {
            avatarUpdater.stopRetrieval();
            avatarUpdater.interrupt();
            try {
                avatarUpdater.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            avatarUpdater = null;
        }
        
        HashMap<String, Bitmap> avatarsTmp = new HashMap<String, Bitmap>();
        
        for (String s : playerList) {
            Bitmap b = avatars.get(s);
            if (b == null) {
                if (avatarsToUpdate == null) {
                    avatarsToUpdate = new ArrayList<String>();
                }
                avatarsToUpdate.add(s);
            }
            else {
                avatarsTmp.put(s, b);
            }
        }

        avatars = avatarsTmp;
        if (avatarsToUpdate != null && avatarsToUpdate.size() > 0) {
            avatarUpdater = new AvatarUpdater(this, avatarsToUpdate);
            avatarUpdater.start();
        }    
    }
    
    @Override
    public int getCount() {
        int cnt = 0;
        synchronized (this) {
            cnt = playerList.size(); 
        }
        return cnt;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("PlListAdapter", "getView: ["+position+"], total in list: "+playerList.size());
        LinearLayout entry = null;
        String text = null;
        Bitmap avatar = null;
        synchronized (this) {
            if (playerList.size() > 0) {
                text = playerList.get(position);
                avatar = avatars.get(text);
            }
        }
        
        if (text != null) {
            entry = (LinearLayout) inflater.inflate(R.layout.player_list_row, null);
            if (entry != null) {
                TextView tv = (TextView) entry.findViewById(R.id.player_name_in_list);
                PlayerAdminListener l = new PlayerAdminListener(parentFragment, text);
                tv.setText(text);
                tv.setOnClickListener(l);
                ImageButton ib = (ImageButton) entry.findViewById(R.id.player_admin);
                ib.setOnClickListener(l);
                if (avatar != null) {
                    ImageView iv = (ImageView) entry.findViewById(R.id.player_face);
                    BitmapDrawable bd = new BitmapDrawable(ctx.getResources(), avatar);
                    bd.setAntiAlias(false);
                    bd.setDither(false);
                    bd.setFilterBitmap(false);
                    iv.setImageDrawable(bd);
                }
            }
        }
        return entry;
    }
    
    private static class PlayerAdminListener implements OnClickListener {
        
        private String name;
        private PlayersFragment parentFragment;
        
        public PlayerAdminListener(PlayersFragment parentFragment, String playerName) {
            name = playerName;
            this.parentFragment = parentFragment;
        }

        @Override
        public void onClick(View v) {
            if (v instanceof TextView) {
                parentFragment.showUserActionDialog(name);
            }
            else if (v instanceof ImageButton) {
                parentFragment.showUserAdminDialog(name);
            }
        }
    }

    private static class AvatarUpdater extends Thread {

        private ArrayList<String> updateList;
        private PlayerListAdapter parent;
        private boolean retrievalAborted = false;
        
        public AvatarUpdater(PlayerListAdapter playerListAdapter, ArrayList<String> avatarsToUpdate) {
            parent = playerListAdapter;
            updateList = avatarsToUpdate;
        }

        @Override
        public void run() {
            for (String s : updateList) {
                try {
                    InputStream in = (InputStream) new URL("http://s3.amazonaws.com/MinecraftSkins/"+s+".png").getContent();
                    if (in != null) {
                        Bitmap b = BitmapFactory.decodeStream(in);
                        synchronized(parent) {
                            parent.avatars.put(s, Bitmap.createBitmap(b, 8, 8, 8, 8));
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            synchronized(parent) {
                if (!retrievalAborted) {
                    parent.ctx.runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            parent.notifyDataSetChanged();
                        }});
                }
            }
        }

        public void stopRetrieval() {
            retrievalAborted = true;
        }
    }

    public String[] getPlayers() {
        String[] ret = new String[0];
        synchronized(this) {
            ret = playerList.toArray(ret);
        }
        return ret;
    }
    
}
