package fyp.recorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
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

public class FileTCP {

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

    public String message = "";

    String rawName = "", rinexName = "", nmeaName = "";
    String[] path = new String[]{"", "", ""};
    String[] prefix = new String[]{"/Raw", "/RINEX", "/NMEA"};
    List<String> logPath;

    String filePath = "";
    String fileName = "";
    int fileType = 0;

    //int i = 0;

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

        message = msg;

        MsgTCP msgTCP = new MsgTCP();
        msgTCP.execute();
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
                socket = new Socket("192.168.0.122", 8080);
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
