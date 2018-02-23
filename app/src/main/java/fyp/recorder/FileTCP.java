package fyp.recorder;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class FileTCP {

    private Socket socket;
    private ServerSocket serverSocket;
    private BufferedReader bufferedReader;

    private PrintWriter printWriter;

    private String IP = "192.168.0.122";
    private int PORT = 25565;

    public void sendFile () {
        Task task = new Task();
        task.execute();
    }

    class Task extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground (Void... params) {
            try{
                socket = new Socket(IP, PORT);
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
