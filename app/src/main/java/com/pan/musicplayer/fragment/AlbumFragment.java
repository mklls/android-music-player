package com.pan.musicplayer.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.pan.musicplayer.R;
import com.pan.musicplayer.activity.MainActivity;
import com.pan.musicplayer.activity.PlaylistActivity;
import com.pan.musicplayer.adapter.GridViewAdapter;
import com.pan.musicplayer.model.Album;
import com.pan.musicplayer.service.AudioService;

import java.util.ArrayList;


public class AlbumFragment extends Fragment {
    private MainActivity activity;
    public AudioService service;
    private ArrayList<Album> albums;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
        service = activity.audioService;
        albums = service.localResourceHelper.albums;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_album, container, false);

        GridView gv = v.findViewById(R.id.grid);

        GridViewAdapter adapter = new GridViewAdapter(getContext(), albums);

        gv.setAdapter(adapter);

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getContext(), PlaylistActivity.class);
                i.putExtra("index", position);
                i.putExtra(PlaylistActivity.VIEW_TYPE,
                        PlaylistActivity.VIEW_TYPE_ALBUM);
                startActivity(i);
            }
        });

        return v;
    }
}
