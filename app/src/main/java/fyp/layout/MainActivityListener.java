package fyp.layout;

import android.hardware.SensorEvent;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;

public interface MainActivityListener extends LocationListener {
    void gpsStart();

    void gpsStop();

    void onGnssFirstFix(int ttffMillis);

    void onSatelliteStatusChanged(GnssStatus status);

    void onGnssStarted();

    void onGnssStopped();

    void onGnssMeasurementsReceived(GnssMeasurementsEvent event);

    void onGnssNavigationMessageReceived(GnssNavigationMessage event);

    void onNmeaReceived(long l, String s);

    void onOrientationChanged(double orientation, double tilt);

    void sensorValue(double gyroX, double gyroY, double gyroZ, double accelX, double accelY, double accelZ, double heading);
}
