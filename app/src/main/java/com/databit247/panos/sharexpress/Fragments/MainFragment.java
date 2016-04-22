package com.databit247.panos.sharexpress.Fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.databit247.panos.sharexpress.MainActivity;
import com.databit247.panos.sharexpress.R;

public class MainFragment extends Fragment {
    public static final String SETTINGS = "settings";
    private ImageButton downloadButton, uploadButton, playerButton;
    private Switch backupSwitch;
    private View view;
    private static final String TAG = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.FRAGMENT_TAG_STRING = TAG;
        setHasOptionsMenu(true);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.main_fragment, container, false);


        Log.d(TAG, "Main Fragment Created");


        MainActivity ma = new MainActivity();
        setupComponents();
        if (ma.getSocket() == null) {
            downloadButton.setActivated(false);
            uploadButton.setActivated(false);
            playerButton.setActivated(false);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_menu_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.settings) {
            //load backup settings.
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.loadFragmentIfConnected(new Settings_Fragment());
        }
        if (menuItem.getItemId() == android.R.id.home) {
            Toast.makeText(getActivity().getApplicationContext(), "Home Button Pushed", Toast.LENGTH_SHORT).show();
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.loadFragmentIfConnected(new MainFragment());
        }
        return true;
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
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
        Log.d(TAG, "onDestroy");

    }


    public void setupComponents() {
        setupUploadButton();
        setupDownloadButton();
        setupPlayerButton();
        //setupSettingsButton();   Replaced by Action Menu Item
        //setupBackupSwitch();
    }

    public void setupUploadButton() {

        final MainActivity mainActivity = (MainActivity) getActivity();
        uploadButton = (ImageButton) view.findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.loadFragmentIfConnected(new UploadFragment());
            }
        });

    }

    public void setupDownloadButton() {
        final MainActivity mainActivity = (MainActivity) getActivity();
        downloadButton = (ImageButton) view.findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.loadFragmentIfConnected(new DownloadFragment());
            }
        });
    }

    public void setupPlayerButton() {
        final MainActivity mainActivity = (MainActivity) getActivity();
        playerButton = (ImageButton) view.findViewById(R.id.playerButton);
        playerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.loadFragmentIfConnected(new AudioStreamerFragment());
            }
        });
    }


}
