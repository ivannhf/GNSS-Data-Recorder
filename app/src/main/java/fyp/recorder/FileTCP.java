package fyp.recorder;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTCP {

    private static final String TAG = "SendFile";

    private Socket socket;
    private ServerSocket serverSocket;
    private BufferedReader bufferedReader;

    private PrintWriter printWriter;

    private String IP = "192.168.0.122";
    private int PORT = 25565;

    String rawPath = "", rinexPath = "", nmeaPath = "";

    public void sendFile (String RawPath, String RINEXPath, String NMEAPath) {
        rawPath = RawPath;
        rinexPath = RINEXPath;
        nmeaPath = NMEAPath;

        Task task = new Task();
        task.execute();
    }

    class Task extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground (Void... params) {
            try{
                socket = new Socket(IP, PORT);

                File file = new File("/storage/emulated/0/AAE01_GNSS_Data/test.jpg");
                long length = file.length();

                byte[] bytes = new byte[64 * 1024];
                InputStream in = new FileInputStream(file);
                OutputStream out = socket.getOutputStream();
                int count;
                while ((count = in.read(bytes)) > 0) {
                    out.write(bytes, 0, count);
                }
                Log.d(TAG, "JPG finish");

                printWriter = new PrintWriter(socket.getOutputStream());
                printWriter.write("Success");

                printWriter.flush();
                printWriter.close();
                in.close();
                out.close();
                socket.close();

            } catch (IOException e){

            }
            return null;
        }
    }

}
