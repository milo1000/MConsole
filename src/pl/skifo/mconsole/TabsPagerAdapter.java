package pl.skifo.mconsole;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    private ServerConsoleFragment svFrag;
    private PlayersFragment plFrag;
    
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
        svFrag = new ServerConsoleFragment();
        plFrag = new PlayersFragment();
    }
    
    @Override
    public Fragment getItem(int item) {
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("TabsPagerAdapter", "getItem<"+item+">");
        switch (item) {
            case 2: return svFrag;
            case 0: return plFrag;
            case 1: return new ServerFragment();
        }
        return new ServerConsoleFragment();
    }

    @Override
    public int getCount() {
        return 3;
    }
    
    public void refreshPlayers() {
        plFrag.refreshPlayersList();
    }
    
}
