package pl.skifo.mconsole;

import pl.skifo.mconsole.CommandResponseEvaluator.EvaluatorType;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

public class ServerFragment extends Fragment implements OnClickListener {

    private ActionBarActivity ctx;
    private CommandPrompt prompt;
    private EditText sec;
    
    /*
     * action bar:
     * banlist/pardon
     * say
     * 
     * menu:
     * save
     * stop
     
     * Commands to be supported:
     * time add/set
     * gamerule
     * weather
     * difficulty
     * spawnpoint
     * toggledownfall
     */

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ctx = (ActionBarActivity) activity;
        if (activity instanceof CommandPrompt) {
            prompt = (CommandPrompt)activity;
        }
    }
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_server, container, false);
        
        ImageButton ib = (ImageButton)rootView.findViewById(R.id.weather_sun_button);
        ib.setOnClickListener(this);
        ib = (ImageButton)rootView.findViewById(R.id.weather_rain_button);
        ib.setOnClickListener(this);
        ib = (ImageButton)rootView.findViewById(R.id.weather_thunder_button);
        ib.setOnClickListener(this);
        ib = (ImageButton)rootView.findViewById(R.id.weather_toggle_button);
        ib.setOnClickListener(this);
        ib = (ImageButton)rootView.findViewById(R.id.time_button);
        ib.setOnClickListener(this);
        ib = (ImageButton)rootView.findViewById(R.id.time_15min_button);
        ib.setOnClickListener(this);
        ib = (ImageButton)rootView.findViewById(R.id.time_1h_button);
        ib.setOnClickListener(this);
        ib = (ImageButton)rootView.findViewById(R.id.time_dawn_button);
        ib.setOnClickListener(this);
        ib = (ImageButton)rootView.findViewById(R.id.time_noon_button);
        ib.setOnClickListener(this);
        ib = (ImageButton)rootView.findViewById(R.id.time_dusk_button);
        ib.setOnClickListener(this);
        ib = (ImageButton)rootView.findViewById(R.id.time_midnight_button);
        ib.setOnClickListener(this);
        ib = (ImageButton)rootView.findViewById(R.id.diff_peaceful_button);
        ib.setOnClickListener(this);
        ib = (ImageButton)rootView.findViewById(R.id.diff_easy_button);
        ib.setOnClickListener(this);
        ib = (ImageButton)rootView.findViewById(R.id.diff_normal_button);
        ib.setOnClickListener(this);
        ib = (ImageButton)rootView.findViewById(R.id.diff_hard_button);
        ib.setOnClickListener(this);
        sec = (EditText)rootView.findViewById(R.id.weather_seconds);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
// difficulty            
            case R.id.diff_peaceful_button:
                prompt.sendCommand(CommandSet.getCommand(CommandSet.DIFFICULTY)+"peaceful",
                                   new ResponseToastGenerator(ctx, 
                                   new CommandResponseEvaluator(EvaluatorType.difficulty),
                                   R.string.diff_set_to_peace, R.string.diff_set_failed));
                break;
            
            case R.id.diff_easy_button:
                prompt.sendCommand(CommandSet.getCommand(CommandSet.DIFFICULTY)+"easy",
                                   new ResponseToastGenerator(ctx, 
                                   new CommandResponseEvaluator(EvaluatorType.difficulty),
                                   R.string.diff_set_to_easy, R.string.diff_set_failed));
                break;
            
            case R.id.diff_normal_button:
                prompt.sendCommand(CommandSet.getCommand(CommandSet.DIFFICULTY)+"normal",
                                   new ResponseToastGenerator(ctx, 
                                   new CommandResponseEvaluator(EvaluatorType.difficulty),
                                   R.string.diff_set_to_normal, R.string.diff_set_failed));
                break;
                
            case R.id.diff_hard_button:
                prompt.sendCommand(CommandSet.getCommand(CommandSet.DIFFICULTY)+"hard",
                                   new ResponseToastGenerator(ctx, 
                                   new CommandResponseEvaluator(EvaluatorType.difficulty),
                                   R.string.diff_set_to_hard, R.string.diff_set_failed));
                break;
//weather                
            case R.id.weather_sun_button:
                weatherSet("clear", R.string.weather_set_to_clear);
                break;
            case R.id.weather_rain_button:
                weatherSet("rain", R.string.weather_set_to_rain);
                break;
            case R.id.weather_thunder_button:
                weatherSet("thunder", R.string.weather_set_to_thunder);
                break;
            case R.id.weather_toggle_button:    
                prompt.sendCommand("toggledownfall",
                        new ResponseToastGenerator(ctx, 
                        new CommandResponseEvaluator(EvaluatorType.toggledownfall),
                        R.string.toggledownfall_ok, R.string.toggledownfall_failed));
                break;

//time                
            case R.id.time_button:
                DialogFragment dialog = new TimeSetDialog(prompt);
                dialog.show(ctx.getSupportFragmentManager(), "TimeSetDialog");
                break;
            case R.id.time_15min_button:
                advanceTime(15 * 60);
                break;
            case R.id.time_1h_button:
                advanceTime(60 * 60);
                break;
            case R.id.time_dawn_button:
                setTime(0);
                break;
            case R.id.time_noon_button:
                setTime(6000);
                break;
            case R.id.time_dusk_button:
                setTime(12000);
                break;
            case R.id.time_midnight_button:
                setTime(18000);
                break;
        }
    }

    // units 0 - 24000
    // dawn = 0
    // noon = 6000
    // dusk = 12000
    // midnight = 18000
    private void setTime(int units) {
        prompt.sendCommand(CommandSet.getCommand(CommandSet.TIME) + "set "+units,
                new ResponseToastGenerator(ctx, 
                new CommandResponseEvaluator(EvaluatorType.time),
                R.string.time_set_ok, R.string.time_set_failed));
    }
    
    
    private void advanceTime(int seconds) {
        prompt.sendCommand(CommandSet.getCommand(CommandSet.TIME)+"add "+((seconds * 1000)/3600),
                new ResponseToastGenerator(ctx, 
                new CommandResponseEvaluator(EvaluatorType.time_add),
                R.string.time_set_ok, R.string.time_set_failed));
    }
    
    
    private void weatherSet(String kind, int okId) {
        String s = sec.getText().toString();
        String timeValue = "";
        try {
            int t = Integer.parseInt(s);
            timeValue = " "+t;
        }
        catch (NumberFormatException ignore){};
        
        prompt.sendCommand(CommandSet.getCommand(CommandSet.WEATHER)+kind+timeValue,
                           new ResponseToastGenerator(ctx, 
                           new CommandResponseEvaluator(EvaluatorType.weather),
                           okId, R.string.weather_set_failed));
    }
    
}
