package fyp.logger;

//import android.app.Fragment;
import android.content.Context;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
        import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

        import android.location.Location;
        import android.widget.LinearLayout;

        import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
        import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fyp.layout.R;


public class MapFragment extends Fragment implements OnMapReadyCallback, MainActivityListener {
    @Override
    public void sensorValue(double gyroX, double gyroY, double gyroZ, double accelX, double accelY, double accelZ, double heading) {

    }
//View myView;

    private static final String TAG = "MapFragment";
    Context context;

    private GoogleMap googleMap;
    Marker mCurrLocationMarker = null;

    private MapView mapView;
    private boolean mapsSupported = true;
    double longitude = 0.0, latitude = 0.0;
    LatLng latLng = new LatLng(22.304185, 114.179534);

    private LocationManager locationManager;
    private LocationListener locationListener;
    private LocationRequest locationRequest;
    private Location mLastLocation;

    public MapFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final LinearLayout myView = (LinearLayout) inflater.inflate(R.layout.map_layout, container, false);
        context = container.getContext();

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        SupportMapFragment fragment = new SupportMapFragment();
        transaction.add(R.id.mapView, fragment);
        transaction.commit();

        fragment.getMapAsync(this);


        return myView;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //map is ready
        googleMap = map;
        //googleMap.setMyLocationEnabled(true);

        //mCurrLocationMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(22.304185, 114.179534)).title("Current Position"));
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.304185, 114.179534), 17));

        /*locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);
        } else {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, MapFragment.this);
            //locationRequest = new LocationRequest();
        }*/
        MainActivity.getInstance().addListener(this);


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


        LatLng latLng = new LatLng(latitude, longitude);



        //Toast.makeText(getContext(), "Lat: " + latitude + " Lng: " + longitude, Toast.LENGTH_LONG).show();


        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }


        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");

        //if ((markerOptions != null) && (latLng != null)) {
        //mCurrLocationMarker = googleMap.addMarker(new MarkerOptions().position(latLng));
        mCurrLocationMarker = googleMap.addMarker(markerOptions);
        //mCurrLocationMarker.setPosition(new LatLng(latitude, longitude));
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        //mCurrLocationMarker.setPosition(latLng);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        //Toast.makeText(context, "Marker Updated\n" + latLng, Toast.LENGTH_SHORT).show();
        //}

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
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

