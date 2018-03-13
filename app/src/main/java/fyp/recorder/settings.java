package fyp.recorder;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.provider.SyncStateContract;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Set;

import fyp.layout.R;

public class settings extends PreferenceActivity {

    private Toolbar mActionBar;

    SharedPreferences.OnSharedPreferenceChangeListener rawLogListener, sendLogFTP, hasLoginName;

    Boolean logRaw = false;
    boolean sendLog = false;
    boolean loginName = false;

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

        sendLogFTP = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                sendLog = preferences.getBoolean(getString(R.string.pref_key_send_to_ftp), true);
                getPreferenceScreen().findPreference(getString(R.string.pref_key_ip_address)).setEnabled(sendLog);
                getPreferenceScreen().findPreference(getString(R.string.pref_key_port)).setEnabled(sendLog);
                getPreferenceScreen().findPreference(getString(R.string.pref_key_ftp_login_name)).setEnabled(sendLog);
                getPreferenceScreen().findPreference(getString(R.string.pref_key_ftp_login_pw)).setEnabled((sendLog && loginName));
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(sendLogFTP);

        hasLoginName = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                String name = preferences.getString(getString(R.string.pref_key_ftp_login_name), "");
                if (name.compareTo("") == 0) {
                    loginName = false;
                    getPreferenceScreen().findPreference(getString(R.string.pref_key_ftp_login_name)).setSummary("Now is anonymous login");
                } else {
                    loginName = true;
                    getPreferenceScreen().findPreference(getString(R.string.pref_key_ftp_login_name)).setSummary("Now login as " + name);
                }
                getPreferenceScreen().findPreference(getString(R.string.pref_key_ftp_login_pw)).setEnabled((sendLog && loginName));
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(hasLoginName);
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

    public void onResume() {
        super.onResume();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);

        sendLog = pref.getBoolean(getString(R.string.pref_key_send_to_ftp), true);
        getPreferenceScreen().findPreference(getString(R.string.pref_key_ip_address)).setEnabled(sendLog);
        getPreferenceScreen().findPreference(getString(R.string.pref_key_port)).setEnabled(sendLog);

        String name = pref.getString(getString(R.string.pref_key_ftp_login_name), "");
        if (name.compareTo("") == 0) {
            loginName = false;
            getPreferenceScreen().findPreference(getString(R.string.pref_key_ftp_login_name)).setSummary("Now is anonymous login");
        } else {
            loginName = true;
            getPreferenceScreen().findPreference(getString(R.string.pref_key_ftp_login_name)).setSummary("Now login as " + name);
        }

        getPreferenceScreen().findPreference(getString(R.string.pref_key_ftp_login_name)).setEnabled(sendLog);
        getPreferenceScreen().findPreference(getString(R.string.pref_key_ftp_login_pw)).setEnabled((sendLog && loginName));

        Set<String> selections = pref.getStringSet(getString(R.string.pref_key_log_type), null);
        String[] selected = selections.toArray(new String[]{});
        logRaw = false;
        for (int i = 0; i < selected.length; i++) {
            if (Integer.parseInt(selected[i]) == 1) {
                logRaw = true;
            }
        }
        getPreferenceScreen().findPreference(getString(R.string.pref_key_raw_log_type)).setEnabled(logRaw);

    }
}
