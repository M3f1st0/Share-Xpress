package com.databit247.panos.sharexpress.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.databit247.panos.sharexpress.BackupService;
import com.databit247.panos.sharexpress.MainActivity;
import com.databit247.panos.sharexpress.R;


/**
 * Created by panos on 2016-03-27.
 */
public class Settings_Fragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String START_BACKUP_IF_FREE_SPACE = "start_backup_if_free_space";
    public static final String INTERVAL_CHECK = "interval_check";
    public static final String DELETE_FILES_AFTER_BACKUP = "delete_files_after_backup";
    public static final String BACKUP_ENABLED = "backup_enabled";
    public static final String FILE_SERVER = "file_server";
    public static final String STREAM_SERVER = "stream_server";
    private static final String TAG = "Settings_Fragment";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.FRAGMENT_TAG_STRING = TAG;
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_layout);
        setHasOptionsMenu(true);


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

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(START_BACKUP_IF_FREE_SPACE)) {

            restartBackupService();
        }
        if (key.equals(DELETE_FILES_AFTER_BACKUP)) {

            restartBackupService();
        }
        if (key.equals(INTERVAL_CHECK)) {
            restartBackupService();
        }
        if (key.equals(STREAM_SERVER)) {
            Toast.makeText(getActivity().getApplicationContext(), "Restart the application to apply changes.", Toast.LENGTH_LONG).show();
        }
        if (key.equals(FILE_SERVER)) {
            Toast.makeText(getActivity().getApplicationContext(), "Restart the application to apply changes.", Toast.LENGTH_LONG).show();
        }
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
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        pref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        pref.registerOnSharedPreferenceChangeListener(this);
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

    public void restartBackupService() {
        Intent intent = new Intent(getActivity(), BackupService.class);
        getActivity().stopService(intent);
        getActivity().startService(intent);
    }
}
