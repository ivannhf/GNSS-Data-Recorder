package fyp.layout;

//import android.app.Fragment;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ToolFragment extends Fragment implements MainActivityListener{

    View myView;

    TextView textX, textY, textZ;
    TextView tv_accX, tv_accY, tv_accZ, tv_mag;
    SensorManager gyroManager, accManager, magManager;
    Sensor gyroSensor, accSensor, magSensor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myView = inflater.inflate(R.layout.tool_layout, container, false);

        textX = (TextView) myView.findViewById(R.id.textX);
        textY = (TextView) myView.findViewById(R.id.textY);
        textZ = (TextView) myView.findViewById(R.id.textZ);

        tv_accX = (TextView) myView.findViewById(R.id.accX);
        tv_accY = (TextView) myView.findViewById(R.id.accY);
        tv_accZ = (TextView) myView.findViewById(R.id.accZ);

        tv_mag = (TextView) myView.findViewById(R.id.magnet);

        MainActivity.getInstance().addListener(this);

        return myView;


    }

    public void onResume() {
        super.onResume();
    }

    public void onStop() {
        super.onStop();
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
        textX.setText("X : " + gyroX + " rad/s");
        textY.setText("Y : " + gyroY + " rad/s");
        textZ.setText("Z : " + gyroZ + " rad/s");

        tv_accX.setText("X Acc : " + accelX + " m/s2");
        tv_accY.setText("Y Acc: " + accelY + " m/s2");
        tv_accZ.setText("Z Acc: " + accelZ + " m/s2");

        tv_mag.setText("Heading: " + heading + " deg");
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
}
