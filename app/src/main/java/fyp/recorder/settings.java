package fyp.recorder;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Set;

import fyp.layout.R;

public class settings extends PreferenceActivity {

    private Toolbar mActionBar;

    SharedPreferences.OnSharedPreferenceChangeListener rawLogListener;

    Boolean logRaw = false;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("settings");
        addPreferencesFromResource(R.xml.settings);

        final SharedPreferences preferences = this.getSharedPreferences("settings", MODE_PRIVATE);
        rawLogListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Set<String> selections = preferences.getStringSet(getString(R.string.pref_key_log_type), null);
                String[] selected = selections.toArray(new String[]{});
                logRaw = false;
                for (int i = 0; i < selected.length; i++) {
                    if (Integer.parseInt(selected[i]) == 1) {
                        logRaw = true;
                    }
                }
                getPreferenceScreen().findPreference(getString(R.string.pref_key_raw_log_type)).setEnabled(logRaw);
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(rawLogListener);
    }

    @Override
    public void setContentView(int layoutResID) {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.setting_layout, new LinearLayout(this), false);

        mActionBar = (Toolbar) contentView.findViewById(R.id.action_bar);

        mActionBar.setTitle(getTitle());

        mActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ViewGroup contentWrapper = (ViewGroup) contentView.findViewById(R.id.content_wrapper);
        LayoutInflater.from(this).inflate(layoutResID, contentWrapper, true);

        getWindow().setContentView(contentView);

        mActionBar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
        mActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
