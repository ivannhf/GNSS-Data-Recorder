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

    String rawPath = "", rinexPath = "", nmeaPath = "";
    String[] path = new String[] {"", "", ""};
    String[] prefix = new String[] {"/Raw", "/RINEX", "/NMEA"};

    String filePath = "";
    String fileName = "";

    //int i = 0;

    public void sendFile (String RawName, String RINEXName, String NMEAName) {
        this.mContext = MainActivity.getInstance().context;

        rawPath = RawName;
        rinexPath = RINEXName;
        nmeaPath = NMEAName;

        path[0] = RawName;
        path[1] = RINEXName;
        path[2] = NMEAName;

        Log.d(TAG, "sending: " + rawPath + " to " + IP + ":" + PORT);

        SharedPreferences setting = mContext.getSharedPreferences("settings", MODE_PRIVATE);
        IP = setting.getString(mContext.getString(R.string.pref_key_ip_address), "");
        PORT = Integer.parseInt(setting.getString(mContext.getString(R.string.pref_key_port), "8080"));

        for (int i = 0; i < 2; i++) {
            if(path[i] == "") continue;
            filePath = Environment.getExternalStorageDirectory().toString() + "/AAE01_GNSS_Data" + prefix[i];
            fileName = path[i];
            Task_t task = new Task_t();
            task.execute();
        }
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

                File file = new File(filePath, fileName);
                long length = file.length();

                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
                DataOutputStream d = new DataOutputStream(out);

                d.writeUTF(fileName);
                Files.copy(file.toPath(), d);

                out.close();
                d.close();
                socket.close();

                Log.d(TAG, "file finish");
            } catch (IOException e){

            }
            return null;
        }
    }

}
