package com.databit247.panos.sharexpress.Fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.databit247.panos.sharexpress.AsyncTasks.UploadFilesTask;
import com.databit247.panos.sharexpress.MainActivity;
import com.databit247.panos.sharexpress.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

//TODO delete files after they are backed up and make BackupService listen if backup is needed again by changing boolean flag"isNotified" in DefaultPreference to false.

public class UploadBackupFragment extends Fragment {

    //directories for backup
    private static final File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private static final File picsAndVideos = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "Camera");

    private ArrayList<String> pathsOfBackupFiles = new ArrayList<>();
    public static final String TAG = "UploadBackupFragment";
    private Button backupButton;
    private TextView textView;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.upload_backup_fragment, container, false);

        cleanContainer(container);
        fillBackupFileList();
        setupComponents();

        return view;
    }

    public void cleanContainer(ViewGroup container) {
        if (container != null) {
            container.removeAllViews();
        }
    }

    public void fillBackupFileList() {
        if (downloads.listFiles() != null) {
            for (File file : downloads.listFiles()) {
                if (file.isFile()) {
                    pathsOfBackupFiles.add(file.getAbsolutePath());
                }
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "There are no files that can be deleted to free up more space"
                    , Toast.LENGTH_LONG).show();
        }


/*
commented out because of testing. May be too many files to backup.
*/
//        for(File file: picsAndVideos.listFiles()){
//            if(file.isFile()) {
//                pathsOfBackupFiles.add(file.getAbsolutePath());
//            }
//        }
        Log.i(TAG, "files in backup list: " + pathsOfBackupFiles.size());
    }

    public void setupComponents() {
        setupUploadButton();
        setupTextView();
    }

    public void setupUploadButton() {
        backupButton = (Button) view.findViewById(R.id.uploadBackupFilesButton);
        backupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processUpload();
                try {
                    loadMainFragment();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void processUpload() {
        //upload files
        MainActivity mainActivity = (MainActivity) getActivity();
        AsyncTask uploadTask = new UploadFilesTask().execute(pathsOfBackupFiles,
                mainActivity.getSocket(), mainActivity.getApplicationContext());
        try {
            //after upload is done delete files and set isNotified flag ON, only if "delete files
            // after backup" setting is checked. This will allow Backup service to continue checking storage
            String s = (String) uploadTask.get();
            if (s.contentEquals("Done") && getBooleanDefaultPreference("delete_files_after_backup")) {
                deleteFiles();
                setBooleanDefaultPreference("backup_enabled", false);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void deleteFiles() {
        if (downloads.listFiles() != null) {
            for (File file : downloads.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Emtpy Folder"
                    , Toast.LENGTH_LONG).show();
        }

    }

    public void loadMainFragment() throws IOException {
        MainActivity ma = (MainActivity) getActivity();
        ma.loadFragmentIfConnected(new MainFragment());
    }

    public boolean getBooleanDefaultPreference(String setting) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        return preferences.getBoolean(setting, false);
    }

    public void setBooleanDefaultPreference(String setting, boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(setting, value);
        editor.commit();
    }

    public void setupTextView() {
        textView = (TextView) view.findViewById(R.id.backupTextView);
        textView.setText("\n\n\nYour System is out of free space. Click Upload to backup " + pathsOfBackupFiles.size() + " file(s) " +
                "to your PC. Files from these directories are included (they are NOT deleted): \n\n" + downloads.getPath() + "\n" + picsAndVideos.getPath());
    }
}
