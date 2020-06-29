package com.pan.musicplayer.service;

import android.app.Service;
import android.content.Intent;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import android.os.Message;
import android.util.Log;

import com.pan.musicplayer.model.Playlist;
import com.pan.musicplayer.model.Song;
import com.pan.musicplayer.util.LocalResourceHelper;

import java.io.IOException;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Random;
import java.util.UUID;


public class AudioService extends Service {
    // 字符串常量

    public static final int NULL = 0xff;

    public static final int CURRENT_SONG    = 0x00;
    public static final int NEXT_SONG       = 0x01;
    public static final int PREVIOUS_SONG   = 0x02;
    public static final int CURRENT_COVER   = 0x03;
    public static final int NEXT_COVER      = 0x04;
    public static final int PREVIOUS_COVER  = 0x05;

    // 播放器相关

    // 播放器状态
    public static final String PLAYBACK_STATE        = "playback_state";
    public static final int PLAYBACK_STATE_IDLE      = 0x06;
    public static final int PLAYBACK_STATE_PLAYING   = 0x07;
    public static final int PLAYBACK_STATE_PAUSED    = 0x08;
    public static final int PLAYBACK_STATE_SKIP      = 0x09;

    // 播放模式
    public static final String PLAYBACK_MODE            = "playback_mode";
    public static final int PLAYBACK_MODE_REPEAT        = 0x0a;
    public static final int PLAYBACK_MODE_SINGLE        = 0x0b;
    public static final int PLAYBACK_MODE_REPEAT_ONE    = 0x0c;

    // 切歌
    public static final String SKIP             = "skip";
    public static final int SKIP_TO_NEXT        = 0x0d;
    public static final int SKIP_TO_PREVIOUS    = 0x0e;

    // 随机播放
    public static final String SHUFFLE = "shuffle";
    public static final int SHUFFLE_ALL = 0x0f;

    // 喜欢
    public static final String LIKE     = "like";
    public static final int LIKE_TRUE   = 0x10;
    public static final int LIKE_FALSE  = 0x11;

    public static final String PLAYQUEUE_CHANGE = "playqueue_change";

    // 广播发送的Action
    public static final String BROADCAST_ACTION = "com.pan.musicplayer.broadcast";

    // 改变进度
    public static final String SEEKTO = "seek_to";

    // 广播常量

    // 种类
    public static final String GENRE = "genre";

    // 数据
    public static final String MESSAGE = "message";

    // 本地媒体资源管理
    public LocalResourceHelper localResourceHelper;
    
    // 播放器状态
    public int playbackState;

    // 播放器模式
    public int playbackMode;

    // 中间人将自身提供给Activity
    private ServiceBinder binder;

    // 媒体播放器
    private MediaPlayer player;

    // 当前播放列表id
    private String playlistId;

    // 播放队列 内容为歌曲id
    private ArrayList<Integer> playQueue;

    // 当前播放歌曲的索引(相对于播放队列)
    private int currentIndex;

    // 下一曲索引
    private int nextIndex;

    // 上一曲索引
    private int previousIndex;

    // 当前播放的歌曲
    private Song currentSong;

    // 下一曲
    private Song nextSong;

    // 上一曲
    private Song previousSong;

    // 当前歌曲封面
    private Bitmap currentCover;

    // 下一曲封面
    private Bitmap nextCover;

    // 上一曲封面
    private Bitmap previousCover;

    // 可能有多个SeekBar需要同步进度，
    // 比如Main2Activity的SeekBar和Notification的SeekBar，
    // 所以此处使用了ArrayList来存放多个handler，在updateProgressThread线程里面依次使用
    private ArrayList<Handler> handlers;

    //更新当前进度
    private Thread updateProgressThread;

    public AudioService() {}

    @Override
    public void onCreate() {
        Log.i("service", "服务创建成功");
        initAudioService();
    }

