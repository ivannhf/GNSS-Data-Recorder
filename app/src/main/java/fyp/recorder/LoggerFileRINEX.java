package fyp.recorder;

import android.content.Context;
import android.content.Intent;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.BuildConfig;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import fyp.layout.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static android.location.GnssStatus.CONSTELLATION_BEIDOU;
import static android.location.GnssStatus.CONSTELLATION_GALILEO;
import static android.location.GnssStatus.CONSTELLATION_GLONASS;
import static android.location.GnssStatus.CONSTELLATION_GPS;
import static android.location.GnssStatus.CONSTELLATION_QZSS;

/**
 * A GNSS logger to store information to a file.
 */
public class LoggerFileRINEX implements MainActivityListener {

    private static final String TAG = "FileLogger";
    private static final String FLOFDER_PREFIX = "AAE01_GNSS_Data/RINEX";
    private static final String FILE_PREFIX = "gnss_data_RINEX";
    private static final String ERROR_WRITING_FILE = "Problem writing to file.";
    private static final String COMMENT_START = "# ";
    private static final char RECORD_DELIMITER = ',';
    private static final String VERSION_TAG = "Version: ";

    private static final String RINEX_VERSION = "3.03";
    private static final String RINEX_TYPE = "OBSERVATION DATA";
    private static final String RINEX_SYS = "M: Mixed";

    private GnssStatus firstFixStatus = null;
    private int leapSec = -1;
    private String satTypestr = "";

    private static final int MAX_FILES_STORED = 100;
    private static final int MINIMUM_USABLE_FILE_SIZE_BYTES = 1000;

    private final Context mContext;

    private final Object mFileLock = new Object();
    private BufferedWriter mFileWriter;
    private File mFile;

    private LogFragment.UIFragmentComponent mUiComponent;

    public synchronized LogFragment.UIFragmentComponent getUiComponent() {
        return mUiComponent;
    }

    public synchronized void setUiComponent(LogFragment.UIFragmentComponent value) {
        mUiComponent = value;
    }

    public LoggerFileRINEX (Context context) {
        this.mContext = context;
        MainActivity.getInstance().addListener(this);
    }

