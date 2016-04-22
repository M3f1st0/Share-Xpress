package com.databit247.panos.sharexpress.Fragments;


import android.app.ListFragment;
import android.os.AsyncTask;
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


import com.databit247.panos.sharexpress.AsyncTasks.DownloadFilesTask;
import com.databit247.panos.sharexpress.AsyncTasks.RetrieveFilesTask;
import com.databit247.panos.sharexpress.FileArrayAdapter;
import com.databit247.panos.sharexpress.FileItem;
import com.databit247.panos.sharexpress.MainActivity;
import com.databit247.panos.sharexpress.R;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Karolis on 2016-03-05.
 */
public class DownloadFragment extends ListFragment implements AdapterView.OnItemClickListener {
    private ArrayList<FileItem> selectedFileItems = new ArrayList<>();
    private LayoutInflater inflater;
    private ListView listView;
    private FileArrayAdapter adapter;
    private Button downloadButton;
    private View view;
    private static final String TAG = "DownloadFragment";
    String path;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.FRAGMENT_TAG_STRING = TAG;
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.download_fragment, container, false);
        this.inflater = inflater;

        //cleanContainer(container);
        if (container != null) {
            container.removeAllViews();
        }
        setupComponents();

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.settings) {
            //load backup settings.
            MainActivity mainActivity = (MainActivity) getActivity();
            try {
                mainActivity.loadFragmentIfConnected(new Settings_Fragment());
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        //MainActivity.FRAGMENT_TAG_STRING="MainFragment";
        Log.d(TAG, "onDestroy");
    }

    //needed to clean previous views to avoid not replacing fragment. This cost me a lot of pain...
    public void cleanContainer(ViewGroup container) {
        if (container != null) {
            container.removeAllViews();
        }
    }

    public void setupComponents() {
        listView = (ListView) view.findViewById(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        downloadButton = (Button) view.findViewById(R.id.downloadFilesButton);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadSelectedFiles();
                loadMainFragment();
            }
        });
    }

    public void downloadSelectedFiles() {
        MainActivity mainActivity = (MainActivity) getActivity();
        new DownloadFilesTask().execute(selectedFileItems,
                mainActivity.getSocket(), mainActivity.getApplicationContext());
    }

    public void loadMainFragment() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.loadFragmentIfConnected(new MainFragment());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fillListView();
        getListView().setOnItemClickListener(this);
    }

    public void fillListView() {
        ArrayList<FileItem> files = getFileListFromPC();
        adapter = new FileArrayAdapter(inflater.getContext(), R.layout.files_dir_phone, files);
        setListAdapter(adapter);
    }

    public ArrayList<FileItem> getFileListFromPC() {
        MainActivity mainActivity = (MainActivity) getActivity();
        AsyncTask aSyncFileListRetriever = new RetrieveFilesTask().execute(mainActivity.getSocket());
        ArrayList<FileItem> files = null;
        try {
            files = (ArrayList<FileItem>) aSyncFileListRetriever.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return files;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        changeIcon(position, view);
        addToSelectedFileItems(position);
    }

    public void changeIcon(int position, View v) {
        FileItem o = adapter.getItem(position);
        o.setImage("selected_icon");
        ImageView imageView = (ImageView) v.findViewById(R.id.fd_Icon1);
        imageView.setImageResource(R.drawable.selected_icon);
    }

    public void addToSelectedFileItems(int position) {
        selectedFileItems.add(adapter.getItem(position));
    }

}