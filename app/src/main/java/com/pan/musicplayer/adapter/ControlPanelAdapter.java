package com.pan.musicplayer.adapter;

import android.content.Context;

import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.pan.musicplayer.R;
import com.pan.musicplayer.model.Song;
import com.pan.musicplayer.util.CoverHelper;

import java.util.ArrayList;


public class ControlPanelAdapter extends MyPagerAdapter {

    public ControlPanelAdapter(Context context, ArrayList<Song> list) {
        super(context, list);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View v = inflater.inflate(R.layout.control_panel, container, false);
        ImageView iv = v.findViewById(R.id.cover);
        Song s = list.get(position);
        iv.setImageBitmap(CoverHelper.getCover(s.getPath(), context));
        container.addView(v);
        return v;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public float getPageWidth(final int position) {
        return 1f;
    }
}
