package com.databit247.panos.sharexpress.AsyncTasks;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Karolis on 2016-03-19.
 */
public class UploadFilesTask extends AsyncTask<Object, Void, String> {
    public static final String TAG = "UploadFilesTask";
    private static final int BUFFER = 1024;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private Socket connection;

    @Override
    protected String doInBackground(Object... params) {
        ArrayList<String> uploadList = (ArrayList<String>) params[0];
        connection = (Socket) params[1];
        Context context = (Context) params[2];

        startUploadNotification(context);
        uploadFiles(uploadList);

        //update progress bar in notification to finished
        mBuilder.setContentText("Upload complete").setProgress(0, 0, false);
        mNotifyManager.notify(0, mBuilder.build());

        return "Done";
    }

    //creating notification manager and builder by getting them from context. builder must have three attributes: content title, content text, and icon, otherwise it wont work.
    public void startUploadNotification(Context context) {
        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("Files Uploading To PC").setContentText("Upload progress").setSmallIcon(android.R.drawable.stat_sys_download_done);
    }

    public void uploadFiles(ArrayList<String> uploadList) {
        for (int i = 0; i < uploadList.size(); i++) {
            File fileToUpload = new File(uploadList.get(i));
            updateUploadNotification(uploadList.size(), i);
            try {
                sendRequestHeader(fileToUpload);
                sendFile(fileToUpload);
                receiveACK();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateUploadNotification(int totalUploadFiles, int finished) {
        mBuilder.setProgress(totalUploadFiles, finished, false);
        mNotifyManager.notify(0, mBuilder.build());
    }

    public void sendRequestHeader(File fileToUpload) throws IOException {
        BufferedWriter outputToServer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        outputToServer.write("upload/" + fileToUpload.getName() + "/" + String.valueOf(fileToUpload.length()) + "\r\n");
        outputToServer.flush();
    }

    public void sendFile(File file) throws IOException {
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream(), BUFFER);
        Log.i(TAG, "filpath" + file.getPath());

        byte[] buff = new byte[1024];
        int bytesSent;
        try {
            while ((bytesSent = inputStream.read(buff)) > 0) {
                outputStream.write(buff, 0, bytesSent);
                outputStream.flush();
            }
            inputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    //need to get ACK when PC gets a file, because of synchronization. Phone sends the data out pretty fast,
    //but PC receives it delayed. When this happens, all sending files are sent as one byte
    //array and PC gets lost when one file finishes and another starts. Waiting for ACK from server
    // when it receives one file helps avoid it.
    public void receiveACK() throws IOException {
        BufferedReader bufferedReaderInput = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = bufferedReaderInput.readLine();
        Log.i(TAG, response);
    }
}
