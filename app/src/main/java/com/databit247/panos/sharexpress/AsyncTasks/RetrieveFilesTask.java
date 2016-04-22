package com.databit247.panos.sharexpress.AsyncTasks;

import android.os.AsyncTask;

import com.databit247.panos.sharexpress.FileItem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;


public class RetrieveFilesTask extends AsyncTask<Socket, Void, ArrayList<FileItem>> {

    @Override
    protected ArrayList<FileItem> doInBackground(Socket... params) {
        Socket connection = params[0];

        return retrieveFiles(connection);
    }

    public ArrayList<FileItem> retrieveFiles(Socket connection) {
        ArrayList<FileItem> fileItems = new ArrayList<>();
        ArrayList<String> files;
        try {
            sendGetFilesCommand(connection);
            files = getFileList(connection);
            fileItems = getFileItems(files);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileItems;
    }

    public void sendGetFilesCommand(Socket connection) throws IOException {
        BufferedWriter outputToServer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        outputToServer.write("dir" + "\r\n");
        outputToServer.flush();
    }

    public ArrayList<String> getFileList(Socket connection) throws IOException {
        BufferedReader inputFromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        ArrayList<String> files = new ArrayList<>();
        while (true) {
            String responseFromSrv = inputFromServer.readLine();
            if (responseFromSrv.length() == 0) {
                break;
            } else {
                files.add(responseFromSrv);
            }
        }
        return files;
    }

    public ArrayList<FileItem> getFileItems(ArrayList<String> files) {
        ArrayList<FileItem> fileItems = new ArrayList<>();
        for (String file : files) {
            fileItems.add(generateFileItem(file));
        }
        return fileItems;
    }

    public FileItem generateFileItem(String file) {
        FileItem fileItem = new FileItem();
        String[] tokens = file.split(",");
        fileItem.setName(tokens[0]);
        fileItem.setData(parseSize(tokens[1]));
        fileItem.setImage(parseType(tokens[2]));

        return fileItem;
    }

    public String parseSize(String size) {
        String parsedSize;
        long bytes = Long.valueOf(size);
        if (bytes > 1048576)//1048576 bytes = 1mb
            parsedSize = String.valueOf((int) (bytes / 1048576)) + " Mb";
        else
            parsedSize = String.valueOf((int) (bytes / 1024)) + " Kb";

        return parsedSize;
    }

    public String parseType(String mimeType) {
        String type = mimeType.split("/")[0];
        String imageName;
        if (type.contentEquals("audio"))
            imageName = "audio_icon";
        else if (type.contentEquals("image"))
            imageName = "image_icon";
        else if (type.contentEquals("text"))
            imageName = "text_icon";
        else
            imageName = "unknown";
        return imageName;
    }
}
