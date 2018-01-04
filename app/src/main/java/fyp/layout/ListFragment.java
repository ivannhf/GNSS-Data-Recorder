package fyp.layout;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.Iterator;

public class ListFragment extends Fragment implements GpsStatus.Listener {
    View myView;

    private static final String TAG = "ListFragment";
    Context context;

    TextView tv;

    LocationManager locationManager = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.list_layout, container, false);
        context = container.getContext();

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.addGpsStatusListener(this);

        tv = (TextView) myView.findViewById(R.id.List);

        return myView;
    }


    @Override
    public void onGpsStatusChanged(int event) {
        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        String strGpsStats = "";
        if(gpsStatus != null) {
            Iterable<GpsSatellite>satellites = gpsStatus.getSatellites();
            Iterator<GpsSatellite> sat = satellites.iterator();
            int i=0;
            while (sat.hasNext()) {
                GpsSatellite satellite = sat.next();
                strGpsStats += (i++) + ": " + satellite.getPrn() + "," + satellite.usedInFix() + "," + satellite.getSnr() + "," + satellite.getAzimuth() + "," + satellite.getElevation()+ "\n\n";
            }
            tv.setText(strGpsStats);
        }

    }
}