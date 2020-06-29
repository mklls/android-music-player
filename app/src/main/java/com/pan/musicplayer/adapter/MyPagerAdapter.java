package com.pan.musicplayer.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.pan.musicplayer.model.Song;

import java.util.ArrayList;

public class MyPagerAdapter extends PagerAdapter {
    protected LayoutInflater inflater;
    protected ArrayList<Song> list;
    protected Context context;

    public MyPagerAdapter(Context context, ArrayList<Song> list) {
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    public void changePlayQueue(ArrayList<Song> list) {
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
    }
}
