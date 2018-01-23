package fyp.recorder;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fyp.layout.R;

public class RadarFragment extends Fragment {
    View myView;
    View skyview;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.radar_layout, container, false);
        skyview = myView.findViewById(R.id.sys_radar);
        return myView;
    }

    @Override
    public void onResume() {
        //skyview.setVisibility(View.VISIBLE);
        super.onResume();
    }

    @Override
    public void onPause() {
        //skyview.setVisibility(View.INVISIBLE);
        super.onPause();
    }
}
