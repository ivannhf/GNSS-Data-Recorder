package fyp.recorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    String rawName = "", rinexName = "", nmeaName = "";
    String[] path = new String[] {"", "", ""};
    String[] prefix = new String[] {"/Raw", "/RINEX", "/NMEA"};

    String filePath = "";
    String fileName = "";

    //int i = 0;

    public void sendFile (String RawName, String RINEXName, String NMEAName) {
        this.mContext = MainActivity.getInstance().context;

        rawName = RawName;
        rinexName = RINEXName;
        nmeaName = NMEAName;

        path[0] = RawName;
        path[1] = RINEXName;
        path[2] = NMEAName;

        //Log.d(TAG, "sending: " + rawPath + " to " + IP + ":" + PORT);

        SharedPreferences setting = mContext.getSharedPreferences("settings", MODE_PRIVATE);
        IP = setting.getString(mContext.getString(R.string.pref_key_ip_address), "");
        PORT = Integer.parseInt(setting.getString(mContext.getString(R.string.pref_key_port), "8080"));

        Task_t task = new Task_t();
        task.execute();

        /*for (int i = 0; i < 2; i++) {
            if(path[i] == "") continue;
            filePath = Environment.getExternalStorageDirectory().toString() + "/AAE01_GNSS_Data" + prefix[i];
            fileName = path[i];
            Task_t task = new Task_t();
            task.execute();
        }*/
    }

    class Task extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground (Void... params) {
            try{
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

            } catch (IOException e){

            }
            return null;
        }
    }

    class Task_t extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try{
                socket = new Socket(IP, PORT);

                BufferedOutputStream outRaw = new BufferedOutputStream(socket.getOutputStream());
                DataOutputStream dosRaw = new DataOutputStream(outRaw);

                /*BufferedOutputStream outRinex = new BufferedOutputStream(socket.getOutputStream());
                DataOutputStream dosRinex = new DataOutputStream(outRinex);

                BufferedOutputStream outNmea = new BufferedOutputStream(socket.getOutputStream());
                DataOutputStream dosNmea = new DataOutputStream(outNmea);*/

                File rawfile = null;
                File rinexfile = null;
                File nmeafile = null;

                String pathPrefix = Environment.getExternalStorageDirectory().toString() + "/AAE01_GNSS_Data";

                //if (path[0] != "") {
                    String rawPath = pathPrefix + prefix[0];
                    rawfile = new File(rawPath, rawName);
               // }
                /*if (path[1] != "") {
                    String rinexPath = pathPrefix + prefix[1];
                    rinexfile = new File(rinexPath, rinexName);
                }
                if (path[2] != "") {
                    String nmeaPath = pathPrefix + prefix[2];
                    nmeafile = new File(nmeaPath, nmeaName);
                }*/

                dosRaw.writeUTF(rawName);
                /*dosRinex.writeUTF(rinexName);
                dosNmea.writeUTF(nmeaName);*/

                //if (rawfile != null) {
                    Files.copy(rawfile.toPath(), dosRaw);
                //}
                /*if (rinexfile != null) {
                    Files.copy(rinexfile.toPath(), dosRinex);
                }
                if (nmeafile != null) {
                    Files.copy(nmeafile.toPath(), dosNmea);
                }*/

                outRaw.close();
                dosRaw.close();

                /*outRinex.close();
                dosRinex.close();

                outNmea.close();
                dosNmea.close();*/

                //long length = file.length();

                //d.writeUTF(fileName);
                //Files.copy(file.toPath(), d);

                //out.close();
                //d.close();
                socket.close();

                Log.d(TAG, "file finish");
            } catch (IOException e){

            }
            return null;
        }
    }

}
