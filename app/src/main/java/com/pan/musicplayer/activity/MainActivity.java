package com.pan.musicplayer.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pan.musicplayer.adapter.BottomControlBarAdapter;
import com.pan.musicplayer.R;
import com.pan.musicplayer.fragment.MusicFragment;
import com.pan.musicplayer.service.AudioService;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;



public class MainActivity extends AppCompatActivity {

    public static final String SERVICE_CONNECTED = "service_connected";
    private int position;
    private AudioService.ServiceBinder binder;
    public AudioService audioService;
    private MyConnection myConn;
    private Intent service;

    public BottomControlBarAdapter adapter;
    public ViewPager vpControlBar;

    private ImageView ivLike;
    private ImageView ivPlayback;
    private MyBroadcastReceiver receiver;
    private ImageView ivCover;

    private Fragment navHostFragment;
    private MusicFragment musicFragment;

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销广播接收器
        unregisterReceiver(receiver);
        // 解绑服务
        unbindService(myConn);
        // 停止服务
        stopService(service);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 状态栏透明
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // 设置底部导航栏
        BottomNavigationView navView = findViewById(R.id.nav_view);
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);
        navHostFragment = getSupportFragmentManager().getPrimaryNavigationFragment();

        // 注册广播接收器
        receiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioService.BROADCAST_ACTION);
        registerReceiver(receiver, filter);
        requestPermission();
        initView();

        // 底部导航栏点按监听器
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // 导航到相应界面
                navController.navigate(item.getItemId());
                return true;
            }
        });
    }

    public void initView() {
        ivLike = findViewById(R.id.like);
        ivPlayback = findViewById(R.id.playback);
        ivCover = findViewById(R.id.cover);
        vpControlBar = findViewById(R.id.bottom_pager);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cover:
            case R.id.info_wrapper:
                // 跳转到音乐控制界面
                Intent i = new Intent(this, Main2Activity.class);
                startActivity(i);
                break;
            case R.id.playback:
                if (audioService.playbackState == AudioService.PLAYBACK_STATE_PLAYING) {
                    audioService.pause();
                } else {
                    audioService.play();
                }
                break;
            case R.id.like:
                audioService.toggleLike();
                break;
        }
    }


    /**
     * 由于需要获取外置存储的音乐所以需要动态申请读写外置存储的权限
     */
    private void requestPermission() {
        String[] PERMISSION = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSION, 1);
        } else {
            // 拥有读写外置存储的权限，启动服务
            service = new Intent(this, AudioService.class);
            myConn = new MyConnection();
            startService(service);
            bindService(service, myConn, BIND_AUTO_CREATE);
        }
    }

    /**
     * 权限申请结束的回调函数
     * 授权结束后会调用此函数
     *
     * @param requestCode  请求码
     * @param permissions  权限列表
     * @param grantResults 授权结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

            Intent intent = new Intent(this, AudioService.class);
            startService(intent);
            bindService(intent, new MyConnection(), BIND_AUTO_CREATE);
        }
    }

    /**
     * 和服务建立的连接用来获取IBinder
     * IBinder 用来调用服务的各种方法
     **/


    public void updatePage() {
        ivCover.setImageBitmap(audioService.getCover(AudioService.CURRENT_COVER));
        vpControlBar.setCurrentItem(audioService.getCurrentIndex(), false);

        if (musicFragment != null) {
            musicFragment.setNowPlayingItem(audioService.getSong(AudioService.CURRENT_SONG).getId());
        }
        if (audioService.liked()) {
            ivLike.setImageResource(R.drawable.favorite_fill);
        } else {
            ivLike.setImageResource(R.drawable.favorite);
        }
    }

    /**
     * 管理和服务的连接，获取服务实例，以便调用相关方法
     */

    private class MyConnection implements ServiceConnection {

        /**
         * 与服务连接成功后的回调
         * @param name
         * @param service 服务的中间人，用于获取服务实例
         */
        @SuppressLint("SetTextI18n")
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 获取中间人
            binder = (AudioService.ServiceBinder) service;
            // 获取服务
            audioService = binder.getService();
            // 初始化底部音乐控制栏
            adapter = new BottomControlBarAdapter(MainActivity.this,
                    audioService.getAllSongFromPlayQueue());
            vpControlBar.setAdapter(adapter);
            // 设置滑动切换音乐
            vpControlBar.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

                @Override
                public void onPageSelected(int position) {
                    audioService.play(position);
                    MainActivity.this.position = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) { }
            });
            // 刷新界面
            updatePage();
            // 获取首页片段并初始化
            navHostFragment = getSupportFragmentManager().getPrimaryNavigationFragment();
            musicFragment = (MusicFragment) navHostFragment
                    .getChildFragmentManager()
                    .getFragments()
                    .get(0);
            musicFragment.init();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
            Log.i("service", "服务断开连接");
        }
    }

    @SuppressWarnings("ConstantConditions")
    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(AudioService.GENRE);
            int data = intent.getIntExtra(AudioService.MESSAGE, -1);
            if (data == -1) return;
            if (type.equals(AudioService.PLAYBACK_MODE)) return;

            switch (type) {
                case AudioService.PLAYBACK_STATE:
                    if (data == AudioService.PLAYBACK_STATE_PLAYING) {
                        ivPlayback.setImageResource(R.drawable.pause);
                    } else {
                        ivPlayback.setImageResource(R.drawable.play);
                    }
                    break;
                case AudioService.SKIP:
                    updatePage();
                    break;
                case AudioService.LIKE:
                    if (data == AudioService.LIKE_TRUE) {
                        ivLike.setImageResource(R.drawable.favorite_fill);
                    } else if (data == AudioService.LIKE_FALSE) {
                        ivLike.setImageResource(R.drawable.favorite);
                    }
                    break;
                case AudioService.PLAYQUEUE_CHANGE:
                    adapter = new BottomControlBarAdapter(MainActivity.this,
                            audioService.getAllSongFromPlayQueue());
                    vpControlBar.setAdapter(adapter);
                    updatePage();
                    break;
                default:
                    break;
            }
        }
    }
}
