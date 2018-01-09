package fyp.recorder;


import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import fyp.layout.R;

public class Setting extends PreferenceActivity {

    private Toolbar mActionBar;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
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
