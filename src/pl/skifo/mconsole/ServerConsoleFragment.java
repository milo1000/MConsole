package pl.skifo.mconsole;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

public class ServerConsoleFragment extends Fragment {
    
    private CommandPrompt prompt;
    private ConsoleOutputImpl consoleOut;
    private Context ctx;
    
//    public ServerConsoleFragment() {
//    }
//    
//    public ServerConsoleFragment(Context ctx, CommandPrompt prompt) {
//        this.prompt = prompt;
//        this.ctx = ctx; 
//    }
    
    //http://s3.amazonaws.com/MinecraftSkins/PLAYERSNAMEHERE.png
    
    private AutoCompleteTextView inputLine; 
    private static final String[] COUNTRIES = new String[] {
        "ban",  //+
        "ban-ip", //+
        "banlist", //+
        "clear", //+
                 // ++++++++++++++++++++++++ debug
        "defaultgamemode", //+
        "deop", //+
        "difficulty", //+
        "effect", //+
        "enchant", //+
        "gamemode", //+
        "gamerule", //+
        "give", //+
        "help", //+
        "kick", //+
        "kill", //+
        "list", //+
        "me", //+
        "op", //+
        "pardon", //+
        "pardon-ip", //+
        "playsound", //+
        "plugins", // ----------------------------
        "reload", // --------------------------
        "save-all", //+
        "save-off", //+
        "save-on", //+
        "say", //+
        "scoreboard", //+
        "seed", // +
                // ++++++++++++++++++++++++++ setblock
                // ++++++++++++++++++++++++++ setidletimeout
                // ++++++++++++++++++++++++++ setworldspawn
        "spawnpoint", // +
        "spreadplayers", // +
        "stop", // +
        "tell", // +
                // ++++++++++++++++++++++++++ tellraw
        "testfor", // +
        "time", // +
        "timings",
        "toggledownfall", // +
        "tp", // +
        "version", // --------------------------
        "weather", // +
        "whitelist", // +
        "xp" // +        
    };
    
    
    
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ctx = activity;
        if (activity instanceof CommandPrompt) {
            prompt = (CommandPrompt)activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("SvConFrag", "onCreateView: "+savedInstanceState);
        
        View rootView = inflater.inflate(R.layout.fragment_console, container, false);
        consoleOut = (pl.skifo.mconsole.ConsoleOutputImpl) rootView.findViewById(R.id.console_output);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx,
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        inputLine = (AutoCompleteTextView)rootView.findViewById(R.id.input_line);
        inputLine.setAdapter(adapter);


        Button b = (Button) rootView.findViewById(R.id.send_button);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prompt != null)
                    prompt.sendCommand(inputLine.getText().toString(), consoleOut);
                inputLine.setText("");
            }
        });
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("SvConFrag", "onCreate: "+savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("SvConFrag", "onDestroyView");
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("SvConFrag", "onPause");
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("SvConFrag", "onResume");
    }
    

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("SvConFrag", "onStart");
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("SvConFrag", "onStop");
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewStateRestored(savedInstanceState);
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("SvConFrag", "onViewStateRestored: "+savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d("SvConFrag", "onSaveInstanceState: "+outState);
    }
}