    // 服务销毁时释放player所用的资源
    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
    }

    /**
     * 初始化媒体服务
     */

    public void initAudioService() {
        updateProgressThread = new Thread();
        localResourceHelper = new LocalResourceHelper(this);
        binder = new ServiceBinder();
        playlistId = UUID.randomUUID().toString();
        handlers = new ArrayList<>();
        playQueue = localResourceHelper.getDefaultPlayQueue();
        if (playQueue.isEmpty()) return;
        update(0);
        initMediaPlayer();
    }

    /**
     * 初始化媒体播放器
     */

    public void initMediaPlayer() {
        player = new MediaPlayer();
        playbackState = PLAYBACK_STATE_IDLE;
        playbackMode = PLAYBACK_MODE_SINGLE;

        // 播放结束监听器
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                switch (playbackMode) {
                    case PLAYBACK_MODE_REPEAT:
                        skipToNext();
                        break;
                    case PLAYBACK_MODE_SINGLE:
                        player.reset();
                        playbackState = PLAYBACK_STATE_IDLE;
                        broadcast(PLAYBACK_STATE, playbackState);
                        break;
                    case PLAYBACK_MODE_REPEAT_ONE:
                        break;
                }
            }
        });

        // 创建子线程，同步播放当前进度
        updateProgressThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                        if (playbackState != PLAYBACK_STATE_PLAYING) {
                            continue;
                        }
                        int position = player.getCurrentPosition();
                        // 无法再其他线程里完成更新UI操作，只能够通过Handler来更新UI
                        for (Handler h: handlers) {
                            Message msg = new Message();
                            msg.arg1 = position;
                            h.sendMessage(msg);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        
    }

    /**
     * 根据索引更新更新歌曲信息
     * 更新当前，上一曲，下一曲的信息
     * @param currentIndex 当前索引
     */

    public void update(int currentIndex) {
        this.currentIndex = currentIndex;
        currentSong = localResourceHelper
                .getSongInfo(playQueue.get(currentIndex));
        currentCover = localResourceHelper
                .getCover(currentSong.getId());

        nextIndex = (currentIndex + 1) % playQueue.size();
        previousIndex = (playQueue.size() + currentIndex - 1) % playQueue.size();

        previousSong = localResourceHelper.getSongInfo(previousIndex);
        nextSong = localResourceHelper.getSongInfo(playQueue.get(nextIndex));

        previousCover = localResourceHelper.getCover(previousSong.getId());
        nextCover = localResourceHelper.getCover(nextSong.getId());
    }

    /**
     * 获取当前播放队列
     * @return ArrayList<Integer> 播放队列，内容为歌曲id
     */

    public ArrayList<Integer> getPlayQueue() {
        return playQueue;
    }

    /**
     * 获取当前播放队列里面的所有歌曲
     * @return
     */

    public ArrayList<Song> getPlayList() {
        ArrayList<Song> s = new ArrayList<>();
        for (Integer i : playQueue) {
            s.add(localResourceHelper.getSongInfo(i));
        }
        return s;
    }
    /**
     * 返回播放队列里的所有歌曲
     * @return 所有歌曲
     */
    public ArrayList<Song> getAllSongFromPlayQueue() {
        ArrayList<Song> s = new ArrayList<>();
        for (Integer i : playQueue) {
            s.add(localResourceHelper.getSongInfo(i));
        }
        return s;
    }
    public void setPlayQueue(ArrayList<Integer> playQueue) {
        this.playQueue = playQueue;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }


    public ArrayList<Song> getAllSong() {
        return localResourceHelper.getAll();
    }


    /**
     * 获取歌曲
     * @param type 三种，当前歌曲，上一曲，下一曲
     *             对应 CURRENT_SONG PREVIOUS_SONG NEXT_SONG
     * @return Song
     */
    public Song getSong(int type) {
        Song t;
        switch (type) {
            default:
            case CURRENT_SONG:
                t = currentSong;
                break;
            case NEXT_SONG:
                t = nextSong;
                break;
            case PREVIOUS_SONG:
                t = previousSong;
        }
        return t;
    }

    /**
     * 获取封面
     * @param type 封面类型，有三种，当前封面，上一张，下一张
     * @return 封面位图
     */

    public Bitmap getCover(int type) {
        Bitmap t;
        switch (type) {
            default:
            case CURRENT_COVER:
                t = currentCover;
                break;
            case NEXT_COVER:
                t = nextCover;
                break;
            case PREVIOUS_COVER:
                t = previousCover;
                break;
        }
        return t;
    }

    /**
     * 用id播放歌曲
     * @param id 歌曲id
     */

    public void play(Integer id) {
        update(playQueue.indexOf(id));
        play();
    }

    /**
     * 播放歌曲
     * @param index 歌曲索引
     * @param list  播放列表
     */
    public void play(int index, Playlist list) {
        if (!playlistId.equals(list.getId())) {
            playlistId = list.getId();
            playQueue = list.getList();
            // 发送广播通知播放队列发生改变
            System.out.println("发送改变通知");
            broadcast(PLAYQUEUE_CHANGE);
        }

        play(index);
    }

    /**
     * 播放歌曲
     * @param index 歌曲位于当前播放队列的索引
     */
    public void play(int index) {
        if (index == currentIndex && playbackState == PLAYBACK_STATE_PLAYING) return;
        update(index);
        playbackState = PLAYBACK_STATE_SKIP;
        int type;
        if (index < currentIndex) {
            type = SKIP_TO_PREVIOUS;
        } else {
            type = SKIP_TO_NEXT;
        }
        broadcast(SKIP, type);
        play();
    }

    /**
     * 播放当前歌曲
     */

    public void play()  {
        if (currentSong == null) return;
        try {
            if (playbackState == PLAYBACK_STATE_IDLE) {
                player.setDataSource(currentSong.getPath());
                player.prepare();
            } else if (playbackState == PLAYBACK_STATE_SKIP) {
                player.reset();
                player.setDataSource(currentSong.getPath());
                player.prepare();
            }

            player.start();
            playbackState = PLAYBACK_STATE_PLAYING;
            broadcast(PLAYBACK_STATE, playbackState);
            if (!updateProgressThread.isAlive()) {
                updateProgressThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 乱序播放
     */

    public void shuffle() {
        shuffle(playQueue);
    }

    /**
     * 打乱顺序随机播放
     * @param playQueue 需要乱序的播放队列
     */

    public void shuffle(ArrayList<Integer> playQueue) {
        playbackState = PLAYBACK_STATE_SKIP;
        Collections.shuffle(playQueue);
        playlistId = UUID.randomUUID().toString();
        int random = new Random().nextInt(playQueue.size() - 1);
        update(random);
        play();
        broadcast(PLAYQUEUE_CHANGE);
    }

    /**
     * 修改播放模式
     */

    public void changePlaybackMode() {
        final int[] mode = {
                AudioService.PLAYBACK_MODE_REPEAT,
                AudioService.PLAYBACK_MODE_REPEAT_ONE,
                AudioService.PLAYBACK_MODE_SINGLE };
        int i = 0;
        while (i < mode.length && playbackMode != mode[i]) i++;
        playbackMode = mode[(i+1) % mode.length];
        if (playbackMode == PLAYBACK_MODE_REPEAT_ONE) {
            player.setLooping(true);
        } else {
            player.setLooping(false);
        }
        broadcast(PLAYBACK_MODE, playbackMode);
    }


    public void pause() {
        if (player.isPlaying()) {
            player.pause();
            playbackState = PLAYBACK_STATE_PAUSED;
            broadcast(PLAYBACK_STATE, playbackState);
            updateProgressThread.interrupt();
        }
    }

    /**
     * 修改播放进度
     * @param progress 需改的进度
     */

    public void seekTo(int progress) {
        player.seekTo(progress);
        broadcast(SEEKTO, progress);
    }

    /**
     * 下一曲
     */

    public void skipToNext() {
        update(nextIndex);
        playbackState = PLAYBACK_STATE_SKIP;
        play();
        broadcast(SKIP, SKIP_TO_NEXT);
    }

    /**
     * 上一曲
     */

    public void skipToPrevious() {
        update(previousIndex);
        playbackState = PLAYBACK_STATE_SKIP;
        play();
        broadcast(SKIP, SKIP_TO_PREVIOUS);
    }

    // 添加到或从‘我喜欢’移除
    public boolean toggleLike() {
        return toggleLike(currentSong.getId());
    }

    public boolean toggleLike(int id) {
        boolean ret;
        if (localResourceHelper.like.contains(id)) {
            ret = false;
            System.out.println("dislike");
            localResourceHelper.like.remove(localResourceHelper.db, id);
        } else {
            ret = true;
            System.out.println("like");
            localResourceHelper.like.add(localResourceHelper.db, id);
        }
        if (ret) broadcast(LIKE, LIKE_TRUE);
        else broadcast(LIKE, LIKE_FALSE);

        return ret;
    }

    /**
     * 是否喜欢当前正在播放的歌曲
     * @return 喜欢返回 true 不喜欢 返回 false
     */

    public boolean liked() {
        if(localResourceHelper.like == null || currentSong == null) return false;
        return localResourceHelper.like.contains(currentSong.getId());
    }

    /**
     * 添加线程修改UI的把手
     * 不能再UI线程以外去修改UI，但是可以重过Handler修改
     * @param
     */

    public void addProgressHandler(Handler h) {
        handlers.add(h);
    }

    public void removeProgressHandler(Handler h) {
        handlers.remove(h);
    }

    /**
     * 发送广播给各个组件
     * @param name  广播类型
     * @param arg   广播参数
     */

    public void broadcast(String name, int arg) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_ACTION);
        intent.putExtra(GENRE, name);
        intent.putExtra(MESSAGE, arg);
        sendBroadcast(intent);
    }

    public void broadcast(String name) {
        broadcast(name, -2);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class ServiceBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }
}

