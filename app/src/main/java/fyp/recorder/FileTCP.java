package fyp.recorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.GnssClock;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import fyp.layout.R;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WIFI_SERVICE;

public class FileTCP implements MainActivityListener {

    private static final String TAG = "SendFile";
    private Context mContext;

    private Socket socket;
    private ServerSocket serverSocket;
    private BufferedReader bufferedReader;

    private PrintWriter printWriter;

    private String IP = "192.168.0.122";
    private int PORT = 8080;
    private String loginName = "anonymous";
    private String loginPW = "";

    private String TCPIP = "192.168.0.0";
    private int TCPPort = 8080;
    private String TCPuser = "User";

    public String message = "";

    String rawName = "", rinexName = "", nmeaName = "";
    String[] path = new String[]{"", "", ""};
    String[] prefix = new String[]{"/Raw", "/RINEX", "/NMEA"};
    List<String> logPath;

    String filePath = "";
    String fileName = "";
    int fileType = 0;

    //int i = 0;

    public FileTCP() {
        mContext = MainActivity.getInstance().context;

        MainActivity.getInstance().addListener(this);
    }

    public void sendFile(List<String> path) {
        this.mContext = MainActivity.getInstance().context;

        logPath = path;

        SharedPreferences setting = mContext.getSharedPreferences("settings", MODE_PRIVATE);
        IP = setting.getString(mContext.getString(R.string.pref_key_ip_address), "");
        PORT = Integer.parseInt(setting.getString(mContext.getString(R.string.pref_key_port), "8080"));
        loginName = setting.getString(mContext.getString(R.string.pref_key_ftp_login_name), "anonymous");
        loginPW = setting.getString(mContext.getString(R.string.pref_key_ftp_login_pw), "");

        if(loginName.compareTo("") == 0) {
            loginName = "anonymous";
            loginPW = "";
        }

        TaskFTP taskFTP = new TaskFTP();
        taskFTP.execute();
    }

    public void sendMsg(String msg) {
        this.mContext = MainActivity.getInstance().context;

        SharedPreferences setting = mContext.getSharedPreferences("settings", MODE_PRIVATE);
        TCPIP = setting.getString(mContext.getString(R.string.pref_key_tcp_ip_address), "");
        TCPPort = setting.getInt(mContext.getString(R.string.pref_key_tcp_port), 8080);
        TCPuser = setting.getString(mContext.getString(R.string.pref_key_tcp_user), "User");
        message = getIP() + "," + Build.MODEL + "," + TCPuser + "," + msg;

        MsgTCP msgTCP = new MsgTCP();
        msgTCP.execute();
    }

    public String getIP() {
        WifiManager wm = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        return ip;
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
        GnssClock clock = event.getClock();
        for (GnssMeasurement measurement : event.getMeasurements()) {
            try {
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

                sendMsg(clockStream + measurementStream);
            } catch (Exception e) {

            }
        }
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

    class TaskFTP extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                //String pathPrefix = Environment.getExternalStorageDirectory().toString() + "/AAE01_GNSS_Data";

                FTPClient ftpClient = new FTPClient();
                ftpClient.connect(IP, PORT);

                if(ftpClient.login(loginName, loginPW)) {

                    ftpClient.setSoTimeout(100000);
                    ftpClient.enterLocalPassiveMode();

                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                    ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                    FileInputStream fs = null;

                    for(String pathStr : logPath) {
                        if (pathStr.compareTo("") == 0) continue;

                        //File upFile = new File(Environment.getExternalStorageDirectory().toString() + "/" + pathStr);

                        File upFile = new File(pathStr);

                        Log.d(TAG, "Sending " + upFile + " " + upFile.exists());

                        fs = new FileInputStream(upFile);

                        Log.d(TAG, "Sent " + ftpClient.storeFile(upFile.getName(), fs));

                        fs.close();
                    }

                    ftpClient.logout();
                    ftpClient.disconnect();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class MsgTCP extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                socket = new Socket(TCPIP, TCPPort);
                printWriter = new PrintWriter(socket.getOutputStream());
                printWriter.write(message);

                printWriter.flush();
                printWriter.close();
                socket.close();
            } catch (IOException e) {

            }
            return null;
        }
    }

}
