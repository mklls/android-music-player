package com.pan.musicplayer.activity;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import com.dd.ShadowLayout;
import com.pan.musicplayer.R;
import com.pan.musicplayer.adapter.ControlPanelAdapter;
import com.pan.musicplayer.model.Song;
import com.pan.musicplayer.service.AudioService;
import com.pan.musicplayer.util.ColorHelper;
import com.pan.musicplayer.util.Duration;


public class Main2Activity extends AppCompatActivity {
    private MyBroadcastReceiver receiver;
    private AudioService audioService;
    private AudioService.ServiceBinder binder;
    private MyConnection myConn;
    private ControlPanelAdapter adapter;
    private ViewPager vpControlPanel;

    private int position;
    private ImageView cover;
    private TextView background;
    private SeekBar seekBar;
    private TextView tvTitle;
    private TextView tvArtist;
    private TextView tvProgress;
    private TextView tvDuration;
    private TextView tvAlbum;
    private ImageView ivLike;
    private ImageView ivPlayback;
    private ImageView ivMode;
    private ShadowLayout sl;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int progress = msg.arg1;
            tvProgress.setText(Duration.toString(progress));
            seekBar.setProgress(progress / 1000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioService.BROADCAST_ACTION);
        receiver = new MyBroadcastReceiver();
        registerReceiver(receiver, filter);

