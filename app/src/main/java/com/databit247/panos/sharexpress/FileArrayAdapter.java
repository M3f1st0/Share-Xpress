package com.databit247.panos.sharexpress;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class FileArrayAdapter extends ArrayAdapter<FileItem> {

    private Context c;
    private int id;
    private List<FileItem> items;

    public FileArrayAdapter(Context context, int textViewResourceId,
                            List<FileItem> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }

    public FileItem getItem(int i) {
        return items.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, null);
        }

        /* create a new view of my layout and inflate it in the row */
        //convertView = ( RelativeLayout ) inflater.inflate( resource, null );

        final FileItem o = items.get(position);
        if (o != null) {
            TextView t1 = (TextView) v.findViewById(R.id.TextView01);
            TextView t2 = (TextView) v.findViewById(R.id.TextView02);
            TextView t3 = (TextView) v.findViewById(R.id.TextViewDate);
            ImageView imageView = (ImageView) v.findViewById(R.id.fd_Icon1);


            if (t1 != null)
                t1.setText(o.getName());
            if (t2 != null)
                t2.setText(o.getData());
            if (t3 != null)
                t3.setText(o.getDate());
            if (imageView != null)
                if (o.getImage().contentEquals("directory_icon")) {
                    imageView.setImageResource(R.drawable.directory_icon);
                } else if (o.getImage().contentEquals("selected_icon")) {
                    imageView.setImageResource(R.drawable.selected_icon);
                } else if (o.getImage().contentEquals("audio_icon")) {
                    imageView.setImageResource(R.drawable.audio_icon);
                } else if (o.getImage().contentEquals("image_icon")) {
                    imageView.setImageResource(R.drawable.image_icon);
                } else if (o.getImage().contentEquals("text_icon")) {
                    imageView.setImageResource(R.drawable.text_icon);
                } else {
                    imageView.setImageResource(R.drawable.file_icon);
                }

        }
        return v;
    }
}