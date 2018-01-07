package fyp.layout;

//import android.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.TextView;

import fyp.layout.LoggerUI;


public class LogFragment extends Fragment {

    View myView;

    TextView logView;
    ScrollView logScroll;

    Button startLog, stopLog, timer, clear;

    CheckBox autoScroll;
    boolean isAutoScroll = true;

    private LoggerUI loggerUI;
    private LoggerFile loggerFile;

    private final UIFragmentComponent mUiComponent = new UIFragmentComponent();
    public void setFileLogger(LoggerFile value) {
        loggerFile = value;
    }
    public void setUILogger(LoggerUI value) {
        loggerUI = value;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.log_layout, container, false);

        logView = (TextView) myView.findViewById(R.id.log_view);
        logScroll = (ScrollView) myView.findViewById(R.id.log_scroll);

        startLog = (Button) myView.findViewById(R.id.start_logs);
        stopLog = (Button) myView.findViewById(R.id.send_file);
        timer = (Button) myView.findViewById(R.id.timer);
        clear = (Button) myView.findViewById(R.id.clear);

        autoScroll = (CheckBox) myView.findViewById(R.id.autoScroll);

        LoggerUI currentUiLogger = loggerUI;
        if (currentUiLogger != null) {
            currentUiLogger.setUiFragmentComponent(mUiComponent);
        }

        LoggerFile currentFileLogger = loggerFile;
        if (currentFileLogger != null) {
            currentFileLogger.setUiComponent(mUiComponent);
        }

        startedLog(false);

        startLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startedLog(true);
                loggerFile.startNewLog();
            }
        });

        stopLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startedLog(false);
                loggerFile.send();
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
                } ;
            }
        });



        return myView;
    }

    private void startedLog(boolean started) {
        startLog.setEnabled(!started);
        stopLog.setEnabled(started);
        timer.setEnabled(!started);
    }

    public class UIFragmentComponent {

        private static final int MAX_LENGTH = 42000;
        private static final int LOWER_THRESHOLD = (int) (MAX_LENGTH * 0.5);

        public synchronized void logTextFragment(final String tag, final String text, int color) {
            final SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(tag).append(" | ").append(text).append("\n");
            builder.setSpan(new ForegroundColorSpan(color), 0, builder.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);

            autoScroll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        builder.append("Auto Scroll: On\n");
                    } else {
                        builder.append("Auto Scroll: Off\n");
                    } ;
                }
            });

            Activity activity = getActivity();

            if (activity == null) return;

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logView.append(builder);
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
