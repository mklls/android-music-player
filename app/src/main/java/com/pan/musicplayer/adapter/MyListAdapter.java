package com.pan.musicplayer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.pan.musicplayer.R;
import com.pan.musicplayer.model.Song;

import java.util.ArrayList;


public class MyListAdapter extends BaseAdapter {
    /**
     * 播放列表适配器
     */
    private ArrayList<Song> list;
    private LayoutInflater inflater;
    private Context context;
    public static final String SELECTED_COLOR = "#6200EE";
    public static final String UNSELECTED_COLOR = "#000000";
    private OnEndIconClickListener listener;
    private int select = -1;

    public MyListAdapter(Context context, ArrayList<Song> list) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.list = list;
    }

    public void setOnEndIconClickListener(OnEndIconClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
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

    public interface OnEndIconClickListener {
        void onClick(int id);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_audio, null);
            holder.title = convertView.findViewById(R.id.title);
            holder.subtitle = convertView.findViewById(R.id.subtitle);
            holder.button = convertView.findViewById(R.id.more);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Song s = list.get(position);
        holder.title.setText(s.getTitle());
        holder.subtitle.setText(s.getAlbum() + " • " + s.getArtist());
        if (select == position) {
            holder.title.setTextColor(Color.parseColor(SELECTED_COLOR));
        } else {
            holder.title.setTextColor(Color.parseColor(UNSELECTED_COLOR));
        }

        final View finalConvertView = convertView;
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(list.get(position).getId());
                } else {
                    Snackbar.make(finalConvertView,
                            "行为未定义",
                            BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }
        });

        return convertView;
    }

    public void setSelectItem(int position) {
        if (position < 0 || position > list.size()) return;
        select = position;
        notifyDataSetChanged();
    }

}
