package fyp.layout;

import android.graphics.Color;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import java.text.DecimalFormat;

import fyp.layout.LogFragment.UIFragmentComponent;

public class LoggerUI implements MainActivityListener {
    private static final int USED_COLOR = Color.rgb(0x4a, 0x5f, 0x70);

    public LoggerUI() {
        MainActivity.getInstance().addListener(this);
    }

    private UIFragmentComponent mUiFragmentComponent;

    public synchronized UIFragmentComponent getUiFragmentComponent() {
        return mUiFragmentComponent;
    }

    public synchronized void setUiFragmentComponent(UIFragmentComponent value) {
        mUiFragmentComponent = value;
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
        StringBuilder builder = new StringBuilder("[ GnssMeasurementsEvent:\n\n");

        builder.append(toStringClock(event.getClock()));
        builder.append("\n");

        for (GnssMeasurement measurement : event.getMeasurements()) {
            builder.append(toStringMeasurement(measurement));
            builder.append("\n");
        }

        builder.append("]");
        //logMeasurementEvent("onGnsssMeasurementsReceived: " + builder.toString());
    }

    @Override
    public void onGnssNavigationMessageReceived(GnssNavigationMessage event) {

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
        //loglocationEvent("onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void logLocationEvent(String event) {
        logEvent("Location", event, USED_COLOR);
    }

    private void logEvent(String tag, String message, int color) {
        //String composedTag = GnssContainer.TAG + tag;
        String composedTag = "";
        Log.d(composedTag, message);
        logText(tag, message, color);
    }

    private void logText(String tag, String text, int color) {
        UIFragmentComponent component = getUiFragmentComponent();
        if (component != null) {
            component.logTextFragment(tag, text, color);
        }
    }

    private String toStringClock(GnssClock gnssClock) {
        final String format = "   %-4s = %s\n";
        StringBuilder builder = new StringBuilder("GnssClock:\n");
        DecimalFormat numberFormat = new DecimalFormat("#0.000");
        if (gnssClock.hasLeapSecond()) {
            builder.append(String.format(format, "LeapSecond", gnssClock.getLeapSecond()));
        }

        builder.append(String.format(format, "TimeNanos", gnssClock.getTimeNanos()));
        if (gnssClock.hasTimeUncertaintyNanos()) {
            builder.append(
                    String.format(format, "TimeUncertaintyNanos", gnssClock.getTimeUncertaintyNanos()));
        }

        if (gnssClock.hasFullBiasNanos()) {
            builder.append(String.format(format, "FullBiasNanos", gnssClock.getFullBiasNanos()));
        }

        if (gnssClock.hasBiasNanos()) {
            builder.append(String.format(format, "BiasNanos", gnssClock.getBiasNanos()));
        }
        if (gnssClock.hasBiasUncertaintyNanos()) {
            builder.append(
                    String.format(
                            format,
                            "BiasUncertaintyNanos",
                            numberFormat.format(gnssClock.getBiasUncertaintyNanos())));
        }

        if (gnssClock.hasDriftNanosPerSecond()) {
            builder.append(
                    String.format(
                            format,
                            "DriftNanosPerSecond",
                            numberFormat.format(gnssClock.getDriftNanosPerSecond())));
        }

        if (gnssClock.hasDriftUncertaintyNanosPerSecond()) {
            builder.append(
                    String.format(
                            format,
                            "DriftUncertaintyNanosPerSecond",
                            numberFormat.format(gnssClock.getDriftUncertaintyNanosPerSecond())));
        }

        builder.append(
                String.format(
                        format,
                        "HardwareClockDiscontinuityCount",
                        gnssClock.getHardwareClockDiscontinuityCount()));

        return builder.toString();
    }

    private String toStringMeasurement(GnssMeasurement measurement) {
        final String format = "   %-4s = %s\n";
        StringBuilder builder = new StringBuilder("GnssMeasurement:\n");
        DecimalFormat numberFormat = new DecimalFormat("#0.000");
        DecimalFormat numberFormat1 = new DecimalFormat("#0.000E00");
        builder.append(String.format(format, "Svid", measurement.getSvid()));
        builder.append(String.format(format, "ConstellationType", measurement.getConstellationType()));
        builder.append(String.format(format, "TimeOffsetNanos", measurement.getTimeOffsetNanos()));

        builder.append(String.format(format, "State", measurement.getState()));

        builder.append(
                String.format(format, "ReceivedSvTimeNanos", measurement.getReceivedSvTimeNanos()));
        builder.append(
                String.format(
                        format,
                        "ReceivedSvTimeUncertaintyNanos",
                        measurement.getReceivedSvTimeUncertaintyNanos()));

        builder.append(String.format(format, "Cn0DbHz", numberFormat.format(measurement.getCn0DbHz())));

        builder.append(
                String.format(
                        format,
                        "PseudorangeRateMetersPerSecond",
                        numberFormat.format(measurement.getPseudorangeRateMetersPerSecond())));
        builder.append(
                String.format(
                        format,
                        "PseudorangeRateUncertaintyMetersPerSeconds",
                        numberFormat.format(measurement.getPseudorangeRateUncertaintyMetersPerSecond())));

        if (measurement.getAccumulatedDeltaRangeState() != 0) {
            builder.append(
                    String.format(
                            format, "AccumulatedDeltaRangeState", measurement.getAccumulatedDeltaRangeState()));

            builder.append(
                    String.format(
                            format,
                            "AccumulatedDeltaRangeMeters",
                            numberFormat.format(measurement.getAccumulatedDeltaRangeMeters())));
            builder.append(
                    String.format(
                            format,
                            "AccumulatedDeltaRangeUncertaintyMeters",
                            numberFormat1.format(measurement.getAccumulatedDeltaRangeUncertaintyMeters())));
        }

        if (measurement.hasCarrierFrequencyHz()) {
            builder.append(
                    String.format(format, "CarrierFrequencyHz", measurement.getCarrierFrequencyHz()));
        }

        if (measurement.hasCarrierCycles()) {
            builder.append(String.format(format, "CarrierCycles", measurement.getCarrierCycles()));
        }

        if (measurement.hasCarrierPhase()) {
            builder.append(String.format(format, "CarrierPhase", measurement.getCarrierPhase()));
        }

        if (measurement.hasCarrierPhaseUncertainty()) {
            builder.append(
                    String.format(
                            format, "CarrierPhaseUncertainty", measurement.getCarrierPhaseUncertainty()));
        }

        builder.append(
                String.format(format, "MultipathIndicator", measurement.getMultipathIndicator()));

        if (measurement.hasSnrInDb()) {
            builder.append(String.format(format, "SnrInDb", measurement.getSnrInDb()));
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (measurement.hasAutomaticGainControlLevelDb()) {
                builder.append(
                        String.format(format, "AgcDb", measurement.getAutomaticGainControlLevelDb()));
            }
            if (measurement.hasCarrierFrequencyHz()) {
                builder.append(String.format(format, "CarrierFreqHz", measurement.getCarrierFrequencyHz()));
            }
        }

        return builder.toString();
    }
}
