package com.databit247.panos.sharexpress;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.databit247.panos.sharexpress.AsyncTasks.RetrieveConnection;
import com.databit247.panos.sharexpress.AsyncTasks.UploadFilesTask;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class SharePhotoActivity extends AppCompatActivity {
    private Socket socket;

    private ImageView imageView;
    private Button button;

    private ArrayList<String> selectedPhoto = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.share_photo_activity);

        connectToPC();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();

        imageView = (ImageView) findViewById(R.id.imageView);
        button = (Button) findViewById(R.id.uploadButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPhoto();
            }
        });

        if (Intent.ACTION_SEND.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                String fileName = parseUriToFilename(uri);
                selectedPhoto.add(fileName);

                if (fileName != null) {
                    try {
                        Bitmap bmp = new BitmapDrawable(getResources(), fileName).getBitmap();
                        int nh = (int) (bmp.getHeight() * (512.0 / bmp.getWidth()));
                        Bitmap scaled = Bitmap.createScaledBitmap(bmp, 512, nh, true);
                        imageView.setImageBitmap(scaled);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    public void connectToPC() {
        try {
            AsyncTask aSyncConnectionRetriever = new RetrieveConnection().execute("connect");
            socket = (Socket) aSyncConnectionRetriever.get();
            //showConnectionToast();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void showConnectionToast() {
        if (isConnected()) {
            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "No Connection", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isConnected() {
        boolean isConnected = false;
        if (socket != null && socket.isConnected())
            isConnected = true;
        return isConnected;
    }

    public void uploadPhoto() {
        MainActivity mainActivity = new MainActivity();
        if (mainActivity != null) {
            new UploadFilesTask().execute(selectedPhoto,
                    mainActivity.getSocket(), getApplicationContext());
        } else {
            new UploadFilesTask().execute(selectedPhoto,
                    getSocket(), getApplicationContext());
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    public String parseUriToFilename(Uri uri) {
        String selectedImagePath = null;
        String fileManagerPath = uri.getPath();

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            selectedImagePath = cursor.getString(column_index);
        }

        if (selectedImagePath != null) {
            return selectedImagePath;
        } else if (fileManagerPath != null) {
            return fileManagerPath;
        }
        return null;
    }

    public Socket getSocket() {
        return socket;
    }

}


