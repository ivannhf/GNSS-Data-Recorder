package fyp.recorder;

import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import fyp.layout.R;


public class PositionFragment extends Fragment implements MainActivityListener {

    private static final String TAG = "PositionFragment";
    Context context;

    View myView;

    private TextView longitudeField, latitudeField, altitudeField, accField;

    double latitude = 0, longitude = 0, altitude = 0, accurate = 0;

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
        context = container.getContext();

        longitudeField = (TextView) myView.findViewById(R.id.Value_long);
        latitudeField = (TextView) myView.findViewById(R.id.Value_lat);
        altitudeField = (TextView) myView.findViewById(R.id.Value_alt);
        accField = (TextView) myView.findViewById(R.id.Value_accurate);

        MainActivity.getInstance().addListener(this);

        //locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

        /*if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);
        } else {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, PositionFragment.this);
            //locationRequest = new LocationRequest();
        }*/
        return myView;
    }


    @Override
    public void gpsStart() {

    }

    @Override
    public void gpsStop() {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        longitude = mLastLocation.getLongitude();
        latitude = mLastLocation.getLatitude();
        altitude = mLastLocation.getAltitude();
        accurate = mLastLocation.getAccuracy();

        String longStr = String.format("%3.4f", longitude);
        String latStr = String.format("%3.4f", latitude);

        longitudeField.setText(String.format("%3.4f", longitude));
        latitudeField.setText(String.format("%3.4f", latitude));
        altitudeField.setText(String.format("%3.4f", altitude));
        accField.setText(String.format("%3.4f", accurate));

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //Toast.makeText(context, "Position fixed: " + latLng, Toast.LENGTH_SHORT).show();
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
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
