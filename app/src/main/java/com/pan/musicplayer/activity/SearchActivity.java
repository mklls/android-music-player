package com.pan.musicplayer.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.pan.musicplayer.R;
import com.pan.musicplayer.adapter.MyListAdapter;
import com.pan.musicplayer.model.Playlist;
import com.pan.musicplayer.model.Song;
import com.pan.musicplayer.service.AudioService;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private TextInputLayout searchBar;
    private AudioService audioService;
    private MyConn conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        // 状态栏透明
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        initView();
    }

    public void initView() {
        conn = new MyConn();
        searchBar = findViewById(R.id.search_bar);
        Intent i = new Intent(this, AudioService.class);
        bindService(i, conn, BIND_AUTO_CREATE);
        searchBar.setStartIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });
        searchBar.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
        assert searchBar.getEditText() != null;

        searchBar.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                }
                return false;
            }
        });

    }

    private void search() {
        assert searchBar.getEditText() != null;
        String query = searchBar.getEditText().getText().toString().trim();
        if (query.isEmpty()) return;
        ArrayList<Song> all = audioService.getAllSong();
        ArrayList<Song> result = new ArrayList<>();
        final ArrayList<Integer> queue = new ArrayList<>();
        for (Song s : all) {
            if (s.getTitle().contains(query)
                    || s.getArtist().contains(query)
                    || s.getAlbum().contains(query)
                    || s.getPath().contains(query)) {
                result.add(s);
                queue.add(s.getId());
            }
        }
        RelativeLayout content = findViewById(R.id.content);
        LayoutInflater inflater = getLayoutInflater();
        View v;
        content.removeAllViews();
        if (result.size() == 0) {
            v = inflater.inflate(R.layout.notfound, content, false);
        } else {
            v = inflater.inflate(R.layout.item_search_result, content, false);
            final MyListAdapter adapter = new MyListAdapter(this, result);

            ((ListView) v ).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    adapter.setSelectItem(position);
                    Playlist temp = new Playlist("temp");
                    temp.setList(queue);
                    audioService.play(position, temp);
                    audioService.broadcast(AudioService.PLAYQUEUE_CHANGE);
                }
            });

            ((ListView) v).setAdapter(adapter);
        }
        content.addView(v);
    }

    private class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.ServiceBinder binder = (AudioService.ServiceBinder) service;
            audioService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }
}