package fyp.recorder;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

                byte[] bytes = new byte[1024];
                InputStream is = socket.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                int bytesRead = is.read(bytes, 0, bytes.length);
                bos.write(bytes, 0, bytesRead);
                bos.close();
                Log.d(TAG, "JPG finish");

                printWriter = new PrintWriter(socket.getOutputStream());
                printWriter.write("Success");

                printWriter.flush();
                printWriter.close();
                socket.close();

            } catch (IOException e){

            }
            return null;
        }
    }

}
