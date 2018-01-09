package fyp.logger;

//import android.app.Fragment;

import android.content.Context;
import android.content.res.Resources;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import fyp.layout.R;
import fyp.logger.util.GnssType;
import fyp.logger.util.GpsTestUtil;


public class SysFragment extends Fragment implements MainActivityListener {
    View myView;
    private static final String TAG = "ListFragment";
    Context context;
    Resources resources;

    TextView gpstv, glotv, qzsstv, bdtv, galtv;
    ProgressBar gpsBar, gloBar, qzssBar, bdBar, galBar;

    private int mSvCount, mUsedInFixCount;
    private int gpsCount, gloCount, qzssCount, bdCount, galCount;
    private int gpsFix, gloFix, qzssFix, bdFix, galFix;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.system_layout, container, false);
        context = getContext();
        resources = getResources();

        MainActivity.getInstance().addListener(this);

        gpstv = (TextView) myView.findViewById(R.id.Sat_GPS);
        glotv = (TextView) myView.findViewById(R.id.Sat_GLO);
        qzsstv = (TextView) myView.findViewById(R.id.Sat_QZSS);
        bdtv = (TextView) myView.findViewById(R.id.Sat_BD);
        galtv = (TextView) myView.findViewById(R.id.Sat_GAL);

        gpsBar = (ProgressBar) myView.findViewById(R.id.progressBarGPS);
        gloBar = (ProgressBar) myView.findViewById(R.id.progressBarGLO);
        qzssBar = (ProgressBar) myView.findViewById(R.id.progressBarQZSS);
        bdBar = (ProgressBar) myView.findViewById(R.id.progressBarBD);
        galBar = (ProgressBar) myView.findViewById(R.id.progressBarGAL);

        return myView;
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
        final int length = status.getSatelliteCount();

        mSvCount = 0;
        mUsedInFixCount = 0;

        gpsCount = 0;
        gloCount = 0;
        qzssCount = 0;
        bdCount = 0;
        galCount = 0;
        gpsFix = 0;
        gloFix = 0;
        qzssFix = 0;
        bdFix = 0;
        galFix = 0;

        gpsBar.setProgress(0);
        gloBar.setProgress(0);
        qzssBar.setProgress(0);
        bdBar.setProgress(0);
        galBar.setProgress(0);

        while (mSvCount < length) {
            GnssType type;
            type = GpsTestUtil.getGnssConstellationType(status.getConstellationType(mSvCount));

            switch (type) {
                case NAVSTAR:
                    gpsCount++;
                    if (status.usedInFix(mSvCount)) gpsFix++;
                    break;
                case GLONASS:
                    gloCount++;
                    if (status.usedInFix(mSvCount)) gloFix++;
                    break;
                case QZSS:
                    qzssCount++;
                    if (status.usedInFix(mSvCount)) qzssFix++;
                    break;
                case BEIDOU:
                    bdCount++;
                    if (status.usedInFix(mSvCount)) bdFix++;
                    break;
                case GALILEO:
                    galCount++;
                    if (status.usedInFix(mSvCount)) galFix++;
                    break;
            }

            mSvCount++;
            if (status.usedInFix(mSvCount)) mUsedInFixCount++;
        }


        gpsBar.setMax(gpsCount);
        gpsBar.setProgress(gpsFix, false);

        gloBar.setMax(gloCount);
        gloBar.setProgress(gloFix, false);

        qzssBar.setMax(qzssCount);
        qzssBar.setProgress(qzssFix, false);

        bdBar.setMax(bdCount);
        bdBar.setProgress(bdFix, false);

        galBar.setMax(galCount);
        galBar.setProgress(galFix, false);


        gpstv.setText("" + resources.getString(R.string.gps_num_sats_value, gpsFix, gpsCount));
        glotv.setText("" + resources.getString(R.string.gps_num_sats_value, gloFix, gloCount));
        qzsstv.setText("" + resources.getString(R.string.gps_num_sats_value, qzssFix, qzssCount));
        bdtv.setText("" + resources.getString(R.string.gps_num_sats_value, bdFix, bdCount));
        galtv.setText("" + resources.getString(R.string.gps_num_sats_value, galFix, galCount));

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
