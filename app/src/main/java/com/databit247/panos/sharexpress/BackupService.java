package com.databit247.panos.sharexpress;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.Log;


import com.databit247.panos.sharexpress.Fragments.Settings_Fragment;

import java.util.Timer;
import java.util.TimerTask;


//TODO add slider in app to specify BACKUP_LIMITER_MB.
//TODO add option to specify INTERVAL_FOR_BACKUP_CHECK

public class BackupService extends IntentService {
    public static final String TAG = "BackupService";
    private static final int BYTES_IN_MB = 1048576;
    private int backupLimit;
    private int intervalForBackup;
    private boolean isNotified;
    private Timer timer;

    public BackupService() {
        super("BackupService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "service started");
    }

    //
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);

        scheduleTimerForBackupChecks();

        return START_STICKY;
    }

    public void scheduleTimerForBackupChecks() {
        if (timer == null) {
            intervalForBackup = getIntFromDefaultPreferences(Settings_Fragment.INTERVAL_CHECK);

            timer = new Timer();
            TimerTask checkStorage = new TimerTask() {
                @Override
                public void run() {
                    getSettings();
                    backupCheck();
                }
            };
            timer.schedule(checkStorage, 0l, 1000 * intervalForBackup);
        }
    }

    public void getSettings() {
        backupLimit = getIntFromDefaultPreferences(Settings_Fragment.START_BACKUP_IF_FREE_SPACE);
    }

    public void backupCheck() {
        int freeSpace = getFreeSpaceInMegabytes();
        isNotified = getBooleanFromDefaultPreferences(Settings_Fragment.BACKUP_ENABLED);
        Log.i(TAG, "Free space: " + freeSpace + " backup limit: " + backupLimit + " isNotified: " + isNotified);

        //this should be run only once, otherwise it will load UploadBackupFragment every 10seconds. isNotified
        // flag is used for that. It is saved in preferences too because destroying the service it
        // only restarts with new variable values. START_STICKY
        if (freeSpace < backupLimit && !isNotified) {
            Log.i(TAG, String.valueOf(freeSpace) + " Backup required");
            loadUploadFragment();
            setDefaultPreference(Settings_Fragment.BACKUP_ENABLED, true);
        }
    }

    public int getFreeSpaceInMegabytes() {
        StatFs stats = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = stats.getAvailableBytes();
        long megabytesAvailable = bytesAvailable / BYTES_IN_MB;
        return (int) megabytesAvailable;
    }

    public void loadUploadFragment() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("Backup", true);
        Intent dialogIntent = new Intent(this, MainActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        dialogIntent.putExtras(bundle);
        startActivity(dialogIntent);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "service destroyed");
        timer.cancel();
    }

    public boolean getBooleanFromDefaultPreferences(String setting) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean(setting, true);
    }

    public int getIntFromDefaultPreferences(String setting) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return Integer.parseInt(preferences.getString(setting, ""));
    }

    public void setDefaultPreference(String option, boolean isTurnedOn) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(option, isTurnedOn);
        editor.commit();
    }
}
