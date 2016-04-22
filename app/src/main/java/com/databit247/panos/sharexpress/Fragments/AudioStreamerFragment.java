package com.databit247.panos.sharexpress.Fragments;

import android.app.ListFragment;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.databit247.panos.sharexpress.AsyncTasks.RetrieveFilesTask;
import com.databit247.panos.sharexpress.FileArrayAdapter;
import com.databit247.panos.sharexpress.FileItem;
import com.databit247.panos.sharexpress.MainActivity;
import com.databit247.panos.sharexpress.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Panagiotis Bitharis on 6/3/2016.
 */
public class AudioStreamerFragment extends ListFragment implements AdapterView.OnItemClickListener {
    private ArrayList<FileItem> selectedFileItems = new ArrayList<>();
    private LayoutInflater inflater;
    private ImageButton mPlayButton, mStopButton, mPauseButton;
    private SeekBar seekBar;
    private ListView mp3List;
    private ArrayList<FileItem> mp3s = null;
    private FileArrayAdapter adapter;
    private View view;
    private int previousChoice = -1;
    private View previousView;
    private int pausedAt;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private static final String TAG = "AudioStreamerFragment";
    private final String PORT = ":8888/";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.FRAGMENT_TAG_STRING = TAG;
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        if (container != null) {
            container.removeAllViews();
        }
        view = inflater.inflate(R.layout.audio_streamer_fragment, container, false);
        mPlayButton = (ImageButton) view.findViewById(R.id.streamer_play);
        mStopButton = (ImageButton) view.findViewById(R.id.streamer_stop);
        mPauseButton = (ImageButton) view.findViewById(R.id.streamer_pause);
        mp3List = (ListView) view.findViewById(android.R.id.list);
        mp3List.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);


        float volume = (float) (1 - (Math.log(100 - seekBar.getProgress()) / Math.log(100)));
        mediaPlayer.setVolume(volume, volume);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = (float) (1 - (Math.log(100 - progress) / Math.log(100)));
                mediaPlayer.setVolume(volume, volume);
                mediaPlayer.start();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                if (pausedAt > 0) {
                    mediaPlayer.seekTo(pausedAt);
                    pausedAt = 0;
                    mediaPlayer.start();
                }


                System.out.println(selectedFileItems.size());
                System.out.println(selectedFileItems.isEmpty());

                if (!mediaPlayer.isPlaying() && selectedFileItems.size() > 0) {
                    //String url = "http://192.168.1.7:8888/" + selectedFileItems.get(0).getName();
                    //String url = "http://192.168.0.7:8888/" + selectedFileItems.get(0).getName();
                    PreferenceManager.setDefaultValues(getActivity().getApplicationContext(), R.xml.settings_layout, false);
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                    String url = sharedPreferences.getString(Settings_Fragment.STREAM_SERVER, "");
                    url = url + PORT + selectedFileItems.get(0).getName();

                    try {
                        mediaPlayer.setDataSource(url);
                        mediaPlayer.prepareAsync();


                        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                mp.reset();
                                return false;
                            }
                        });

                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {

                                mp.start();

                            }
                        });


                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                System.out.println("Song Completed");
                                mp.stop();
                                mp.reset();
                            }
                        });


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }
        });


        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.reset();

            }
        });

        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausedAt = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();

            }
        });


        return view;
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        MainActivity mainActivity = (MainActivity) getActivity();
        AsyncTask aSyncFileListRetriever = new RetrieveFilesTask().execute(mainActivity.getSocket());

        try {
            mp3s = (ArrayList<FileItem>) aSyncFileListRetriever.get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        ArrayList<FileItem> tmp = new ArrayList<>();
        for (int i = 0; i < mp3s.size(); i++) {
            System.out.println("Getting file number: " + i + " " + mp3s.get(i).getName());

            if ((mp3s.get(i).getName().endsWith(".mp3"))) {
                tmp.add(mp3s.get(i));
            }
        }
        adapter = new FileArrayAdapter(inflater.getContext(), R.layout.files_dir_phone, tmp);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (previousChoice < 0) {
            changeIcon(position, view);
            addToSelectedFileItems(position);
            previousChoice = position;
            previousView = view;
        } else if (previousChoice >= 0) {
            changeIcon(previousChoice, previousView);
            previousChoice = position;
            previousView = view;
            changeIcon(position, view);
            addToSelectedFileItems(position);
        }


    }

    public void changeIcon(int position, View v) {
        FileItem o = adapter.getItem(position);

        if (o.getImage().equalsIgnoreCase("audio_icon")) {
            o.setImage("selected_icon");
            ImageView imageView = (ImageView) v.findViewById(R.id.fd_Icon1);
            imageView.setImageResource(R.drawable.selected_icon);
        } else {
            o.setImage("audio_icon");
            ImageView imageView = (ImageView) v.findViewById(R.id.fd_Icon1);
            imageView.setImageResource(R.drawable.audio_icon);
        }

    }

    public void addToSelectedFileItems(int position) {
        selectedFileItems.add(0, adapter.getItem(position));
    }


}




