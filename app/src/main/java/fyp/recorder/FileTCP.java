package fyp.recorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

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

import fyp.layout.R;

import static android.content.Context.MODE_PRIVATE;

public class FileTCP {

    private static final String TAG = "SendFile";
    private Context mContext;

    private Socket socket;
    private ServerSocket serverSocket;
    private BufferedReader bufferedReader;

    private PrintWriter printWriter;

    private String IP = "192.168.0.122";
    private int PORT = 8080;

    public String echo = "";

    String rawName = "", rinexName = "", nmeaName = "";
    String[] path = new String[]{"", "", ""};
    String[] prefix = new String[]{"/Raw", "/RINEX", "/NMEA"};

    String filePath = "";
    String fileName = "";
    int fileType = 0;

    //int i = 0;

    public void sendFile(String RawName, int type) {
        this.mContext = MainActivity.getInstance().context;

        rawName = RawName;

        fileType = type;
        /*rinexName = RINEXName;
        nmeaName = NMEAName;

        path[0] = RawName;
        path[1] = RINEXName;
        path[2] = NMEAName;*/

        //Log.d(TAG, "sending: " + rawPath + " to " + IP + ":" + PORT);

        SharedPreferences setting = mContext.getSharedPreferences("settings", MODE_PRIVATE);
        IP = setting.getString(mContext.getString(R.string.pref_key_ip_address), "");
        PORT = Integer.parseInt(setting.getString(mContext.getString(R.string.pref_key_port), "8080"));

        TaskFTP taskFTP = new TaskFTP();
        taskFTP.execute();
    }

    class TaskFTP extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                FTPClient ftpClient = new FTPClient();
                ftpClient.connect(IP, PORT);

                ftpClient.setSoTimeout(100000);
                ftpClient.enterLocalPassiveMode();

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);

                String pathPrefix = Environment.getExternalStorageDirectory().toString() + "/AAE01_GNSS_Data";
                String rawPath = pathPrefix + prefix[fileType];

                File file = new File(rawPath, rawName);

                FileInputStream fs = new FileInputStream(file);

                ftpClient.storeFile(fileName, fs);
                fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class Task extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                socket = new Socket(IP, PORT);

                //filePath = Environment.getExternalStorageDirectory().toString() + "/AAE01_GNSS_Data";
                //fileName = "test.txt";

                File file = new File(filePath, fileName);
                long length = file.length();

                byte[] bytes = new byte[4096];
                InputStream in = new FileInputStream(file);
                OutputStream out = socket.getOutputStream();
                int count;
                while ((count = in.read(bytes)) > 0) {
                    out.write(bytes, 0, count);
                }
                Log.d(TAG, "JPG finish");

                out.close();
                in.close();
                socket.close();

                /*printWriter = new PrintWriter(socket.getOutputStream());
                printWriter.write("Success");

                printWriter.flush();
                printWriter.close();
                in.close();
                out.close();
                socket.close();*/

            } catch (IOException e) {

            }
            return null;
        }
    }

    class Task_t extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                socket = new Socket(IP, PORT);

                BufferedOutputStream outRaw = new BufferedOutputStream(socket.getOutputStream());
                DataOutputStream dosRaw = new DataOutputStream(outRaw);

                File rawfile = null;

                String pathPrefix = Environment.getExternalStorageDirectory().toString() + "/AAE01_GNSS_Data";
                String rawPath = pathPrefix + prefix[fileType];

                rawfile = new File(rawPath, rawName);

                dosRaw.writeUTF(rawName);

                Files.copy(rawfile.toPath(), dosRaw);

                outRaw.close();
                dosRaw.close();
                socket.close();

                Log.d(TAG, "file finish");
            } catch (IOException e) {

            }
            return null;
        }
    }

}
