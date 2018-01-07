package fyp.layout;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class InfoFragment extends DialogFragment implements MainActivityListener {
    private static final String TAG = "InfoFragment";
    View myView;
    Context context;

    TextView gainCtrl, carrierCyc, carrierFreq, carrierPhase, carrierPhaUn, SNR;

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
    public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
        for (GnssMeasurement measurement : event.getMeasurements()) {
            if ((measurement.hasAutomaticGainControlLevelDb() == true) && (android.os.Build.VERSION.SDK_INT >= 26)) {
                gainCtrl.setText("yes");
            } else gainCtrl.setText("no");

            if (measurement.hasCarrierCycles() == true) {
                carrierCyc.setText("yes");
            } else carrierCyc.setText("no");

            if (measurement.hasCarrierFrequencyHz() == true) {
                carrierFreq.setText("yes");
            } else carrierFreq.setText("no");

            if (measurement.hasCarrierPhase() == true) {
                carrierPhase.setText("yes");
            } else carrierPhase.setText("no");

            if (measurement.hasCarrierPhaseUncertainty() == true) {
                carrierPhaUn.setText("yes");
            } else carrierPhaUn.setText("no");

            if (measurement.hasSnrInDb() == true) {
                SNR.setText("yes");
            } else SNR.setText("no");
        }

    }

    @Override
    public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {

    }

    @Override
    public void onOrientationChanged(double orientation, double tilt) {

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