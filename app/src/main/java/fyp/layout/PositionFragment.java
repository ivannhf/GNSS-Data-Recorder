package fyp.layout;

import android.Manifest;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class PositionFragment extends Fragment implements LocationListener {

    private static final String TAG = "PositionFragment";

    View myView;

    private TextView longitudeField, latitudeField;

    double latitude = 0, longitude = 0;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private LocationRequest locationRequest;
    private Location mLastLocation;


    public PositionFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.position_layout, container, false);

        longitudeField = (TextView) myView.findViewById(R.id.TV_long);
        latitudeField = (TextView) myView.findViewById(R.id.TV_lat);

        locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);
        } else {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, PositionFragment.this);
            //locationRequest = new LocationRequest();
        }

        return myView;
    }


    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        longitude = mLastLocation.getLongitude();
        latitude = mLastLocation.getLatitude();

        longitudeField.setText("Longitude: " + longitude + " .");
        latitudeField.setText("Latitude: " + latitude + " .");
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
}
