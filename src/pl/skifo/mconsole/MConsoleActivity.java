package pl.skifo.mconsole;

//import com.google.ads.AdView;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//public class MConsoleActivity extends ActionBarActivity {
public class MConsoleActivity extends Activity {

    public static final boolean LOG_DEBUG = true;
    public static final boolean LOG_REPORT = true;
    
    public static final String SERVER_DATA = "pl.skifo.mconsole.ServerInfo";
    
    public static final String LOG_PREFIX = "MConA";
    private static final String SERVER_LIST_PREF = "slist";
    private static final String RECENT_SERVER_PREF = "idx";
    
    private static final int SERVER_EDIT_DIALOG__ID = 0x1010;
    private static final int SERVER_ADD_DIALOG__ID =  0x1011;
    private static final int SERVER_REMOVE_DIALOG__ID = 0x1210;
    private static final int PASSWORD_DIALOG__ID =  0x1310;
    private static final int WHATS_NEW_DIALOG__ID = 0x1410;
    
    private static final char SPLIT_SEQUENCE = '|';
    
    private ArrayAdapter<ServerInfo> serverInfo;
    private Spinner connSelector;
    private ServerInfo currentlyEditedServer;
    
    private MConsoleActivity myContext;
    
    private ArrayAdapter<ServerInfo> createServerInfo(String serverList) {
        
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "create server list <"+serverList+">");
        
        ArrayAdapter<ServerInfo> adapter;
        adapter = new ArrayAdapter<ServerInfo>(this, android.R.layout.simple_spinner_item);
        
        TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(SPLIT_SEQUENCE);
        splitter.setString(serverList);
        