        Intent intent = new Intent(this, AudioService.class);
        myConn = new MyConnection();
        startService(intent);
        bindService(intent, myConn, BIND_AUTO_CREATE);
        initView();
        // 为封面下方的 seekbar 设置监听事件
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            // 进度条进度改变时调用
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 更新文本
                tvProgress.setText(Duration.toString(progress * 1000));
            }

            // 按下seekbar的thumbnail时触发
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            // 从thumbnail松开时触发
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 更新进度
                audioService.seekTo(seekBar.getProgress() * 1000);
            }
        });
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        System.out.println("2 onStart");
//    }
//    @Override
//    public void onResume() {
//        super.onResume();
//        System.out.println("2 onResume");
//    }
//    @Override
//    public void onPause() {
//        super.onPause();
//        System.out.println("2 onPause");
//    }
//    @Override
//    public void onStop() {
//        super.onStop();
//        System.out.println("2 onStop");
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unbindService(myConn);
        System.out.println("2 onDestroy");
    }

    public void initView() {
        background = findViewById(R.id.background);
        vpControlPanel = findViewById(R.id.cover_pager);
        vpControlPanel.setPageMargin(100);
//        cover = findViewById(R.id.cover);
        seekBar = findViewById(R.id.seek_bar);
        tvTitle = findViewById(R.id.title);
        tvArtist = findViewById(R.id.artist);
        tvProgress = findViewById(R.id.progress);
        tvDuration = findViewById(R.id.duration);
        tvAlbum = findViewById(R.id.album);
        ivLike = findViewById(R.id.like);
        ivPlayback = findViewById(R.id.playback);
        ivMode = findViewById(R.id.mode);
        tvTitle.setSelected(true);
        tvArtist.setSelected(true);
        tvAlbum.setSelected(true);

    }

    /**
     * 模糊背景图片
     */

    private void blurring() {
//        background.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                Blurry.with(Main2Activity.this)
//                        .radius(10)
//                        .sampling(8)
//                        .animate(1500)
//                        .color(Color.argb(80, 0, 0, 0))
//                        .async()
//                        .capture(background)
//                        .into(background);
//                background.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//            }
//        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playback:
                if (audioService.playbackState == AudioService.PLAYBACK_STATE_PLAYING) {
                    audioService.pause();
                } else {
                    audioService.play();
                }
                break;
            case R.id.skip_next:
                audioService.skipToNext();
                break;
            case R.id.skip_previous:
                audioService.skipToPrevious();
                break;
            case R.id.like:
                audioService.toggleLike();
                break;
            case R.id.shuffle:
                audioService.shuffle();
                break;
            case R.id.mode:
                audioService.changePlaybackMode();
                break;
        }
    }

    /**
     * 依次提取图片中的
     * 主色
     * 柔和的暗色
     * 柔和色
     * 有活力的暗色
     * 活力色
     * 直到遇到满足条件的深色为止
     */

    private void setDarkBackground() {
        Palette
            .from(audioService.getCover(AudioService.CURRENT_COVER))
            .generate(new Palette.PaletteAsyncListener() {

            @SuppressWarnings("ConstantConditions")
            @Override
            public void onGenerated(@Nullable Palette palette) {
                int color = ColorHelper.
                        getDarkBackgroundColor(palette,
                                getColor(R.color.colorPrimaryDark));

                background.setBackgroundColor(color);
            }
        });
    }

    /**
     * 切换歌曲时调用，修改页面展示的信息
     */

    public void onMusicChange() {
        // 获取当前正的歌曲
        Song s = audioService.getSong(AudioService.CURRENT_SONG);
        // 设置封面
        vpControlPanel.setCurrentItem(audioService.getCurrentIndex(),true);

        // 标题
        tvTitle.setText(s.getTitle());
        // 顶部专辑
        tvAlbum.setText(s.getAlbum());
        // 艺术家
        tvArtist.setText(s.getArtist());
        // 已播放的进度
        tvProgress.setText("0:00");
        // 总时长
        tvDuration.setText(Duration.toString(s.getDuration()));
        int duration = (int) audioService.getSong(AudioService.CURRENT_SONG).getDuration();
        seekBar.setMax(duration / 1000);
        tvDuration.setText(Duration.toString(duration));
        updateLikeIcon();
        updatePlaybackModeIcon(audioService.playbackMode);

        if (audioService.playbackState != AudioService.PLAYBACK_STATE_PLAYING) {
            ivPlayback.setImageResource(R.drawable.play_circle);
        } else {
            ivPlayback.setImageResource(R.drawable.pause_circle);
        }

        setDarkBackground();
    }

    private void updateLikeIcon() {
        if (audioService.liked()) {
            ivLike.setImageResource(R.drawable.favorite_fill);
            ivLike.setColorFilter(getColor(R.color.like));
        } else {
            ivLike.setImageResource(R.drawable.favorite);
            ivLike.setColorFilter(getColor(R.color.dislike));
        }
    }

    private void updatePlaybackModeIcon(int state) {
        if (state == AudioService.PLAYBACK_MODE_SINGLE) {
            ivMode.setColorFilter(getResources().getColor(R.color.white));
        } else {
            ivMode.setColorFilter(getResources().getColor(R.color.colorAccent));
        }

        if (state == AudioService.PLAYBACK_MODE_SINGLE) {
            ivMode.setImageResource(R.drawable.repeat);
        } else if (state == AudioService.PLAYBACK_MODE_REPEAT) {
            ivMode.setImageResource(R.drawable.repeat);
        } else if (state == AudioService.PLAYBACK_MODE_REPEAT_ONE) {
            ivMode.setImageResource(R.drawable.repeat_one);
        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(AudioService.GENRE);
            int data = intent.getIntExtra(AudioService.MESSAGE, -1);
            if (data == -1) return;

            switch (type) {
                case AudioService.PLAYBACK_STATE:
                    if (data != AudioService.PLAYBACK_STATE_PLAYING) {
                        ivPlayback.setImageResource(R.drawable.play_circle);
                    } else {
                        ivPlayback.setImageResource(R.drawable.pause_circle);
                    }
                    break;
                case AudioService.SKIP:
                    onMusicChange();
                    position = audioService.getCurrentIndex();
                    break;
                case AudioService.SEEKTO:
                    seekBar.setProgress(data / 1000);
                    break;
                case AudioService.LIKE:
                    updateLikeIcon();
                    break;
                case AudioService.PLAYQUEUE_CHANGE:
                    adapter = new ControlPanelAdapter(Main2Activity.this,
                            audioService.getAllSongFromPlayQueue());
                    System.out.println("收到通知：" + audioService.getAllSongFromPlayQueue().size());
                    vpControlPanel.setAdapter(adapter);
                    onMusicChange();
                    break;
                case AudioService.PLAYBACK_MODE:
                    updatePlaybackModeIcon(data);
                    break;
                default:
                    System.out.println(type + " " + data);
                    break;
            }
        }
    }



    private class MyConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            System.out.println("绑定服务");
            binder = (AudioService.ServiceBinder) service;
            audioService = binder.getService();
            audioService.addProgressHandler(handler);
            adapter = new ControlPanelAdapter(Main2Activity.this,
                    audioService.getAllSongFromPlayQueue());
            vpControlPanel.setAdapter(adapter);
            vpControlPanel.setPageMargin(30);
            vpControlPanel.setOffscreenPageLimit(3);
            vpControlPanel.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

                @Override
                public void onPageSelected(int position) {
                    Main2Activity.this.position = position;
                    audioService.play(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) { }
            });
//            vpControlPanel.setPageTransformer(true, new DepthPageTransformer());
            onMusicChange();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            audioService.removeProgressHandler(handler);
            binder = null;
            audioService = null;
        }
    }
}
