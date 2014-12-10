package pl.skifo.mconsole;

import java.util.Calendar;

import pl.skifo.mconsole.CommandResponseEvaluator.EvaluatorType;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimeSetDialog extends DialogFragment implements OnTimeSetListener {

    private CommandPrompt prompt;

    public TimeSetDialog(CommandPrompt prompt) {
        this.prompt = prompt;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        int timeToSet =  (hourOfDay * 1000 + (minute * 1000)/60 + 18000) % 24000;
        prompt.sendCommand(CommandSet.getCommand(CommandSet.TIME) + "set "+timeToSet, new ResponseToastGenerator(getActivity(), 
                                                  new CommandResponseEvaluator(EvaluatorType.time),
                                                  R.string.time_set_ok, R.string.time_set_failed));
    }
}