package com.pan.musicplayer.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.pan.musicplayer.R;
import com.pan.musicplayer.adapter.MyListAdapter;
import com.pan.musicplayer.model.Playlist;
import com.pan.musicplayer.service.AudioService;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class PlaylistActivity extends AppCompatActivity {

    private AudioService audioService;
    private int listIndex = 0;
    private ListView list;
    private FloatingActionButton fab;
    private ImageView cover;
    private MyConn conn;
    private Playlist playlist;
    private MyBroadcastReceiver receiver;
    public static final String VIEW_TYPE = "view_type";
    public static final int VIEW_TYPE_PLAYLIST = 0;
    public static final int VIEW_TYPE_ALBUM = 1;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        unregisterReceiver(receiver);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        assert listIndex >=0 && listIndex < audioService.localResourceHelper.playlists.size();


        receiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioService.BROADCAST_ACTION);
        registerReceiver(receiver, filter);

        conn = new MyConn();
        Intent i = new Intent(this, AudioService.class);
        bindService(i, conn, BIND_AUTO_CREATE);

    }

    public void initView() {
        listIndex = getIntent().getIntExtra("index", 0);
        int type = getIntent().getIntExtra(VIEW_TYPE, VIEW_TYPE_PLAYLIST);

        if (type == VIEW_TYPE_PLAYLIST) {
            playlist = audioService.localResourceHelper.playlists.get(listIndex);
        } else {
            playlist = audioService.localResourceHelper.albums.get(listIndex);
        }

        list = findViewById(R.id.list);
        fab = findViewById(R.id.fab);
        cover = findViewById(R.id.cover);
        final MyListAdapter adapter = new MyListAdapter(this, audioService.localResourceHelper.idList2Song(playlist));

        adapter.setOnEndIconClickListener(new MyListAdapter.OnEndIconClickListener() {
            @Override
            public void onClick(int id) {
                audioService.localResourceHelper.removeFromPlaylist(id, listIndex);
                initView();
            }
        });
        list.setAdapter(adapter);
        updateFAB(audioService.playbackState);
        ((TextView)findViewById(R.id.title)).setText(playlist.getTitle());

        if (playlist.length() == 0) return;
        Glide.with(this)
                .load(audioService.localResourceHelper.getCover(playlist.getList().get(0)))
                .dontAnimate()
                .error(R.drawable.takagisan_min)
                .apply(RequestOptions.bitmapTransform(new BlurTransformation(12, 1)))
                .into(cover);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                audioService.play(position, playlist);
                adapter.setSelectItem(position);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioService.playbackState != AudioService.PLAYBACK_STATE_PLAYING) {
                    audioService.play(0, playlist);
                    adapter.setSelectItem(0);
                } else {
                    audioService.pause();
                }
            }
        });

    }

    private class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            audioService = ((AudioService.ServiceBinder) service).getService();
            initView();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { }
    }

    private void updateFAB(int playbackState) {
        if (playbackState == AudioService.PLAYBACK_STATE_PLAYING) {
            fab.setImageResource(R.drawable.pause);
        } else {
            fab.setImageResource(R.drawable.play);
        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(AudioService.GENRE);
            if (!type.equals(AudioService.PLAYBACK_STATE)) return;
            int data = intent.getIntExtra(AudioService.MESSAGE, -1);
            // 播放器状态发生变化，更新右下角fab的图标
            updateFAB(data);
        }
    }
}