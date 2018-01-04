package fyp.layout;

import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.LocationListener;

public interface MainActivityListener extends LocationListener {
    void gpsStart();

    void gpsStop();
}
