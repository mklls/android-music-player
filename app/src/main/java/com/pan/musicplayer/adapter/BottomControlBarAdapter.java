package com.pan.musicplayer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pan.musicplayer.R;
import com.pan.musicplayer.model.Song;

import java.util.ArrayList;

public class BottomControlBarAdapter extends MyPagerAdapter {

    public BottomControlBarAdapter(Context context, ArrayList<Song> list) {
        super(context, list);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View v = inflater.inflate(R.layout.bottom_control_bar, container, false);
        TextView info = v.findViewById(R.id.info);
        Song s = list.get(position);
        info.setSelected(true);
        info.setText(s.getTitle() + " • " + s.getArtist());
        container.addView(v);
        v.setTag(position);
        return v;
    }
}
