package fyp.recorder;

//import android.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.TextView;

import fyp.layout.R;


public class LogFragment extends Fragment implements MainActivityListener {

    View myView;
    private static final String TIMER_FRAGMENT_TAG = "timer";

    TextView logView, timertv;
    ScrollView logScroll;

    Button startLog, stopLog, timer, clear;

    CheckBox autoScroll;
    boolean isAutoScroll = true;

    private LoggerUI loggerUI;
    private LoggerFile loggerFile;

    private final UIFragmentComponent mUiComponent = new UIFragmentComponent();

    public void setLoggerFile(LoggerFile value) {
        loggerFile = value;
    }

    public void setUILogger(LoggerUI value) {
        loggerUI = value;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.log_layout, container, false);

        MainActivity.getInstance().addListener(this);

        logView = (TextView) myView.findViewById(R.id.log_view);
        timertv = (TextView) myView.findViewById(R.id.timer_display);

        logScroll = (ScrollView) myView.findViewById(R.id.log_scroll);

        startLog = (Button) myView.findViewById(R.id.start_logs);
        stopLog = (Button) myView.findViewById(R.id.send_file);
        timer = (Button) myView.findViewById(R.id.timer);
        clear = (Button) myView.findViewById(R.id.clear);

        autoScroll = (CheckBox) myView.findViewById(R.id.autoScroll);

        LoggerUI currentUiLogger = loggerUI;
        loggerUI = new LoggerUI();
        if (currentUiLogger != null) {
            currentUiLogger.setUiFragmentComponent(mUiComponent);
        }

        LoggerFile currentFileLogger = loggerFile;
        loggerFile = new LoggerFile(getContext());
        if (currentFileLogger != null) {
            currentFileLogger.setUiComponent(mUiComponent);
        }

        startedLog(false);

        startLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startedLog(true);
                MainActivity.getInstance().startNewLogging();
            }
        });

        stopLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startedLog(false);
                MainActivity.getInstance().stopLogging();
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logView.setText("");
            }
        });

        autoScroll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isAutoScroll = true;
                } else {
                    isAutoScroll = false;
                }
                ;
            }
        });

        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //launchTimerDialog();
            }
        });


        return myView;
    }

    public void startedLog(boolean started) {
        startLog.setEnabled(!started);
        stopLog.setEnabled(started);
        timer.setEnabled(!started);
    }

    @Override
    public void gpsStart() {

    }

    @Override
    public void gpsStop() {

    }

    @Override
    public void onGnssFirstFix(int ttffMillis) {

    }

    @Override
    public void onSatelliteStatusChanged(GnssStatus status) {

    }

    @Override
    public void onGnssStarted() {

    }

    @Override
    public void onGnssStopped() {

    }

    @Override
    public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {

    }

    @Override
    public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {

    }

    @Override
    public void onNmeaReceived(long l, String s) {

    }

    @Override
    public void onOrientationChanged(double orientation, double tilt) {

    }

    @Override
    public void sensorValue(double gyroX, double gyroY, double gyroZ, double accelX, double accelY, double accelZ, double heading) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public class UIFragmentComponent {

        private static final int MAX_LENGTH = 15000;
        private static final int LOWER_THRESHOLD = (int) (MAX_LENGTH * 0.5);

        public synchronized void logTextFragment(final String tag, final String text, int color) {
            final SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(tag).append(" | ").append(text).append("\n");
            builder.setSpan(
                    new ForegroundColorSpan(color),
                    0 /* start */,
                    builder.length(),
                    SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE);

            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            logView.append(builder);
                            /*SharedPreferences sharedPreferences = PreferenceManager.
                                    getDefaultSharedPreferences(getActivity());*/
                            Editable editable = logView.getEditableText();
                            int length = editable.length();
                            if (length > MAX_LENGTH) {
                                editable.delete(0, length - LOWER_THRESHOLD);
                            }
                            if (isAutoScroll) {
                                logScroll.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        logScroll.fullScroll(View.FOCUS_DOWN);
                                    }
                                });
                            }
                        }
                    });
        }

        public void startActivity(Intent intent) {
            getActivity().startActivity(intent);
        }
    }
}