    /**
     * Start a new file logging process.
     */
    public void startNewLog() {
        synchronized (mFileLock) {
            File baseDirectory;
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                baseDirectory = new File(Environment.getExternalStorageDirectory(), FLOFDER_PREFIX);
                baseDirectory.mkdirs();
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                logError("Cannot write to external storage.");
                return;
            } else {
                logError("Cannot read external storage.");
                return;
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyy_MM_dd_HH_mm_ss");
            Date now = new Date();
            String fileName = String.format("%s_%s.txt", FILE_PREFIX, formatter.format(now));
            File currentFile = new File(baseDirectory, fileName);
            String currentFilePath = currentFile.getAbsolutePath();
            BufferedWriter currentFileWriter;
            try {
                currentFileWriter = new BufferedWriter(new FileWriter(currentFile));
            } catch (IOException e) {
                logException("Could not open file: " + currentFilePath, e);
                return;
            }

            // initialize the contents of the file
            try {
                currentFileWriter.write("----|---1|0---|---2|0---|---3|0---|---4|0---|---5|0---|---6|0---|---7|0---|---8|0\n");
                //currentFileWriter.write("     3.03           OBSERVATION DATA    M: Mixed            RINEX VERSION / TYPE");
                //currentFileWriter.write("{0:9.2f}           {1:<20}{2:<20}RINEX VERSION / TYPE\n".format(RINEX_VERSION, RINEX_TYPE, RINEX_SYS));
                //currentFileWriter.write(String.format("%5s", "") + RINEX_VERSION + String.format("%11s", "") + RINEX_TYPE + String.format("%4s", "") + RINEX_SYS + String.format("%10s", "") +"RINEX VERSION / TYPE");
                currentFileWriter.write(String.format("%9s", RINEX_VERSION) + String.format("%11s", "") + String.format("%-20s", RINEX_TYPE) + String.format("%-20s", RINEX_SYS) + "RINEX VERSION / TYPE");
                currentFileWriter.newLine();
                Date date = null;
                try {
                    SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyyMMdd HHmmss");
                    dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String UTCdate = dateFormatUTC.format(new Date());
                    SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyyMMdd HHmmss");
                    date = dateFormatLocal.parse(UTCdate);
                } catch (java.text.ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String strDate = String.format("%1$tY%1$tm%1$td %1$tH%1$tM%1$tS UTC", date);
                currentFileWriter.write(String.format("%-20s", "GNSS Data Recorder") + String.format("%-20s", "Ivan") + String.format("%-20s", strDate) + "PGM / RUN BY / DATE");
                currentFileWriter.newLine();
                currentFileWriter.write(String.format("%-60s", "Geo") + "MARKER NAME");
                currentFileWriter.newLine();
                currentFileWriter.write(String.format("%-60s", "GEODETIC") + "MARKER TYPE");
                currentFileWriter.newLine();
                currentFileWriter.write(String.format("%-20s", "OBSERVER") + String.format("%-20s", "AGENCY") + String.format("%-20s", "") + "OBSERVER / AGENCY");
                currentFileWriter.newLine();
                currentFileWriter.write(String.format("%-20s", "0") + String.format("%-20s", "Logger User") + String.format("%-20s", "Logger User") + "REC # / TYPE / VERS");
                currentFileWriter.newLine();
                currentFileWriter.write(String.format("%-20s", "0") + String.format("%-20s", "Android-Antenna") + String.format("%-20s", "") + "ANT # / TYPE");
                currentFileWriter.newLine();
                currentFileWriter.write(String.format("%14s", "0.0000") + String.format("%14s", "0.0000") + String.format("%14s", "0.0000") + String.format("%-18s", "") + "APPROX POSITION XYZ");
                currentFileWriter.newLine();
                currentFileWriter.write(String.format("%14s", "0.0000") + String.format("%14s", "0.0000") + String.format("%14s", "0.0000") + String.format("%-18s", "") + "ANTENNA: DELTA H/E/N");
                currentFileWriter.newLine();
                do {
                    Log.d(TAG, "null");
                } while ((firstFixStatus == null) || (leapSec == -1));
                Date firstObs = null;
                firstObs = satSysTime(firstFixStatus.getConstellationType(0), leapSec);
                String year = String.format("%1$tY", firstObs);
                String month = String.format("%1$tm", firstObs);
                String day = String.format("%1$te", firstObs);
                String hour = String.format("%1$tk", firstObs);
                String min = String.format("%1$tM", firstObs);
                String sec = String.format("%1$tS", firstObs);
                String nanosec = String.format("%1$tN", firstObs).substring(0,7);
                currentFileWriter.write(String.format("%6s", year) + String.format("%6s", month) + String.format("%6s", day)
                        + String.format("%6s", hour) + String.format("%6s", min) + String.format("%5s", sec) + "." + nanosec
                        + String.format("%8s", satTypestr) + String.format("%9s", "") + "TIME OF FIRST OBS");
                currentFileWriter.newLine();
                currentFileWriter.write(String.format("%-60s", leapSec) + " LEAP SECONDS");
                currentFileWriter.newLine();


                currentFileWriter.write(String.format("%-60s", "") + "END OF HEADER");
                currentFileWriter.newLine();

                /*currentFileWriter.write(COMMENT_START);
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.write("Header Description:");
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.write(VERSION_TAG);
                String manufacturer = Build.MANUFACTURER;
                String model = Build.MODEL;
                String fileVersion =
                        mContext.getString(R.string.app_version)
                                + " Platform: "
                                + Build.VERSION.RELEASE
                                + " "
                                + "Manufacturer: "
                                + manufacturer
                                + " "
                                + "Model: "
                                + model;
                currentFileWriter.write(fileVersion);
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.write(
                        "Raw,ElapsedRealtimeMillis,TimeNanos,LeapSecond,TimeUncertaintyNanos,FullBiasNanos,"
                                + "BiasNanos,BiasUncertaintyNanos,DriftNanosPerSecond,DriftUncertaintyNanosPerSecond,"
                                + "HardwareClockDiscontinuityCount,Svid,TimeOffsetNanos,State,ReceivedSvTimeNanos,"
                                + "ReceivedSvTimeUncertaintyNanos,Cn0DbHz,PseudorangeRateMetersPerSecond,"
                                + "PseudorangeRateUncertaintyMetersPerSecond,"
                                + "AccumulatedDeltaRangeState,AccumulatedDeltaRangeMeters,"
                                + "AccumulatedDeltaRangeUncertaintyMeters,CarrierFrequencyHz,CarrierCycles,"
                                + "CarrierPhase,CarrierPhaseUncertainty,MultipathIndicator,SnrInDb,"
                                + "ConstellationType,AgcDb,CarrierFrequencyHz");
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.write(
                        "Fix,Provider,Latitude,Longitude,Altitude,Speed,Accuracy,(UTC)TimeInMs");
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.write("Nav,Svid,Type,Status,MessageId,Sub-messageId,Data(Bytes)");
                currentFileWriter.newLine();
                currentFileWriter.write(COMMENT_START);
                currentFileWriter.newLine();*/
            } catch (IOException e) {
                logException("Count not initialize file: " + currentFilePath, e);
                return;
            }

            if (mFileWriter != null) {
                try {
                    mFileWriter.close();
                } catch (IOException e) {
                    logException("Unable to close all file streams.", e);
                    return;
                }
            }

            mFile = currentFile;
            mFileWriter = currentFileWriter;
            Toast.makeText(mContext, "File opened: " + currentFilePath, Toast.LENGTH_SHORT).show();

            // To make sure that files do not fill up the external storage:
            // - Remove all empty files
            FileFilter filter = new FileToDeleteFilter(mFile);
            for (File existingFile : baseDirectory.listFiles(filter)) {
                existingFile.delete();
            }
            // - Trim the number of files with data
            File[] existingFiles = baseDirectory.listFiles();
            int filesToDeleteCount = existingFiles.length - MAX_FILES_STORED;
            if (filesToDeleteCount > 0) {
                Arrays.sort(existingFiles);
                for (int i = 0; i < filesToDeleteCount; ++i) {
                    existingFiles[i].delete();
                }
            }
        }
    }

    /**
     * Send the current log via email or other options selected from a pop menu shown to the user. A
     * new log is started when calling this function.
     */
    public void send() {
        if (mFile == null) {
            return;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("*/*");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "SensorLog");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        // attach the file
        Uri fileURI =
                FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", mFile);
        emailIntent.putExtra(Intent.EXTRA_STREAM, fileURI);
        //getUiComponent().startActivity(Intent.createChooser(emailIntent, "Send log.."));
        if (mFileWriter != null) {
            try {
                mFileWriter.flush();
                mFileWriter.close();
                mFileWriter = null;
            } catch (IOException e) {
                logException("Unable to close all file streams.", e);
                return;
            }
        }
    }

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onLocationChanged(Location location) {
        /*if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            synchronized (mFileLock) {
                if (mFileWriter == null) {
                    return;
                }
                String locationStream =
                        String.format(
                                Locale.US,
                                "Fix,%s,%f,%f,%f,%f,%f,%d",
                                location.getProvider(),
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getAltitude(),
                                location.getSpeed(),
                                location.getAccuracy(),
                                location.getTime());
                try {
                    mFileWriter.write(locationStream);
                    mFileWriter.newLine();
                } catch (IOException e) {
                    logException(ERROR_WRITING_FILE, e);
                }
            }
        }*/
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

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
        firstFixStatus = status;
        final int length = status.getSatelliteCount();
        int mSvCount = 0;
        while (mSvCount < length) {
            int prn = status.getSvid(mSvCount);
            mSvCount++;
        }
    }

    @Override
    public void onGnssStarted() {

    }

    @Override
    public void onGnssStopped() {

    }

    @Override
    public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {
        /*synchronized (mFileLock) {
            if (mFileWriter == null) {
                return;
            }
            GnssClock gnssClock = event.getClock();
            for (GnssMeasurement measurement : event.getMeasurements()) {
                try {
                    writeGnssMeasurementToFile(gnssClock, measurement);
                } catch (IOException e) {
                    logException(ERROR_WRITING_FILE, e);
                }
            }
        }*/
        GnssClock clock = event.getClock();
        leapSec = clock.getLeapSecond()/1000000000;
    }

    @Override
    public void onGnssNavigationMessageReceived(GnssNavigationMessage navigationMessage) {
        /*synchronized (mFileLock) {
            if (mFileWriter == null) {
                return;
            }
            StringBuilder builder = new StringBuilder("Nav");
            builder.append(RECORD_DELIMITER);
            builder.append(navigationMessage.getSvid());
            builder.append(RECORD_DELIMITER);
            builder.append(navigationMessage.getType());
            builder.append(RECORD_DELIMITER);

            int status = navigationMessage.getStatus();
            builder.append(status);
            builder.append(RECORD_DELIMITER);
            builder.append(navigationMessage.getMessageId());
            builder.append(RECORD_DELIMITER);
            builder.append(navigationMessage.getSubmessageId());
            byte[] data = navigationMessage.getData();
            for (byte word : data) {
                builder.append(RECORD_DELIMITER);
                builder.append(word);
            }
            try {
                mFileWriter.write(builder.toString());
                mFileWriter.newLine();
            } catch (IOException e) {
                logException(ERROR_WRITING_FILE, e);
            }
        }*/
    }

    @Override
    public void onOrientationChanged(double orientation, double tilt) {

    }

    @Override
    public void sensorValue(double gyroX, double gyroY, double gyroZ, double accelX, double accelY, double accelZ, double heading) {

    }

    @Override
    public void onNmeaReceived(long timestamp, String s) {
        /*synchronized (mFileLock) {
            if (mFileWriter == null) {
                return;
            }
            String nmeaStream = String.format(Locale.US, "NMEA,%s,%d", s, timestamp);
            try {
                mFileWriter.write(nmeaStream);
                mFileWriter.newLine();
            } catch (IOException e) {
                logException(ERROR_WRITING_FILE, e);
            }
        }*/
    }

    private void writeGnssMeasurementToFile(GnssClock clock, GnssMeasurement measurement)
            throws IOException {
        String clockStream =
                String.format(
                        "Raw,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        SystemClock.elapsedRealtime(),
                        clock.getTimeNanos(),
                        clock.hasLeapSecond() ? clock.getLeapSecond() : "",
                        clock.hasTimeUncertaintyNanos() ? clock.getTimeUncertaintyNanos() : "",
                        clock.getFullBiasNanos(),
                        clock.hasBiasNanos() ? clock.getBiasNanos() : "",
                        clock.hasBiasUncertaintyNanos() ? clock.getBiasUncertaintyNanos() : "",
                        clock.hasDriftNanosPerSecond() ? clock.getDriftNanosPerSecond() : "",
                        clock.hasDriftUncertaintyNanosPerSecond()
                                ? clock.getDriftUncertaintyNanosPerSecond()
                                : "",
                        clock.getHardwareClockDiscontinuityCount() + ",");
        mFileWriter.write(clockStream);

        String measurementStream =
                String.format(
                        "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        measurement.getSvid(),
                        measurement.getTimeOffsetNanos(),
                        measurement.getState(),
                        measurement.getReceivedSvTimeNanos(),
                        measurement.getReceivedSvTimeUncertaintyNanos(),
                        measurement.getCn0DbHz(),
                        measurement.getPseudorangeRateMetersPerSecond(),
                        measurement.getPseudorangeRateUncertaintyMetersPerSecond(),
                        measurement.getAccumulatedDeltaRangeState(),
                        measurement.getAccumulatedDeltaRangeMeters(),
                        measurement.getAccumulatedDeltaRangeUncertaintyMeters(),
                        measurement.hasCarrierFrequencyHz() ? measurement.getCarrierFrequencyHz() : "",
                        measurement.hasCarrierCycles() ? measurement.getCarrierCycles() : "",
                        measurement.hasCarrierPhase() ? measurement.getCarrierPhase() : "",
                        measurement.hasCarrierPhaseUncertainty()
                                ? measurement.getCarrierPhaseUncertainty()
                                : "",
                        measurement.getMultipathIndicator(),
                        measurement.hasSnrInDb() ? measurement.getSnrInDb() : "",
                        measurement.getConstellationType(),
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                                && measurement.hasAutomaticGainControlLevelDb()
                                ? measurement.getAutomaticGainControlLevelDb()
                                : "",
                        measurement.hasCarrierFrequencyHz() ? measurement.getCarrierFrequencyHz() : "");
        mFileWriter.write(measurementStream);
        mFileWriter.newLine();
    }

    private void logException(String errorMessage, Exception e) {
        Log.e(TAG, errorMessage, e);
        Toast.makeText(mContext, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void logError(String errorMessage) {
        Log.e(TAG, errorMessage);
        Toast.makeText(mContext, errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Implements a {@link FileFilter} to delete files that are not in the
     * {@link FileToDeleteFilter#mRetainedFiles}.
     */
    private static class FileToDeleteFilter implements FileFilter {
        private final List<File> mRetainedFiles;

        public FileToDeleteFilter(File... retainedFiles) {
            this.mRetainedFiles = Arrays.asList(retainedFiles);
        }

        /**
         * Returns {@code true} to delete the file, and {@code false} to keep the file.
         *
         * <p>Files are deleted if they are not in the {@link FileToDeleteFilter#mRetainedFiles} list.
         */
        @Override
        public boolean accept(File pathname) {
            if (pathname == null || !pathname.exists()) {
                return false;
            }
            if (mRetainedFiles.contains(pathname)) {
                return false;
            }
            return pathname.length() < MINIMUM_USABLE_FILE_SIZE_BYTES;
        }
    }

    private Date nowTimeUTC () {
        Date utcTime = null;
        try {
            SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyyMMdd HHmmss");
            dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
            String UTCdate = dateFormatUTC.format(new Date());
            SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyyMMdd HHmmss");
            utcTime = dateFormatLocal.parse(UTCdate);
        } catch (java.text.ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return utcTime;
    }

    private Date satSysTime (int satType, int leapSec) {
        Date sysTime = nowTimeUTC();
        switch (satType) {
            case CONSTELLATION_GLONASS:
                satTypestr = "GLO";
                sysTime = nowTimeUTC();
                break;
            case CONSTELLATION_GPS:
                satTypestr = "GPS";
                sysTime.setTime(nowTimeUTC().getTime() + leapSec);
                break;
            case CONSTELLATION_GALILEO:
                satTypestr = "GAL";
                sysTime.setTime(nowTimeUTC().getTime() + leapSec);
                break;
            case CONSTELLATION_QZSS:
                satTypestr = "QZS";
                sysTime.setTime(nowTimeUTC().getTime() + leapSec);
                break;
            case CONSTELLATION_BEIDOU:
                satTypestr = "BDS";
                break;
        }
        return sysTime;
    }
}
