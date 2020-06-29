package com.pan.musicplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pan.musicplayer.R;
import com.pan.musicplayer.model.Album;
import com.pan.musicplayer.util.ColorHelper;
import com.pan.musicplayer.util.CoverHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class GridViewAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Album> albums;
    private HashMap<Integer, Bitmap> cache;

    public GridViewAdapter(Context context, ArrayList<Album> albums) {
        this.context = context;
        this.albums = albums;
        inflater = LayoutInflater.from(context);
        cache = new HashMap<>();
    }

    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public Object getItem(int position) {
        return albums.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        public TextView title;
        public TextView artist;
        public ImageView cover;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_album, null);
            holder.title = convertView.findViewById(R.id.album);
            holder.artist = convertView.findViewById(R.id.artist);
            holder.cover = convertView.findViewById(R.id.cover);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Album a = albums.get(position);
        holder.title.setText(a.getTitle());
        holder.artist.setText(a.getArtist());

        // 获取封面
        Bitmap temp = cache.get(position);
        // 缓存中不存在则直接生成
        if (temp == null) {
            temp = CoverHelper.getCover(a.getCover(),context);
            cache.put(position, temp);
        }

        holder.cover.setImageBitmap(temp);
        return convertView;
    }
}
