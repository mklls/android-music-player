package com.pan.musicplayer.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.pan.musicplayer.R;
import com.pan.musicplayer.activity.SearchActivity;
import com.pan.musicplayer.activity.MainActivity;
import com.pan.musicplayer.adapter.MyListAdapter;
import com.pan.musicplayer.model.Playlist;
import com.pan.musicplayer.model.Song;
import com.pan.musicplayer.service.AudioService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

public class MusicFragment extends Fragment {
    private static final String LIST = "list";
    private ArrayList<Song> list;
    private ListView listview;
    private MainActivity activity;
    private AudioService service;
    private MaterialButton ivPlayAll;
    private MaterialButton ivShuffle;
    private ImageView ivSearch;
    private TextView tvGreeting;
    private MyListAdapter adapter;
    private String defaultPlayListUUID;

    public MusicFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        System.out.println("onAttach");
        super.onAttach(context);
        this.activity = (MainActivity) context;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println("Fragment onViewCreated");
    }


    @Override
    public void onStart() {
        super.onStart();
        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_music, container, false);
        listview = v.findViewById(R.id.music);
        ivShuffle = v.findViewById(R.id.shuffle);
        ivPlayAll = v.findViewById(R.id.play_all);
        ivSearch = v.findViewById(R.id.search);
        tvGreeting = v.findViewById(R.id.greeting);
        return v;
    }

    private void handleBottomSheetItemClick(int position, int audioId) {
        String msg;

        if (service.localResourceHelper.playlists.get(position).contains(audioId)) {
            msg = "歌曲已存在，添加失败";
        } else {
            service.localResourceHelper.addToPlaylist(audioId, position);
            msg = "已添加到 " + service.localResourceHelper
                    .playlists
                    .get(position)
                    .getTitle();
            activity.updatePage();
        }

        Snackbar.make(tvGreeting, msg,
                BaseTransientBottomBar.LENGTH_SHORT)
                .show();
    }

    private void handleEndIconClick(final int audioId) {
        final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View v = getLayoutInflater().inflate(R.layout.bottom_sheet_add_to_playlist, null);
        ListView listview =  v.findViewById(R.id.playlist);

        ArrayList<HashMap<String, String>> listItem = new ArrayList<>();
        for (Playlist p : service.localResourceHelper.playlists) {
            HashMap<String, String> map = new HashMap<>();
            map.put("title", p.getTitle());
            listItem.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(getContext(),
                listItem, R.layout.item_bottom_sheet_addto,
                new String[] {"title"},
                new int[] {R.id.title});
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleBottomSheetItemClick(position, audioId);
                dialog.cancel();
            }
        });
        dialog.setContentView(v);
        dialog.show();
    }

    private void greeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String msg;
        if (hour >= 6 && hour < 11) {
            msg = getString(R.string.morning);
        } else if (hour >= 11 && hour <= 13) {
            msg = getString(R.string.noon);
        } else if (hour > 13 && hour <= 18) {
            msg = getString(R.string.afternoon);
        } else {
            msg = getString(R.string.evening);
        }
        tvGreeting.setText(msg);
    }

    public void init() {
        service = activity.audioService;
        defaultPlayListUUID = UUID.randomUUID().toString();

        greeting();

        if (service == null) return;
        list = service.getAllSong();

        adapter = new MyListAdapter(getContext(), list);
        listview.setAdapter(adapter);

        adapter.setOnEndIconClickListener(new MyListAdapter.OnEndIconClickListener() {
            @Override
            public void onClick(int id) {
                handleEndIconClick(id);
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectItem(position);
                Playlist temp = new Playlist("temp");
                temp.setList(service.localResourceHelper.getDefaultPlayQueue());
                temp.setId(defaultPlayListUUID);
                service.play(position, temp);
            }
        });

        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), SearchActivity.class);
                startActivity(i);
            }
        });

        ivPlayAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Playlist temp = new Playlist("temp");
                temp.setList(service.localResourceHelper.getDefaultPlayQueue());
                temp.setId(defaultPlayListUUID);
                adapter.setSelectItem(0);
                activity.audioService.play(0, temp);
            }
        });

        ivShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                service.shuffle();
            }
        });
        // 高亮正在播放的条目
        if (service.getSong(AudioService.CURRENT_SONG) == null) return;
        setNowPlayingItem(service.getSong(AudioService.CURRENT_SONG).getId());
    }

    public void onClick(View v) {
        return;
    }

    public void setNowPlayingItem(int id) {
        if (service == null) return;
        int index = service
                .localResourceHelper
                .getDefaultPlayQueue()
                .indexOf(id);
        adapter.setSelectItem(index);
    }

}
