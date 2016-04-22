package com.databit247.panos.sharexpress.AsyncTasks;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class RetrieveConnection extends AsyncTask<String, Void, Socket> {
    private static final short PORT = 8000;
    private static String address;    //Panos
    //private static final String address = "192.168.1.92"; //Karolis

    @Override
    protected Socket doInBackground(String... params) {
        address = params[0];
        Socket socket = null;
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            socket = new Socket(inetAddress, PORT);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return socket;
    }
}
