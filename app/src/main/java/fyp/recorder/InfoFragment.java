package fyp.recorder;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import fyp.layout.R;

public class InfoFragment extends DialogFragment implements MainActivityListener {
    private static final String TAG = "InfoFragment";
    View myView;
    Context context;

    TextView gainCtrl, carrierCyc, carrierFreq, carrierPhase, carrierPhaUn, SNR;
    TextView deviceModeltv, deviceManutv, devicePlatformtv;

    Boolean bool_gainCtrl = null, bool_carrierCyc = null, bool_carrierFreq = null,
            bool_carrierPhase = null, bool_carrierPhaUn = null, bool_SNR = null;

    Double val_gainCtrl = null;
    Long val_carrierCyc = null;
    Float val_carrierFreq = null;
    Double val_carrierPhase = null;
    Double val_carrierPhaUn = null;
    Double val_SNR = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        myView = inflater.inflate(R.layout.info_layout, null);

        builder.setView(myView).setNegativeButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                InfoFragment.this.getDialog().dismiss();
            }
        });

        //myView = getView();
        gainCtrl = (TextView) myView.findViewById(R.id.gainCtrl_value);
        carrierCyc = (TextView) myView.findViewById(R.id.carrierCyc_value);
        carrierFreq = (TextView) myView.findViewById(R.id.carrierFreq_value);
        carrierPhase = (TextView) myView.findViewById(R.id.carrierPhase_value);
        carrierPhaUn = (TextView) myView.findViewById(R.id.carrierPhaUn_value);
        SNR = (TextView) myView.findViewById(R.id.SNR_value);

        deviceModeltv = (TextView) myView.findViewById(R.id.deviceModel);
        deviceManutv = (TextView) myView.findViewById(R.id.deviceManufacturer);
        devicePlatformtv = (TextView) myView.findViewById(R.id.devicePlatform);

        deviceModeltv.setText("Model: " + Build.MODEL + " (" + Build.DEVICE + ")");
        deviceManutv.setText("Manufacturer: " + Build.MANUFACTURER);
        devicePlatformtv.setText("Platform: " + Build.VERSION.RELEASE + " (SDK Ver.: " + Build.VERSION.SDK_INT + ")");

        MainActivity.getInstance().addListener(this);

        return builder.show();
        //return super.onCreateDialog(savedInstanceState);
    }

    /*
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.info_layout, container, false);
        gainCtrl = (TextView) myView.findViewById(R.id.gainCtrl_value);
        carrierCyc = (TextView) myView.findViewById(R.id.carrierCyc_value);
        carrierFreq = (TextView) myView.findViewById(R.id.carrierFreq_value);
        carrierPhase = (TextView) myView.findViewById(R.id.carrierPhase_value);
        carrierPhaUn = (TextView) myView.findViewById(R.id.carrierPhaUn_value);
        SNR = (TextView) myView.findViewById(R.id.SNR_value);

        return myView;
    }*/

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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
        for (GnssMeasurement measurement : event.getMeasurements()) {
            if(Build.VERSION.SDK_INT >= 26) {
                if (measurement.hasAutomaticGainControlLevelDb() == true) {
                    val_gainCtrl = measurement.getAutomaticGainControlLevelDb();
                    bool_gainCtrl = true;
                    //gainCtrl.setText("yes");
                }
            }
            //else gainCtrl.setText("no");


            if (measurement.hasCarrierCycles() == true) {
                val_carrierCyc = measurement.getCarrierCycles();
                bool_carrierCyc = true;
                //carrierCyc.setText("yes");
            }
            //else carrierCyc.setText("no");

            if (measurement.hasCarrierFrequencyHz() == true) {
                val_carrierFreq = measurement.getCarrierFrequencyHz();
                bool_carrierFreq = true;
                //carrierFreq.setText("yes");
            }
            //else carrierFreq.setText("no");

            if (measurement.hasCarrierPhase() == true) {
                val_carrierPhase = measurement.getCarrierPhase();
                bool_carrierPhase = true;
                // carrierPhase.setText("yes");
            } else carrierPhase.setText("no");

            if (measurement.hasCarrierPhaseUncertainty() == true) {
                val_carrierPhaUn = measurement.getCarrierPhaseUncertainty();
                bool_carrierPhaUn = true;
                //carrierPhaUn.setText("yes");
            }
            //else carrierPhaUn.setText("no");

            if (measurement.hasSnrInDb() == true) {
                val_SNR = measurement.getSnrInDb();
                bool_SNR = true;
                //SNR.setText("yes");
            }
            //else SNR.setText("no");
        }

        if (val_gainCtrl != null) gainCtrl.setText("yes");
        else gainCtrl.setText(val_gainCtrl+"");

        if (val_carrierCyc != null) carrierCyc.setText("yes");
        else carrierCyc.setText("no");

        if (val_carrierFreq != null) carrierFreq.setText("yes");
        else carrierFreq.setText("no");

        if (val_carrierPhase != null) carrierPhase.setText("yes");
        else carrierPhase.setText("no");

        if (val_carrierPhaUn != null) carrierPhaUn.setText("yes");
        else carrierPhaUn.setText("no");

        if (val_SNR != null) SNR.setText("yes");
        else SNR.setText(val_SNR +"");

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