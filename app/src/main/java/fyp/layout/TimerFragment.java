package fyp.layout;

import static com.google.common.base.Preconditions.checkState;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import fyp.layout.TimerService.TimerListener;

public class TimerFragment extends DialogFragment {
    private TimerListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        checkState(
                getTargetFragment() instanceof TimerListener,
                "Target fragment is not instance of TimerListener");

        mListener = (TimerListener) getTargetFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.timer, null);
        final NumberPicker timerHours = (NumberPicker) view.findViewById(R.id.hours_picker);
        final NumberPicker timerMinutes = (NumberPicker) view.findViewById(R.id.minutes_picker);
        final NumberPicker timerSeconds = (NumberPicker) view.findViewById(R.id.seconds_picker);

        final TimerValues values;

        if (getArguments() != null) {
            values = TimerValues.fromBundle(getArguments());
        } else {
            values = new TimerValues(0 /* hours */, 0 /* minutes */, 0 /* seconds */);
        }

        values.configureHours(timerHours);
        values.configureMinutes(timerMinutes);
        values.configureSeconds(timerSeconds);

        builder.setTitle(R.string.timer_title);
        builder.setView(view);
        builder.setPositiveButton(
                R.string.timer_set,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.processTimerValues(
                                new TimerValues(
                                        timerHours.getValue(), timerMinutes.getValue(), timerSeconds.getValue()));
                    }
                });
        builder.setNeutralButton(
                R.string.timer_cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.processTimerValues(values);
                    }
                });
        builder.setNegativeButton(
                R.string.timer_reset,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.processTimerValues(
                                new TimerValues(0 /* hours */, 0 /* minutes */, 0 /* seconds */));
                    }
                });

        return builder.create();
    }

}
