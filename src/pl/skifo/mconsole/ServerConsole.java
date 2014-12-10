package pl.skifo.mconsole;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import pl.skifo.mconsole.CommandResponseEvaluator.EvaluatorType;
import pl.skifo.mconsole.CommandSet.SupportedSet;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class ServerConsole extends ActionBarActivity implements ActionBar.TabListener, CommandPrompt {
    
    public static final String LOG_PREFIX = "SvConsole";
    
    private static final String ERROR_MSG_KEY = "msg";
    
    private ConnDialogWrapper connDialog;
    
    private ConsoleOutput consoleOut;
    private ServerConnector connector;
    private LooperThread connectorThread;
    
    private ServerInfo svInfo;
    
    private static class LooperThread extends Thread {
        private Handler mHandler = null;
        private Looper mLooper = null;
        private boolean canEnterLoop = true;
        
        public LooperThread() {
        }

        public Handler getConnectorQueue() {
            synchronized (this) {
                if (mHandler == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {}
                }
            }
            return mHandler;
        }
        
        public void run() {
            Looper.prepare();
            synchronized (this) {
                mLooper = Looper.myLooper();
                mHandler = new Handler();
                notify();
            }
            if (canEnterLoop)
                Looper.loop();
        }
        
        public void quit() {
            synchronized (this) {
                canEnterLoop = false;
                if (mLooper != null) {
                    mLooper.quit();
                }
            }
        }
    }    
    
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "onCreate");
        
        setContentView(R.layout.activity_server_console);
        
        connector = new ServerConnector();

        // Initilization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getSupportActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
 
        viewPager.setAdapter(mAdapter);
        viewPager.setOffscreenPageLimit(2);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);        

        actionBar.addTab(actionBar.newTab().setText(R.string.tab_players)
                .setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_world)
                .setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.tab_console)
                .setTabListener(this));
        
        viewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        actionBar.setSelectedNavigationItem(position);
                    }
                });        
    }

    public synchronized void printToDefaultConsole(String txt) {
        if (consoleOut != null) {
            consoleOut.addLine(txt);
            consoleOut.refresh();
        }
    }
    
    
    @Override
    protected void onStart() {
        connectorThread = new LooperThread();
        connectorThread.start();
        super.onStart();
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "onStart");
        
        printToDefaultConsole("connecting...");

        Intent i = getIntent();
        svInfo = (ServerInfo) i.getSerializableExtra(MConsoleActivity.SERVER_DATA);
        
        connDialog = new ConnDialogWrapper(this, svInfo.getName());
        connDialog.run();
        openConnection(this, svInfo);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "onStop");
        connectorThread.quit();
        new Thread(new Runnable(){public void run(){connector.disconnect();}}).start();
    }




    @Override
    protected void onPause() {
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "onResume");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "onRestart");
        super.onRestart();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog ret = null;
        switch (id) {
            case UNKNOWN_HOST_DIALOG__ID: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.unknown_host_error)
                .setTitle(R.string.error);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
                ret = builder.create();
                break;
            }
            case CONNECTION_ERROR_DIALOG__ID: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.connection_error)
                .setTitle(R.string.error);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
                ret = builder.create();
                break;
            }
            case WRONG_PASSWORD_DIALOG__ID: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.password_error)
                .setTitle(R.string.error);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
                ret = builder.create();
                break;
            }
        }
        return ret;
    }


    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        super.onPrepareDialog(id, dialog, args);
        String baseMsg = null;
        boolean updateDialog = false;
        switch (id) {
            case UNKNOWN_HOST_DIALOG__ID:
                baseMsg = getString(R.string.unknown_host_error);
                updateDialog = true;
                break;
            case CONNECTION_ERROR_DIALOG__ID: {
                baseMsg = getString(R.string.connection_error);
                updateDialog = true;
                break;
            }
        }
        if (updateDialog) {
            AlertDialog d = (AlertDialog)dialog;
            while (true) {
                if (args != null) {
                    String msg = args.getString(ERROR_MSG_KEY);
                    if (msg != null) {
                        d.setMessage(baseMsg + " <"+msg+">");
                        break;
                    }
                }
                d.setMessage(baseMsg);
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.server_console_actions, menu);
        MenuItem mi = menu.findItem(R.id.action_send_report);
        mi.setVisible(MConsoleActivity.LOG_REPORT);
        return super.onCreateOptionsMenu(menu);
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(MConsoleActivity.LOG_PREFIX, "onOptionsItemSelected: "+item.getTitle());

        int id = item.getItemId();
        if (id == R.id.action_say) {
            DialogFragment dialog = new SayDialog(this);
            dialog.show(getSupportFragmentManager(), "SayDialog");
        }
        else if (id == R.id.action_banlist) {
            sendCommand(CommandSet.getCommand(CommandSet.BANLIST), new BanlistReceiver(this));
        }
        else if (id == R.id.action_save_all) {
            sendCommand(CommandSet.getCommand(CommandSet.SAVE_ALL), new ResponseToastGenerator(this, new CommandResponseEvaluator(EvaluatorType.save), 
                                                               R.string.server_admin_save_ok,
                                                               R.string.server_admin_save_failed));
        }
        else if (id == R.id.action_stop) {
            DialogFragment dialog = new ConfirmDialog(this);
            dialog.show(getSupportFragmentManager(), "ConfirmDialog");
        }
        else if (id == R.id.action_send_report) {
            MConsoleActivity.sendReport(this);
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void reportError(ConnResult err, String details) {
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(MConsoleActivity.LOG_PREFIX, "report error " + err);
        connDialog.dismiss();
        runOnUiThread(new ConnectionResult(this, err, details));
    }

    private void reportSuccess() {
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(MConsoleActivity.LOG_PREFIX, "connected to host");
        connDialog.dismiss();
        printToDefaultConsole("connected to "+svInfo.getAddress());
    }
    
    private void postLoginCommands() {
        sendCommand("plugins", new PluginsReceiver(this));
    }
    
    public void refreshPlayers() {
        mAdapter.refreshPlayers();
    }
    
    private class ConnDialogWrapper implements Runnable {

        private ProgressDialog d;
        private boolean started = false;
        private boolean alreadyDissmised = false;
        
        private ServerConsole ctx;
        private String serverName;
        
        public ConnDialogWrapper(ServerConsole c, String name) {
            ctx = c;
            serverName = name;
        }
        
        @Override
        public void run() {
            synchronized(this) {
                if (!alreadyDissmised) {
                    d = ProgressDialog.show(ctx, serverName, "connecting", true, false);
                    d.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(MConsoleActivity.LOG_PREFIX, "dialog canceled!");                                
                        }
                    });
                    started = true;
                }
            }
        }
        
        public void dismiss() {
            synchronized(this) {
                if (!alreadyDissmised) {
                    alreadyDissmised = true;
                    if (started)
                        d.dismiss();
                }
            }
        }
    }


    public synchronized void registerDefaultConsoleOutput(ConsoleOutput dc) {
        consoleOut = dc;
    }
    
    private void openConnection(ServerConsole serverConsole, ServerInfo svInfo) {
        connectorThread.getConnectorQueue().post(new OpenConnection(this, connector, svInfo.getAddress(), svInfo.getPort(), svInfo.getPassword()));
    }

    
    @Override
    public void sendCommand(String command, ResponseReceiver receiver) {
        connectorThread.getConnectorQueue().post(new OutMessage(connector, command, receiver));
    }
    
    private static class OutMessage implements Runnable {

        private ServerConnector connector;
        private ResponseReceiver out;
        private String msg;
        private ServerResponse response;
        
        public OutMessage(ServerConnector connector, String message, ResponseReceiver receiver) {
            this.connector = connector;
            msg = message;
            this.out = receiver;
        }
        
        
        @Override
        public void run() {
            try {
                connector.sendCommand(msg);
                response = connector.getResponse();
                out.response(response);
//                out.addBlock(response.getResponseBlock());
//                out.postInvalidate();
                if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(MConsoleActivity.LOG_PREFIX, response.toString());
            } catch (IOException e) {
                if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(MConsoleActivity.LOG_PREFIX, "TODO: connection lost, reconnect?");
                e.printStackTrace();
            }
        }
    }
    
    private enum ConnResult {
        UNKNOWN_HOST,
        IO_ERROR,
        WRONG_PASSWORD
    };
    
    static private final int UNKNOWN_HOST_DIALOG__ID = 1;
    static private final int CONNECTION_ERROR_DIALOG__ID = 2;
    static private final int WRONG_PASSWORD_DIALOG__ID = 3;
    
    private static class ConnectionResult implements Runnable {

        private ConnResult res;
        private ServerConsole ctx;
        private Bundle b;
        
        public ConnectionResult(ServerConsole serverConsole, ConnResult r, String detailedMsg) {
            res = r;
            ctx = serverConsole;
            b = new Bundle();
            b.putString(ERROR_MSG_KEY, detailedMsg);
            if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(MConsoleActivity.LOG_PREFIX, "connection result created");
        }
        
        
        @Override
        public void run() {
            if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(MConsoleActivity.LOG_PREFIX, "connection result run");
            switch (res) {
                case UNKNOWN_HOST: {
                    ctx.showDialog(UNKNOWN_HOST_DIALOG__ID, b);
                    break;
                }
                case IO_ERROR: {
                    ctx.showDialog(CONNECTION_ERROR_DIALOG__ID, b);
                    break;
                }
                case WRONG_PASSWORD: {
                    ctx.showDialog(WRONG_PASSWORD_DIALOG__ID, null);
                    break;
                }
            }
        }
    }
    
    private static class OpenConnection implements Runnable {
        
        private ServerConnector connector;
        private String serverName;
        private int port;
        private String password;
        private ServerConsole ctx;
        
        public OpenConnection(ServerConsole serverConsole, ServerConnector connector, String serverName, int port, String password) {
            this.connector = connector;
            this.serverName = serverName;
            this.port = port;
            this.password = password;
            ctx = serverConsole;
        }

        
        @Override
        public void run() {
            try {
                connector.connect(serverName, port);
                boolean loginOK = connector.login(password);
                if (loginOK) {
                    ctx.postLoginCommands();
                    ctx.reportSuccess();
                }
                else {
                    ctx.reportError(ConnResult.WRONG_PASSWORD, null);
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
                ctx.reportError(ConnResult.UNKNOWN_HOST, e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                ctx.reportError(ConnResult.IO_ERROR, e.getMessage());
            }
        }
    }


    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
        
    }




    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
        viewPager.setCurrentItem(tab.getPosition());
    }




    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
        
    }

    private static class PluginsReceiver implements ResponseReceiver {

        private ServerConsole parent;
        
        public PluginsReceiver(ServerConsole parent) {
            this.parent = parent;
        }
        
        @Override
        public void response(ServerResponse response) {
            String plugList = response.getResponseBlock().toString();
            if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "plugins: <"+plugList+">");
            if (plugList.contains("Essentials")) { // simple "Essentials" detection
                if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "Essentials detected.");
                CommandSet.setCurrentSet(SupportedSet.ESSENTIALS);
            }
            parent.refreshPlayers();
        }
    }
    

    private static class BanlistReceiver implements ResponseReceiver {

        private ServerConsole parent;
        
        public BanlistReceiver(ServerConsole parent) {
            this.parent = parent;
        }
        
        @Override
        public void response(ServerResponse response) {
            String bList = response.getResponseBlock().toString();
            int idx = bList.indexOf("banned players:");
            if (idx >= 0) {
                String[] bl = bList.substring(idx + "banned players:".length()).split(",");
                ArrayList<String> al = new ArrayList<String>();  
                for (int i = 0; i < bl.length; i++) {
                    String s = bl[i].trim();
                    if (i == (bl.length - 1)) { 
                        idx = s.indexOf(" and ");
                        if (idx >= 0) {
                            String s0 = s.substring(0, idx);
                            if (s0.length() > 0)
                                al.add(s0);
                            s0 = s.substring(idx + " and ".length());
                            if (s0.length() > 0)
                                al.add(s0);
                        }
                        else {
                            al.add(s);
                        }
                    }
                    else {
                        al.add(s);
                    }    
                }
                if (al.size() > 0) {
                    bl = al.toArray(new String[0]);
                    Arrays.sort(bl);
                    DialogFragment dialog = new BanlistDialog(parent, bl);
                    dialog.show(parent.getSupportFragmentManager(), "BanlistDialog");
                    for (String s : bl) {
                        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "banned: <"+s+">");
                    }
                }
            }
        }
    }


}
