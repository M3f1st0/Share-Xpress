package com.databit247.panos.sharexpress;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.databit247.panos.sharexpress.AsyncTasks.RetrieveConnection;
import com.databit247.panos.sharexpress.Fragments.DownloadFragment;
import com.databit247.panos.sharexpress.Fragments.MainFragment;
import com.databit247.panos.sharexpress.Fragments.Settings_Fragment;
import com.databit247.panos.sharexpress.Fragments.UploadBackupFragment;
import com.databit247.panos.sharexpress.Fragments.UploadFragment;


import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {
    private static Socket socket;
    private static final String TAG = "MainActivity";
    public static String FRAGMENT_TAG_STRING = "MainFragment";
    public static int i = 1;
    public static Fragment frgmnt;
    private FragmentManager fm;
    private String file_server_address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getting the IP address from the preferences
        PreferenceManager.setDefaultValues(this, R.xml.settings_layout, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        file_server_address = sharedPreferences.getString(Settings_Fragment.FILE_SERVER, "");
        fm = getFragmentManager();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //if it's the first loading of the MainActivity load the UI directly
        //and establish connection
        if (savedInstanceState == null) {
            loadFragment();
            try {
                connectToPC(file_server_address);
            } catch (IOException e) {
                e.printStackTrace();
            }
            showConnectionToast();
        } else {
            try {
                connectToPC(file_server_address);
            } catch (IOException e) {
                e.printStackTrace();
            }
            showConnectionToast();
            Log.d(TAG, "SavedInstance not null");
            if (FRAGMENT_TAG_STRING.equalsIgnoreCase("AudioStreamerFragment")) {
                //Retained fragment do nothing
                Log.d(TAG, FRAGMENT_TAG_STRING);
            } else if (FRAGMENT_TAG_STRING.equalsIgnoreCase("DownloadFragment")) {
                //load DownloadFragment
                Log.d(TAG, FRAGMENT_TAG_STRING);
                frgmnt = new DownloadFragment();
                loadFragmentIfConnected(frgmnt);
            } else if (FRAGMENT_TAG_STRING.equalsIgnoreCase("UploadFragment")) {
                //loadUploadFragment
                Log.d(TAG, FRAGMENT_TAG_STRING);
                frgmnt = new UploadFragment();
                loadFragmentIfConnected(frgmnt);
            } else if (FRAGMENT_TAG_STRING.equalsIgnoreCase("MainFragment")) {
                //loadUploadFragment
                Log.d(TAG, FRAGMENT_TAG_STRING);
                frgmnt = new MainFragment();
                loadFragmentIfConnected(frgmnt);
            }
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onDestroy");
    }


    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        loadFragmentIfConnected(new UploadBackupFragment());
    }

    public void connectToPC(String address) throws IOException {
        if (socket != null) {
            socket.close();
            try {
                AsyncTask aSyncConnectionRetriever = new RetrieveConnection().execute(address);
                socket = (Socket) aSyncConnectionRetriever.get();
                showConnectionToast();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            try {
                AsyncTask aSyncConnectionRetriever = new RetrieveConnection().execute(address);
                socket = (Socket) aSyncConnectionRetriever.get();
                showConnectionToast();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }


    }

    public void showConnectionToast() {
        if (isConnected()) {
            Toast.makeText(this, "Connection Established", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Server unreachable check Settings", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isConnected() {
        boolean isConnected = false;
        if (socket != null && socket.isConnected())
            isConnected = true;
        return isConnected;
    }

    public void loadFragment() {
        if (appStartedByBackupService()) {
            //loadFragmentIfConnected(new UploadBackupFragment());
            frgmnt = new UploadBackupFragment();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.add(R.id.fragment_container, frgmnt, FRAGMENT_TAG_STRING);
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            Log.d(TAG, FRAGMENT_TAG_STRING);
            frgmnt = new MainFragment();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.add(R.id.fragment_container, frgmnt, FRAGMENT_TAG_STRING);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }


    public boolean appStartedByBackupService() {
        return getIntent().getExtras() != null;
    }


    public <T extends Fragment> void loadFragmentIfConnected(T newFragment) {

        if (isConnected()) {
            if (newFragment == null) {

                Fragment fragment = newFragment;
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.add(R.id.fragment_container, fragment, FRAGMENT_TAG_STRING);
                transaction.addToBackStack(null);
                transaction.commit();
            } else {
                Log.d(TAG, FRAGMENT_TAG_STRING);
                Fragment fragment = newFragment;
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_container, fragment, FRAGMENT_TAG_STRING);
                transaction.addToBackStack(FRAGMENT_TAG_STRING);
                transaction.commit();
            }
        } else {
            Log.d(TAG, FRAGMENT_TAG_STRING);
            Fragment fragment = newFragment;
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.fragment_container, fragment, FRAGMENT_TAG_STRING);
            transaction.addToBackStack(FRAGMENT_TAG_STRING);
            transaction.commit();
        }
    }


    public Socket getSocket() {
        return socket;
    }

}