        for (String info : splitter) {
            if (info.length() > 0) {
                String[] serverData = info.split(",");
                if (serverData.length > 0) {
                    ServerInfo svInfo = new ServerInfo(serverData[0]);
                    if (serverData.length > 1 && serverData[1].length() > 0) {
                        svInfo.setEncodedName(serverData[1]);
                    }
                    if (serverData.length > 2 && serverData[2].length() > 0) {
                        try {
                            svInfo.setPort(Integer.parseInt(serverData[2]));
                        }
                        catch (NumberFormatException ignore) {}
                    }
                    if (serverData.length > 3 && serverData[3].length() > 0) {
                        svInfo.setEncodedPassword(serverData[3]);
                    }
                    adapter.add(svInfo);
                }
            }
        }
        return adapter;
    }

    
    private void saveServerData(ArrayAdapter<ServerInfo> list) {
        int cnt = list.getCount();
        if (cnt > 0) {
            StringBuilder out = new StringBuilder();
            int i = 0;
            for (; i < cnt - 1; i++) {
                out.append(list.getItem(i).toExternalForm());
                out.append(SPLIT_SEQUENCE);
            }
            out.append(list.getItem(i).toExternalForm());
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            Editor e = prefs.edit();
            e.putString(SERVER_LIST_PREF, out.toString());
            e.commit();
            if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "saved list <"+out.toString()+">");
        }
        else if (cnt == 0) {
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            Editor e = prefs.edit();
            e.remove(SERVER_LIST_PREF);
            e.commit();
            if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "server list removed");
        }
    }
    
    private void setCurrentlyEditedServer() {
        if (connSelector != null) {
            int cnt = connSelector.getCount();
            if (cnt > 0) {
                currentlyEditedServer = (ServerInfo) connSelector.getSelectedItem();
                if (currentlyEditedServer == null) {
                    currentlyEditedServer = (ServerInfo) connSelector.getItemAtPosition(0);
                }
            }
            else {
                currentlyEditedServer = null;
            }
        }
        else {
            currentlyEditedServer = null;
        }
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mconsole);

        //getSupportActionBar().hide();
        
        myContext = this;
        connSelector = (Spinner) findViewById(R.id.spinner1);

        // pref: server "address,name:optional,port:optional,password:optional|,,,"
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        
        serverInfo = createServerInfo(prefs.getString(SERVER_LIST_PREF, ""));
        serverInfo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serverInfo.setNotifyOnChange(true);
        
        connSelector.setAdapter(serverInfo);
        int recentIdx = prefs.getInt(RECENT_SERVER_PREF, 0);
        if (recentIdx >= connSelector.getCount() || recentIdx < 0) {
            recentIdx = 0;
        }
        connSelector.setSelection(recentIdx);
        
        Button add = (Button) findViewById(R.id.add_button);
        Button connect = (Button) findViewById(R.id.connect_button);
        Button edit = (Button) findViewById(R.id.edit_button);
        Button remove = (Button) findViewById(R.id.remove_button);
        
        connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ServerInfo svInfo = (ServerInfo) connSelector.getSelectedItem();
                if (svInfo == null) {
                    int cnt = serverInfo.getCount();
                    if (cnt > 0) {
                        svInfo = (ServerInfo) connSelector.getItemAtPosition(0);
                    }
                }
                if (svInfo != null) {
                    if ("".equals(svInfo.getPassword())){
                        currentlyEditedServer = svInfo;
                        showDialog(PASSWORD_DIALOG__ID, null);
                    }
                    else
                        startConsole(svInfo);
                }
                else {
                    if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "no server in list");
                    // add server
                    currentlyEditedServer = new ServerInfo(); 
                    showDialog(SERVER_ADD_DIALOG__ID, null);
                }
                
            }
        });
        
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                currentlyEditedServer = new ServerInfo(); 
                showDialog(SERVER_ADD_DIALOG__ID, null);
            }
        });        

        edit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setCurrentlyEditedServer();
                if (currentlyEditedServer != null) {
                    showDialog(SERVER_EDIT_DIALOG__ID, null);
                }
            }
        });        

        remove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setCurrentlyEditedServer();
                if (currentlyEditedServer != null) {
                    showDialog(SERVER_REMOVE_DIALOG__ID, null);
                }
            }
        });        
        
        int vCode = prefs.getInt("vCode", -1);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (info.versionCode != vCode) {
                SharedPreferences.Editor e = prefs.edit();
                e.putInt("vCode", info.versionCode);
                e.commit();
                showDialog(WHATS_NEW_DIALOG__ID, null);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }        
        
        
    }
    
    @Override
    protected void onStop() {
        int recentIdx = connSelector.getSelectedItemPosition();
        if (recentIdx != AdapterView.INVALID_POSITION) {
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            Editor e = prefs.edit();
            e.putInt(RECENT_SERVER_PREF, recentIdx);
            e.commit();
        }
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return LOG_REPORT;
    }

    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_report: {
                sendReport(this);
                break;
            }
        }
        return false;
    }


    private void removeServer() {
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "removing: "+currentlyEditedServer);
        if (currentlyEditedServer != null) {
            serverInfo.remove(currentlyEditedServer);
        }
    }
    
    private void updateServerList(String addr, String n, String p, boolean resetPasswd) {
        
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "update list: addr = "+addr+", n = "+n+", p = "+p);
        
        if (currentlyEditedServer != null && addr != null && addr.length() > 0) {
            currentlyEditedServer.setAddress(addr);
            int pos = serverInfo.getPosition(currentlyEditedServer);
            if (pos < 0) {
             // new elem
                serverInfo.add(currentlyEditedServer);
                int sel = connSelector.getCount();
                connSelector.setSelection(sel - 1);
            }
            currentlyEditedServer.setName((n == null || n.length() == 0) ? addr:n);
            int port = ServerInfo.DEFAULT_PORT;
            if (p != null && p.length() > 0) {
                try {
                    port = Integer.parseInt(p);
                }
                catch (NumberFormatException ignore) {}
            }
            currentlyEditedServer.setPort(port);
            if (resetPasswd)
                currentlyEditedServer.setPassword("");
            serverInfo.notifyDataSetChanged();
            if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "currentlyEditedServer = <"+currentlyEditedServer.toExternalForm()+">");
        }
    }
    
    
    @Override
    protected void onPrepareDialog (int id, Dialog dialog, Bundle args) {
        super.onPrepareDialog(id, dialog, args);
        if (currentlyEditedServer == null) {
            setCurrentlyEditedServer();
            if (currentlyEditedServer == null) {// FATAL - shall never happen
                Thread.dumpStack();
                return;
            }
        }
        if (id == SERVER_EDIT_DIALOG__ID) {
            AlertDialog d = (AlertDialog) dialog;
            EditText addr = (EditText) d.findViewById(R.id.server_address_edit);
            EditText name = (EditText) d.findViewById(R.id.server_name_edit);
            EditText port = (EditText) d.findViewById(R.id.server_port_edit);
            addr.requestFocus();
            
            int pos = serverInfo.getPosition(currentlyEditedServer);
            if (pos >= 0) {
                //edit
                String a = currentlyEditedServer.getAddress();
                addr.setText(a);
                String n = currentlyEditedServer.getName();
                name.setText((n == ServerInfo.DEFAULT_ADDRESS || n.equals(a)) ? "":n);
                int p = currentlyEditedServer.getPort();
                port.setText((p == ServerInfo.DEFAULT_PORT) ? "":p+"");
                if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "a = "+a+", n = "+n+", p = "+p);
            }
            else {
                // cleanup
                addr.setText("");
                name.setText("");
                port.setText("");
            }
        }
        else if (id == SERVER_REMOVE_DIALOG__ID) {
            AlertDialog d = (AlertDialog) dialog;
            TextView name = (TextView) d.findViewById(R.id.server_name_delete);
            name.setText(currentlyEditedServer.getName());
        }
    }
    
    
    @Override
    protected Dialog onCreateDialog (int id, Bundle args) {
        
        Dialog ret = null;
        switch (id) {
            case SERVER_ADD_DIALOG__ID: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.server);
                LayoutInflater inflater = getLayoutInflater();
                builder.setView(inflater.inflate(R.layout.add_server_dialog, null));
                builder.setPositiveButton(R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
    
                builder.setNegativeButton(R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    
                final AlertDialog ad = builder.create();
                ad.setOnShowListener(new DialogInterface.OnShowListener() {
    
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button b = ad.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {
    
                            @Override
                            public void onClick(View view) {
                                AlertDialog d = (AlertDialog) dialog;
                                EditText addr = (EditText) d.findViewById(R.id.server_address_add);
                                String a = addr.getText().toString();
                                EditText name = (EditText) d.findViewById(R.id.server_name_add);
                                String n = name.getText().toString();
                                EditText port = (EditText) d.findViewById(R.id.server_port_add);
                                String p = port.getText().toString();
    
                                if (a == null || a.length() == 0) {
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(myContext, R.string.server_address_alert, duration);
                                    toast.show();
                                }
                                else {
                                    updateServerList(a, n, p, true);
                                    saveServerData(serverInfo);
                                    ad.dismiss();
                                }
                            }
                        });                        
                    }
                });
                ret = ad;                
                break;
            }
            case SERVER_EDIT_DIALOG__ID: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.server);
                LayoutInflater inflater = getLayoutInflater();
                builder.setView(inflater.inflate(R.layout.edit_server_dialog, null));
                builder.setPositiveButton(R.string.ok, new OnClickListener() {
    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
    
    
                builder.setNegativeButton(R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    
                final AlertDialog ad = builder.create();
                ad.setOnShowListener(new DialogInterface.OnShowListener() {
    
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button b = ad.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {
    
                            @Override
                            public void onClick(View view) {
                                AlertDialog d = (AlertDialog) dialog;
                                EditText addr = (EditText) d.findViewById(R.id.server_address_edit);
                                String a = addr.getText().toString();
                                EditText name = (EditText) d.findViewById(R.id.server_name_edit);
                                String n = name.getText().toString();
                                EditText port = (EditText) d.findViewById(R.id.server_port_edit);
                                String p = port.getText().toString();
    
                                if (a == null || a.length() == 0) {
                                    int duration = Toast.LENGTH_SHORT;
                                    Toast toast = Toast.makeText(myContext, R.string.server_address_alert, duration);
                                    toast.show();
                                }
                                else {
                                    CheckBox pass = (CheckBox) d.findViewById(R.id.reset_password);
                                    updateServerList(a, n, p, pass.isChecked());
                                    saveServerData(serverInfo);
                                    ad.dismiss();
                                }
                            }
                        });                        
                    }
                });
                ret = ad;                
                break;
            }
    
            case SERVER_REMOVE_DIALOG__ID: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.server_delete);
                LayoutInflater inflater = getLayoutInflater();
                builder.setView(inflater.inflate(R.layout.remove_server_dialog, null));
                builder.setPositiveButton(R.string.ok, new OnClickListener() {
    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "removing server...");
                        removeServer();
                        saveServerData(serverInfo);
                    }
                });
    
    
                builder.setNegativeButton(R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    
                ret = builder.create();                
                break;
            }

            case PASSWORD_DIALOG__ID: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.enter_password);
                LayoutInflater inflater = getLayoutInflater();
                builder.setView(inflater.inflate(R.layout.enter_password_dialog, null));
                builder.setPositiveButton(R.string.ok, new OnClickListener() {
    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog d = (AlertDialog) dialog;
                        EditText pass = (EditText) d.findViewById(R.id.server_password_edit);
                        String passwd = pass.getText().toString();
                        if (currentlyEditedServer != null) {
                            ServerInfo svInfo = currentlyEditedServer;
                            svInfo.setPassword(passwd);
                            currentlyEditedServer = null; 
                            
                            CheckBox check = (CheckBox) d.findViewById(R.id.store_password);
                            if (check.isChecked()) {
                                saveServerData(serverInfo);
                            }
                            startConsole(svInfo);
                            check.setChecked(false);
                        }
                        pass.setText("");
                    }
                });
    
    
                builder.setNegativeButton(R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog d = (AlertDialog) dialog;
                        EditText pass = (EditText) d.findViewById(R.id.server_password_edit);
                        CheckBox check = (CheckBox) d.findViewById(R.id.store_password);
                        check.setChecked(false);
                        pass.setText("");
                        dialog.dismiss();
                    }
                });
    
                ret = builder.create();                
                break;
            }
            
            case WHATS_NEW_DIALOG__ID: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();
                View whatsNew = inflater.inflate(R.layout.whats_new, null);
                builder.setView(whatsNew);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((Dialog)dialog).dismiss();
                    }
                });
                ret = builder.create();                
            }
            break;
            
            default: break;
        }


        return ret;
    }
    
    private void startConsole(ServerInfo svInfo) {
        Intent startConsole = new Intent("pl.skifo.mconsole.action.START_CONSOLE");
        startConsole.putExtra(SERVER_DATA, svInfo);
        startConsole.setClassName("pl.skifo.mconsole", "pl.skifo.mconsole.ServerConsole");
        startActivity(startConsole);
    }    
    

    private static ArrayList<String> report = new ArrayList<String>();
    private static int maxReportLines = 10000;    
    public static void doLog(String prefix, String msg) {
        Log.d(prefix, msg);
        if (LOG_REPORT) {
            synchronized(report) {
                if (report.size() < maxReportLines) {
                    report.add("["+prefix+"]"+msg);
                }
            }
        }
    }
    
    public static void sendReport(Activity ctx) {
        Intent send = new Intent(Intent.ACTION_SENDTO);
        StringBuilder sb = new StringBuilder();
        synchronized (report) {
            for (String s:report) {
                sb.append(s);
                sb.append("\n");
            }
        }
        
        String uriText = "mailto:" + Uri.encode("someone@gmail.com") + 
                  "?subject=" + Uri.encode("MConsole report") + 
                  "&body=" + Uri.encode(sb.toString());
        Uri uri = Uri.parse(uriText);

        send.setData(uri);
        ctx.startActivity(Intent.createChooser(send, "Send mail..."));        
    }


    public static void d(String tag, String msg) {
        doLog(tag, msg);
    }
}
