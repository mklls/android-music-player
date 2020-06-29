package com.pan.musicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pan.musicplayer.R;
import com.pan.musicplayer.model.Playlist;

import java.util.ArrayList;

public class MyPlaylistViewAdapter extends BaseAdapter {
    private ArrayList<Playlist> playlists;
    private LayoutInflater inflater;
    private Context context;
    private OnEndIconClickListener listener;

    public interface OnEndIconClickListener {
        void OnClick(int position);
    }

    public MyPlaylistViewAdapter(Context context, ArrayList<Playlist> playlists) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.playlists = playlists;
    }

    @Override
    public int getCount() {
        return playlists.size();
    }

    @Override
    public Object getItem(int position) {
        return playlists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        public TextView title;
        public TextView subtitle;
        public ImageView button;
    }

    public void setOnEndIconClickListener(OnEndIconClickListener listener) {
        this.listener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_playlist, parent, false);
            holder.title = convertView.findViewById(R.id.title);
            holder.subtitle = convertView.findViewById(R.id.subtitle);
            holder.button = convertView.findViewById(R.id.more);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.title.setText(playlists.get(position).getTitle());
        holder.subtitle.setText(playlists.get(position).length() + "首");
        final View finalConvertView = convertView;
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.OnClick(position);
                }
            }
        });
        return convertView;
    }
}
