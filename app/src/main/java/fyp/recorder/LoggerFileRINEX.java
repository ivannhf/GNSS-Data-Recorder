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
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import static android.location.GnssStatus.CONSTELLATION_BEIDOU;
import static android.location.GnssStatus.CONSTELLATION_GALILEO;
import static android.location.GnssStatus.CONSTELLATION_GLONASS;
import static android.location.GnssStatus.CONSTELLATION_GPS;
import static android.location.GnssStatus.CONSTELLATION_QZSS;
import static java.lang.Math.floor;

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

    private static final String RINEX_VERSION = "3.02";
    private static final String RINEX_TYPE = "OBSERVATION DATA";
    private static final String RINEX_SYS = "M: Mixed";

    Date firstObs = null;
    private GnssStatus firstFixStatus = null;
    private GnssStatus gnssStatus = null;
    private GnssClock gnssClock = null;
    private GnssMeasurementsEvent gnssMeasurementsEvent = null;
    private Calendar GPSstart, now;
    private int leapSec = -1;
    private String satTypestr = "";
    private Integer satTypeint = -1;
    private Boolean write = false;

    private static final Double SPEED_OF_LIGHT = 299792458.0; // m/s
    private static final Double GPS_L1_FREQ = 154.0 * 10.23e6;
    private static final Double GPS_L1_WAVELENGTH = SPEED_OF_LIGHT / GPS_L1_FREQ;
    private static final Integer WEEKSECS = 604800;
    private static final Double NS_TO_S = 1.0e-9;
    private static final Double NS_TO_M = NS_TO_S * SPEED_OF_LIGHT;

    private static final int MAX_FILES_STORED = 100;
    private static final int MINIMUM_USABLE_FILE_SIZE_BYTES = 1000;

    private final Context mContext;

    private final Object mFileLock = new Object();
    private BufferedWriter mFileWriter;
    private File mFile;
    public String outFilePath = "", outFileName = "";

    private Timer timer;
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {

            if (mFileWriter != null) {
                writeRecord1();
            }
            //Log.d(TAG, "write");
        }
    };

    private LogFragment.UIFragmentComponent mUiComponent;

    public synchronized LogFragment.UIFragmentComponent getUiComponent() {
        return mUiComponent;
    }

    public synchronized void setUiComponent(LogFragment.UIFragmentComponent value) {
        mUiComponent = value;
    }

    public LoggerFileRINEX(Context context) {
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
            outFileName = fileName;
            File currentFile = new File(baseDirectory, fileName);
            String currentFilePath = currentFile.getAbsolutePath();
            outFilePath = currentFilePath;
            BufferedWriter currentFileWriter;
            try {
                currentFileWriter = new BufferedWriter(new FileWriter(currentFile));
            } catch (IOException e) {
                logException("Could not open file: " + currentFilePath, e);
                return;
            }

            // initialize the contents of the file
            try {
                //currentFileWriter.write("----|---1|0---|---2|0---|---3|0---|---4|0---|---5|0---|---6|0---|---7|0---|---8|0\n");
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
                currentFileWriter.write("G" + String.format("%5s", "4") + String.format("%4s", "C1C") + String.format("%4s", "L1C") + String.format("%4s", "D1C") + String.format("%4s", "S1C") + String.format("%38s", "") + "SYS / # / OBS TYPES");
                currentFileWriter.newLine();
                currentFileWriter.write("R" + String.format("%5s", "4") + String.format("%4s", "C1C") + String.format("%4s", "L1C") + String.format("%4s", "D1C") + String.format("%4s", "S1C") + String.format("%38s", "") + "SYS / # / OBS TYPES");
                currentFileWriter.newLine();
                currentFileWriter.write("E" + String.format("%5s", "4") + String.format("%4s", "C1B") + String.format("%4s", "L1B") + String.format("%4s", "D1B") + String.format("%4s", "S1B") + String.format("%38s", "") + "SYS / # / OBS TYPES");
                currentFileWriter.newLine();
                currentFileWriter.write("C" + String.format("%5s", "4") + String.format("%4s", "C1I") + String.format("%4s", "L1I") + String.format("%4s", "D1I") + String.format("%4s", "S1I") + String.format("%38s", "") + "SYS / # / OBS TYPES");
                currentFileWriter.newLine();
                do {
                    //Log.d(TAG, "null");
                } while (((firstFixStatus == null) || (leapSec == -1)) && (gnssClock == null));
                firstObs = getTime();
                //firstObs = satSysTime(firstFixStatus.getConstellationType(0), leapSec);
                String year = String.format("%1$tY", firstObs);
                String month = String.format("%1$tm", firstObs);
                String day = String.format("%1$te", firstObs);
                String hour = String.format("%02d", Integer.parseInt(String.format("%1$tk", date)));
                String min = String.format("%02d", Integer.parseInt(String.format("%1$tM", date)));
                Double sec_db = Double.parseDouble(String.format("%1$tS", firstObs)) + Double.parseDouble(String.format("%1$tL", firstObs)) / 1000.0 + Double.parseDouble(String.format("%1$tL", firstObs)) / 1000000000.0;
                String sec = String.format("%02.7f", sec_db);
                currentFileWriter.write(String.format("%6s", year) + String.format("%6s", month) + String.format("%6s", day)
                        + String.format("%6s", hour) + String.format("%6s", min) + String.format("%13s", sec)
                        + String.format("%8s", satTypestr) + String.format("%9s", "") + "TIME OF FIRST OBS");

                currentFileWriter.newLine();
                currentFileWriter.write("G" + String.format("%59s", "") + "SYS / PHASE SHIFTS");
                currentFileWriter.newLine();
                currentFileWriter.write("R" + String.format("%59s", "") + "SYS / PHASE SHIFTS");
                currentFileWriter.newLine();
                currentFileWriter.write("E" + String.format("%59s", "") + "SYS / PHASE SHIFTS");
                currentFileWriter.newLine();
                currentFileWriter.write("C" + String.format("%59s", "") + "SYS / PHASE SHIFTS");
                currentFileWriter.newLine();
                //currentFileWriter.write(String.format("%-59s", leapSec) + " LEAP SECONDS");
                //currentFileWriter.newLine();


                currentFileWriter.write(String.format("%-60s", "") + "END OF HEADER");
                currentFileWriter.newLine();
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

            if (timer == null) {
                timer = new Timer();
                timer.scheduleAtFixedRate(timerTask, 0, 1000);
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
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

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
        gnssStatus = status;
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
        gnssClock = event.getClock();
        leapSec = gnssClock.getLeapSecond() / 1000000000;

        gnssMeasurementsEvent = event;
    }

    @Override
    public void onGnssNavigationMessageReceived(GnssNavigationMessage navigationMessage) {
    }

    @Override
    public void onOrientationChanged(double orientation, double tilt) {

    }

    @Override
    public void sensorValue(double gyroX, double gyroY, double gyroZ, double accelX, double accelY, double accelZ, double heading) {

    }

    @Override
    public void onNmeaReceived(long timestamp, String s) {
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
         * <p>
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

    private Date nowTimeUTC() {
        Date utcTime = null;
        try {
            SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyyMMdd HHmmss");
            dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
            String UTCdate = dateFormatUTC.format(new Date());
            SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyyMMdd HHmmss");
            utcTime = dateFormatLocal.parse(UTCdate);
            //Log.d(TAG, UTCdate.toString());
        } catch (java.text.ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return utcTime;
    }

    private Date getTime() {
        Date utcTime = null;

        Long fullbiasnanos = gnssClock.getFullBiasNanos();
        Long timenanos = gnssClock.getTimeNanos();
        double biasnanos = gnssClock.getBiasNanos();

        int gpsweek = (int) Math.floor(-fullbiasnanos * 1.0e-9 / WEEKSECS);
        double local_est_GPS_time = timenanos - (fullbiasnanos + biasnanos);
        double gpssow = local_est_GPS_time * 1.0e-9 - gpsweek * WEEKSECS;
        double frac = gpssow - (gpssow % 1);

        // define GPS time start
        GPSstart = Calendar.getInstance();
        GPSstart.set(Calendar.YEAR, 1980);
        GPSstart.set(Calendar.MONTH, 0);
        GPSstart.set(Calendar.DATE, 6);
        GPSstart.set(Calendar.HOUR, 0);
        GPSstart.set(Calendar.MINUTE, 0);
        GPSstart.set(Calendar.SECOND, 0);

        GPSstart.add(Calendar.WEEK_OF_YEAR, gpsweek);
        GPSstart.add(Calendar.SECOND, (int) gpssow);
        //GPSstart.add(Calendar.HOUR, -12);

        try {
            SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyyMMdd HHmmss");
            dateFormatUTC.setTimeZone(TimeZone.getTimeZone("Etc/GMT+4"));
            String UTCdate = dateFormatUTC.format(GPSstart.getTime());
            SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyyMMdd HHmmss");
            utcTime = dateFormatLocal.parse(UTCdate);
            Log.d(TAG, utcTime + "");
        } catch (java.text.ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return utcTime;
    }

    private Date satSysTime(int satType, int leapSec) {
        Date sysTime = nowTimeUTC();
        switch (satType) {
            case CONSTELLATION_GLONASS:
                satTypestr = "GLO";
                satTypeint = CONSTELLATION_GLONASS;
                sysTime = nowTimeUTC();
                break;
            case CONSTELLATION_GPS:
                satTypestr = "GPS";
                satTypeint = CONSTELLATION_GPS;
                sysTime.setTime(nowTimeUTC().getTime() + leapSec);
                break;
            case CONSTELLATION_GALILEO:
                satTypestr = "GAL";
                satTypeint = CONSTELLATION_GALILEO;
                sysTime.setTime(nowTimeUTC().getTime() + leapSec);
                break;
            case CONSTELLATION_QZSS:
                satTypestr = "QZS";
                satTypeint = CONSTELLATION_QZSS;
                sysTime.setTime(nowTimeUTC().getTime() + leapSec);
                break;
            case CONSTELLATION_BEIDOU:
                satTypestr = "BDS";
                satTypeint = CONSTELLATION_BEIDOU;
                break;
        }
        return sysTime;
    }

    private void writeRecord1() {
        GnssMeasurementsEvent localMeasurementsEvent = gnssMeasurementsEvent;
        GnssClock localClock = gnssClock;
        GnssStatus localStatus = gnssStatus;

        Date date = satSysTime(satTypeint, leapSec);

        Integer satCount = 0;

        double firstFullBiasNanos = 0;
        boolean getfirstFullBiasNanos = false;

        Set<String> svidSet = new HashSet<>();
        Set<String> c1Set = new HashSet<>();
        Set<String> l1Set = new HashSet<>();
        Set<String> d1Set = new HashSet<>();
        Set<String> s1Set = new HashSet<>();

        String[] svidArr =new String[100];
        String[] c1Arr = new String[100];
        String[] l1Arr = new String[100];
        String[] s1Arr = new String[100];
        String[] d1Arr = new String[100];

        int recCount = 0;

        for (GnssMeasurement measurement : localMeasurementsEvent.getMeasurements()) {
            boolean satSkip = false;

            if (!getfirstFullBiasNanos) firstFullBiasNanos = gnssClock.getFullBiasNanos();

            String svid = "";
            Integer prn = measurement.getSvid();
            String prnStr = Integer.toString(prn);
            /*if (prn < 10) {
                prnStr = "0" + prn;
            } else prnStr = "" + prn;*/
            if (measurement.getConstellationType() == CONSTELLATION_GPS) {
                svid = String.format("G%1$02d", prn);
            } else if (measurement.getConstellationType() == CONSTELLATION_GLONASS) {
                if (prn >= 93) {
                    Log.d(TAG, "skip measurement");
                    satSkip = true;
                    continue;
                } else svid = String.format("R%1$02d", prn);
            } else if (measurement.getConstellationType() == CONSTELLATION_GALILEO) {
                svid = String.format("E%1$02d", prn);
            } else if (measurement.getConstellationType() == CONSTELLATION_BEIDOU) {
                svid = String.format("C%1$02d", prn);
            } else if (measurement.getConstellationType() == CONSTELLATION_QZSS) {
                svid = String.format("J%1$02d", prn - 192);
            } else if (measurement.getConstellationType() == GnssStatus.CONSTELLATION_SBAS) {
                Log.d(TAG, "skip measurement");
                satSkip = true;
                continue;
            } else {
                Log.d(TAG, "skip measurement");
                satSkip = true;
                continue;
            }

            Log.d(TAG, svid);

            if(satSkip) continue;

            double timeNanos = gnssClock.getTimeNanos();
            double timeOffsetNanos = measurement.getTimeOffsetNanos();
            double ReceivedSvTimeNanos = measurement.getReceivedSvTimeNanos();

            long fullBiasNanos;
            if (gnssClock.hasFullBiasNanos()) {
                fullBiasNanos = gnssClock.getFullBiasNanos();
            } else fullBiasNanos = 0L;

            double biasnanos;
            if (gnssClock.hasBiasNanos()) {
                biasnanos = gnssClock.getBiasNanos();
            } else biasnanos = 0.0;

            double timeUncertaintyNanos;
            if (gnssClock.hasTimeUncertaintyNanos()) {
                timeUncertaintyNanos = gnssClock.getTimeUncertaintyNanos();
            } else timeUncertaintyNanos = 0.0;


            int weekNum = (int) floor(-(double) fullBiasNanos * 1.0e9 / WEEKSECS);
            int weekNanos = (int) (WEEKSECS * 1.0e9);
            int weekNumNanos = weekNum * weekNanos;

            double tRxNanos = timeNanos - firstFullBiasNanos - weekNumNanos;

            double tRxSeconds = ((double) tRxNanos - timeOffsetNanos - biasnanos) * 1.0e-9;
            double tTxSeconds = (double) (ReceivedSvTimeNanos * 1.0e-9);

            double prSeconds = tRxSeconds - tTxSeconds;

            while (prSeconds > (WEEKSECS / 2)) {
                double prS = prSeconds;
                double delS = Math.round(prS / WEEKSECS) * WEEKSECS;
                prS -= delS;
                int maxBiasSeconds = 10;
                if (prS > maxBiasSeconds) {
                    satSkip = true;
                    break;
                } else {
                    prSeconds = prS;
                    tRxSeconds -= delS;
                }
            }

            //if(satSkip) continue;

            double local_est_GPS_time = timeNanos - (fullBiasNanos + biasnanos);
            double gpssow = local_est_GPS_time * 1.0e-9 - weekNum * WEEKSECS;
            //date = new Date((new Date (1980, 1, 6)).getTime() + (weekNum * WEEKSECS + (int) (Math.floor(gpssow))) * 1000);

            Double c1 = prSeconds * SPEED_OF_LIGHT;

            Double l1 = -measurement.getAccumulatedDeltaRangeMeters() / GPS_L1_WAVELENGTH;

            Double d1 = -measurement.getPseudorangeRateMetersPerSecond() / GPS_L1_WAVELENGTH;

            String obsStr = String.format("%.3f", c1);
            String LL1Str = String.format("%.3f", l1);
            String singalStr = String.format("%.3f", measurement.getCn0DbHz());
            String d1Str = String.format("%.3f", d1);

            svidSet.add(svid);
            c1Set.add(obsStr);
            l1Set.add(LL1Str);
            s1Set.add(singalStr);
            d1Set.add(d1Str);

            svidArr[recCount] = svid;
            c1Arr[recCount] = obsStr;
            l1Arr[recCount] = LL1Str;
            s1Arr[recCount] = singalStr;
            d1Arr[recCount] = d1Str;

            recCount++;
        }

        /*String[] svidArr = svidSet.toArray(new String[]{});
        String[] c1Arr = c1Set.toArray(new String[]{});
        String[] l1Arr = l1Set.toArray(new String[]{});
        String[] s1Arr = s1Set.toArray(new String[]{});
        String[] d1Arr = d1Set.toArray(new String[]{});*/

        //Date
        date = getTime();
        String year = String.format("%1$tY", date);
        String month = String.format("%1$tm", date);
        String day = String.format("%1$te", date);
        String hour = String.format("%02d", Integer.parseInt(String.format("%1$tk", date)));
        String min = String.format("%02d", Integer.parseInt(String.format("%1$tM", date)));
        Double sec_db = Double.parseDouble(String.format("%1$tS", date)) + Double.parseDouble(String.format("%1$tL", date)) / 1000.0 + Double.parseDouble(String.format("%1$tL", date)) / 1000000000.0;
        String sec = String.format("%02.7f", sec_db);
        if (gnssStatus.getSatelliteCount() > 0) {
            satCount = gnssStatus.getSatelliteCount();
        } else satCount = 0;
        try {
            mFileWriter.write(">" + String.format("%5s", year) + String.format("%3s", month) + String.format("%3s", day)
                    + String.format("%3s", hour) + String.format("%3s", min) + String.format("%11s", sec)
                    + String.format("%3s", "0") + String.format("%3s", svidSet.size() - 1));
            mFileWriter.newLine();
        } catch (IOException e) {
            Log.d(TAG, "fail");
        }

        //Log.d(TAG, "Write record: " + recCount + " " + svidArr.length + " " + c1Arr.length + " " + l1Arr.length + " " + s1Arr.length + " " + d1Arr.length);

        if (recCount < 1) return;
        for (int i = 1; i < recCount; i++) {
            try {
                mFileWriter.write(svidArr[i] + String.format("%14s", c1Arr[i]) + String.format("%14s", l1Arr[i]) + String.format("%14s", d1Arr[i]) + String.format("%14s", s1Arr[i]));
                mFileWriter.newLine();
            } catch (IOException e) {
                logException(ERROR_WRITING_FILE, e);
                Log.d(TAG, "cannot write");
            }
        }
    }

    private void writeRecord() {
        GnssMeasurementsEvent localMeasurementsEvent = gnssMeasurementsEvent;
        GnssClock localClock = gnssClock;
        GnssStatus localStatus = gnssStatus;

        Integer satCount = 0;

        double firstFullBiasNanos = 0;
        boolean getfirstFullBiasNanos = false;

        double[] c1Set = new double[]{};
        double[] l1Set = new double[]{};
        double[] d1Set = new double[]{};
        double[] s1Set = new double[]{};
        int recCount = 0;

        /*Long fullbiasnanos = 0L;
        Double biasnanos = 0.0;
        Double gpsweek = 0.0;
        Double local_est_GPS_time = 0.0;
        Double gpssow = 0.0;
        Double TimeOffsetNanos = 0.0;
        Double tRxSeconds = 0.0;
        Double tTxSeconds = 0.0;
        Double travelTime = 0.0;*/

        for (GnssMeasurement measurement : localMeasurementsEvent.getMeasurements()) {
            Integer type = measurement.getConstellationType();
            Integer prn = measurement.getSvid();
            switch (type) {
                case CONSTELLATION_GPS:
                case CONSTELLATION_GLONASS:
                case CONSTELLATION_GALILEO:
                case CONSTELLATION_BEIDOU:
                    if (type != CONSTELLATION_GLONASS) {
                        satCount++;
                    } else if ((type == CONSTELLATION_GLONASS) && (prn < 93)) satCount++;
                    break;
            }
        }

        Date date = satSysTime(satTypeint, leapSec);
        String year = String.format("%1$tY", date);
        String month = String.format("%1$tm", date);
        String day = String.format("%1$te", date);
        String hour = String.format("%1$tk", date);
        String min = String.format("%1$tM", date);
        Double sec_db = Double.parseDouble(String.format("%1$tS", date)) + Double.parseDouble(String.format("%1$tL", date)) / 1000.0 + Double.parseDouble(String.format("%1$tL", date)) / 1000000000.0;
        String sec = String.format("%.7f", sec_db);
        if (gnssStatus.getSatelliteCount() > 0) {
            satCount = gnssStatus.getSatelliteCount();
        } else satCount = 0;
        try {
            mFileWriter.write(">" + String.format("%5s", year) + String.format("%3s", month) + String.format("%3s", day)
                    + String.format("%3s", hour) + String.format("%3s", min) + String.format("%11s", sec)
                    + String.format("%3s", "0") + String.format("%3s", satCount + ""));
            mFileWriter.newLine();
        } catch (IOException e) {
            Log.d(TAG, "fail");
        }

        for (GnssMeasurement measurement : localMeasurementsEvent.getMeasurements()) {
            if (!getfirstFullBiasNanos) firstFullBiasNanos = gnssClock.getFullBiasNanos();

            String svid = "";
            Integer prn = measurement.getSvid();
            String prnStr = "";
            if (prn < 10) {
                prnStr = "0" + prn;
            } else prnStr = "" + prn;
            if (measurement.getConstellationType() == CONSTELLATION_GPS) {
                svid = String.format("G%s", prnStr);
            } else if (measurement.getConstellationType() == CONSTELLATION_GLONASS) {
                if (prn >= 93) {
                    Log.d(TAG, "skip measurement");
                    continue;
                } else svid = String.format("R%s", prnStr);
            } else if (measurement.getConstellationType() == CONSTELLATION_GALILEO) {
                svid = String.format("E%s", prnStr);
            } else if (measurement.getConstellationType() == CONSTELLATION_BEIDOU) {
                svid = String.format("C%s", prnStr);
            } else if (measurement.getConstellationType() == CONSTELLATION_QZSS) {
                Log.d(TAG, "skip measurement");
                continue;
            } else if (measurement.getConstellationType() == GnssStatus.CONSTELLATION_SBAS) {
                Log.d(TAG, "skip measurement");
                continue;
            } else {
                Log.d(TAG, "skip measurement");
                continue;
            }

            double timeNanos = gnssClock.getTimeNanos();
            double timeOffsetNanos = measurement.getTimeOffsetNanos();
            double ReceivedSvTimeNanos = measurement.getReceivedSvTimeNanos();

            long fullBiasNanos;
            if (gnssClock.hasFullBiasNanos()) {
                fullBiasNanos = gnssClock.getFullBiasNanos();
            } else fullBiasNanos = 0L;

            double biasnanos;
            if (gnssClock.hasBiasNanos()) {
                biasnanos = gnssClock.getBiasNanos();
            } else biasnanos = 0.0;

            double timeUncertaintyNanos;
            if (gnssClock.hasTimeUncertaintyNanos()) {
                timeUncertaintyNanos = gnssClock.getTimeUncertaintyNanos();
            } else timeUncertaintyNanos = 0.0;


            int weekNum = (int) floor(-(double) fullBiasNanos * 1.0e9 / WEEKSECS);
            int weekNanos = (int) (WEEKSECS * 1.0e9);
            int weekNumNanos = weekNum * weekNanos;

            double tRxNanos = timeNanos - firstFullBiasNanos - weekNumNanos;

            double tRxSeconds = ((double) tRxNanos - timeOffsetNanos - biasnanos) * 1.0e-9;
            double tTxSeconds = (double) (ReceivedSvTimeNanos * 1.0e-9);

            double prSeconds = tRxSeconds - tTxSeconds;

            if (prSeconds > (WEEKSECS / 2)) {
                double prS = prSeconds;
                double delS = Math.round(prS / WEEKSECS) * WEEKSECS;
                prS -= delS;
                int maxBiasSeconds = 10;
                if (prS > maxBiasSeconds) {
                    continue;
                } else {
                    prSeconds = prS;
                    tRxSeconds -= delS;
                }
            }

            /*gpsweek = fullbiasnanos * NS_TO_S / WEEKSECS;
            local_est_GPS_time = gnssClock.getTimeNanos() - (fullbiasnanos + biasnanos);
            gpssow = local_est_GPS_time * NS_TO_S - gpsweek * WEEKSECS;

            Double temp = gpssow + 0.5;
            Double fracPart = gpssow % 1;

            if (measurement.getTimeOffsetNanos() > 0) {
                TimeOffsetNanos = measurement.getTimeOffsetNanos();
            } else TimeOffsetNanos = 0.0;

            int weekNo = (int) (Math.floor(-1 * fullbiasnanos * 1.0e-9 / WEEKSECS));
            int weekNoSecs = weekNo * WEEKSECS;
            Double secOfWeek = (-1 * fullbiasnanos * 1.0e-9) - weekNoSecs - biasnanos * 1.0e-9;

            tRxSeconds = secOfWeek + gnssClock.getTimeNanos() * 1.0e-9 + measurement.getTimeOffsetNanos() * 1.0e-9;
            tTxSeconds = measurement.getReceivedSvTimeNanos() * 1.0e-9;
            travelTime = tRxSeconds - tTxSeconds;*/


            Double c1 = prSeconds * SPEED_OF_LIGHT;

            Double l1 = -measurement.getAccumulatedDeltaRangeMeters() / GPS_L1_WAVELENGTH;

            Double d1 = -measurement.getPseudorangeRateMetersPerSecond() / GPS_L1_WAVELENGTH;

            String obsStr = String.format("%.3f", c1);
            String LL1Str = String.format("%.3f", l1);
            String singalStr = String.format("%.3f", measurement.getCn0DbHz());
            String d1Str = String.format("%.3f", d1);

            try {
                //writeGnssMeasurementToFile(gnssClock, measurement);
                mFileWriter.write(svid + String.format("%14s", obsStr) + String.format("%14s", LL1Str) + String.format("%14s", singalStr) + String.format("%14s", d1Str));
                mFileWriter.newLine();
            } catch (IOException e) {
                logException(ERROR_WRITING_FILE, e);
            }
        }

    }
}
