package fyp.layout.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.GnssMeasurement;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

//import com.android.gpstest.DilutionOfPrecision;

import java.util.concurrent.TimeUnit;

public class GpsTestUtil {
    private static final String TAG = "GpsTestUtil";

    private static final String NMEA_OUTPUT_TAG = "GpsOutputNmea";

    private static final String MEASURE_OUTPUT_TAG = "GpsOutputMeasure";

    private static final String NM_OUTPUT_TAG = "GpsOutputNav";

    private static StringBuilder mNmeaOutput = new StringBuilder();

    @Deprecated
    public static GnssType getGnssType(int prn) {
        if (prn >= 65 && prn <= 96) {
            // See Issue #26 for details
            return GnssType.GLONASS;
        } else if (prn >= 193 && prn <= 200) {
            // See Issue #54 for details
            return GnssType.QZSS;
        } else if (prn >= 201 && prn <= 235) {
            // See Issue #54 for details
            return GnssType.BEIDOU;
        } else if (prn >= 301 && prn <= 330) {
            // See https://github.com/barbeau/gpstest/issues/58#issuecomment-252235124 for details
            return GnssType.GALILEO;
        } else if (prn >= 1 && prn <= 32) {
            return GnssType.NAVSTAR;
        } else {
            return GnssType.UNKNOWN;
        }
    }

    public static GnssType getGnssConstellationType(int gnssConstellationType) {
        switch (gnssConstellationType) {
            case GnssStatus.CONSTELLATION_GPS:
                return GnssType.NAVSTAR;
            case GnssStatus.CONSTELLATION_GLONASS:
                return GnssType.GLONASS;
            case GnssStatus.CONSTELLATION_BEIDOU:
                return GnssType.BEIDOU;
            case GnssStatus.CONSTELLATION_QZSS:
                return GnssType.QZSS;
            case GnssStatus.CONSTELLATION_GALILEO:
                return GnssType.GALILEO;
            case GnssStatus.CONSTELLATION_UNKNOWN:
                return GnssType.UNKNOWN;
            default:
                return GnssType.UNKNOWN;
        }
    }

    public static void writeGnssMeasurementToLog(GnssMeasurement measurement) {
        Log.d(MEASURE_OUTPUT_TAG, measurement.toString());
    }

    public static boolean canManageDialog(Activity activity) {
        if (activity == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !activity.isFinishing() && !activity.isDestroyed();
        } else {
            return !activity.isFinishing();
        }
    }
}
