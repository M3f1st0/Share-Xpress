package com.databit247.panos.sharexpress.Fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;


import com.databit247.panos.sharexpress.AsyncTasks.UploadFilesTask;
import com.databit247.panos.sharexpress.FileArrayAdapter;
import com.databit247.panos.sharexpress.FileItem;
import com.databit247.panos.sharexpress.MainActivity;
import com.databit247.panos.sharexpress.R;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class UploadFragment extends ListFragment implements AdapterView.OnItemClickListener {
    private Button uploadButton;
    private ListView listView;
    private LayoutInflater inflater;
    private File currentDir;
    private FileArrayAdapter adapter;
    private ArrayList<String> pathsOfSelectedFiles = new ArrayList<>();
    private static final String TAG = "UploadFragment";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.FRAGMENT_TAG_STRING = TAG;
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;

        //without this "if" statement fragment is not replaced. This cost me a lot of pain...
        if (container != null) {
            container.removeAllViews();
        }
        currentDir = new File("/sdcard/");
        fill(currentDir);

        //get views, set onClickListener for button,
        View v = inflater.inflate(R.layout.upload_fragment, container, false);
        listView = (ListView) v.findViewById(android.R.id.list);
        uploadButton = (Button) v.findViewById(R.id.uploadFilesButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadSelectedFiles();
            }
        });

        return v;
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
        MainActivity.FRAGMENT_TAG_STRING = "MainFragment";
        Log.d(TAG, "onDestroy");
    }

    public void uploadSelectedFiles() {
        MainActivity mainActivity = (MainActivity) getActivity();
        new UploadFilesTask().execute(pathsOfSelectedFiles,
                mainActivity.getSocket(), mainActivity.getApplicationContext());
    }

    //set itemclick listener on
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnItemClickListener(this);
    }

    private void fill(File f) {
        File[] dirs = f.listFiles();
//        this.setTitle("Current Dir: "+f.getName());
        List<FileItem> dir = new ArrayList<FileItem>();
        List<FileItem> fls = new ArrayList<FileItem>();
        try {
            for (File ff : dirs) {
                Date lastModDate = new Date(ff.lastModified());
                DateFormat formater = DateFormat.getDateTimeInstance();
                String date_modify = formater.format(lastModDate);
                if (ff.isDirectory()) {


                    File[] fbuf = ff.listFiles();
                    int buf = 0;
                    if (fbuf != null) {
                        buf = fbuf.length;
                    } else buf = 0;
                    String num_item = String.valueOf(buf);
                    if (buf == 0) num_item = num_item + " item";
                    else num_item = num_item + " items";

                    //String formated = lastModDate.toString();
                    dir.add(new FileItem(ff.getName(), num_item, date_modify, ff.getAbsolutePath(), "directory_icon"));
                } else {
                    fls.add(new FileItem(ff.getName(), ff.length() / 1024 + " Kilobyte", date_modify, ff.getAbsolutePath(), "file_icon"));
                }
            }
        } catch (Exception e) {

        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if (!f.getName().equalsIgnoreCase("sdcard"))
            dir.add(0, new FileItem("..", "Parent Directory", "", f.getParent(), "directory_up"));
        adapter = new FileArrayAdapter(inflater.getContext(), R.layout.files_dir_phone, dir);
        this.setListAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileItem o = adapter.getItem(position);
        if (o.getImage().equalsIgnoreCase("directory_icon") || o.getImage().equalsIgnoreCase("directory_up")) {
            currentDir = new File(o.getPath());
            fill(currentDir);
        } else {
            onFileClick(o, view);
        }
    }

    private void onFileClick(FileItem fileItem, View view) {
        changeIconToSelected(fileItem, view);
        addPathToList(fileItem);

    }

    public void changeIconToSelected(FileItem fileItem, View view) {
        fileItem.setImage("selected_icon");
        ImageView imageView = (ImageView) view.findViewById(R.id.fd_Icon1);
        imageView.setImageResource(R.drawable.selected_icon);
    }

    public void addPathToList(FileItem fileItem) {
        pathsOfSelectedFiles.add(fileItem.getPath());
    }
}
