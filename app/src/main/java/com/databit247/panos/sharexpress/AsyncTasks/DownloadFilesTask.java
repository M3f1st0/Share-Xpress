package com.databit247.panos.sharexpress.AsyncTasks;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;

import com.databit247.panos.sharexpress.FileItem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Karolis on 2016-03-06.
 */
public class DownloadFilesTask extends AsyncTask<Object, Void, String> {
    private static final int BUFFER = 32 * 1024;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    protected String doInBackground(Object... params) {
        ArrayList<FileItem> downloadList = (ArrayList<FileItem>) params[0];
        Socket connection = (Socket) params[1];
        Context context = (Context) params[2];

        startDownloadNotification(context);
        downloadFiles(downloadList, connection);

        //update progress bar in notification to finished
        mBuilder.setContentText("Download complete").setProgress(0, 0, false);
        mNotifyManager.notify(0, mBuilder.build());

        return null;
    }

    //creating notification manager and builder by getting them from context. builder must have three attributes: content title, content text, and icon, otherwise it wont work.
    public void startDownloadNotification(Context context) {
        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("Files Downloading From PC").setContentText("Download progress").setSmallIcon(android.R.drawable.stat_sys_download_done);
    }

    public void downloadFiles(ArrayList<FileItem> downloadList, Socket connection) {
        for (int i = 0; i < downloadList.size(); i++) {
            String fileToDownload = downloadList.get(i).getName();
            updateDownloadNotification(downloadList.size(), i);
            try {
                sendRequest(fileToDownload, connection);
                getResponse(connection);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateDownloadNotification(int totalDownloadFiles, int finished) {
        mBuilder.setProgress(totalDownloadFiles, finished, false);
        mNotifyManager.notify(0, mBuilder.build());
    }

    public void sendRequest(String fileToDownload, Socket connection) throws IOException {
        BufferedWriter outputToServer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

        outputToServer.write("get " + fileToDownload + "\r\n");
        outputToServer.flush();
    }


    public void getResponse(Socket connection) throws IOException {
        BufferedReader inputFromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        try {
            String response = inputFromServer.readLine();
            if (response.contains("ACK")) {
                String fileSize = inputFromServer.readLine();
                String fileName = inputFromServer.readLine();
                File file = createFileForSavingDownload(fileName);
                //if file is already downloaded createFileForSavingDownload will return null,
                //so we can return to downloadFiles() loop for next file
                if (file == null) {
                    return;
                }
                downloadFile(Integer.parseInt(fileSize), connection, file, inputFromServer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File createFileForSavingDownload(String fileName) throws IOException {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, fileName);
        if (file.exists()) {
            file = null;
        }
        return file;
    }

    private void downloadFile(int fileSize, Socket connection,
                              File file, BufferedReader reader) throws IOException {
        BufferedInputStream readFile = new BufferedInputStream(connection.getInputStream(), BUFFER);
        BufferedOutputStream writeFile = new BufferedOutputStream(new FileOutputStream(file), BUFFER);

        readRemainingLinesFromHeader(reader);

        int bufferSIze = 0;
        int bufferIndex = 0;
        while (fileSize > 0) {
            if (BUFFER <= fileSize) {
                bufferSIze = BUFFER;
            } else {
                bufferSIze = fileSize;
            }
            int tmpBuffer = bufferSIze;
            byte[] incomingBytes = new byte[bufferSIze];
            while (tmpBuffer > 0) {

                incomingBytes[bufferIndex++] = (byte) readFile.read();
                tmpBuffer -= 1;

            }
            bufferIndex = 0;
            writeFile.write(incomingBytes);
            writeFile.flush();
            fileSize = fileSize - bufferSIze;

            System.out.println("Remaining: " + fileSize + " bytes");

        }
        writeFile.flush();
        writeFile.close();

    }

    public void readRemainingLinesFromHeader(BufferedReader reader) throws IOException {
        while (true) {
            String responseFromSrv = reader.readLine();
            if (responseFromSrv == null || responseFromSrv.length() == 0) {
                break;
            }
        }
    }
}
