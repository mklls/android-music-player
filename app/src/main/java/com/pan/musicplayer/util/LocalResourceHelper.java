package com.pan.musicplayer.util;

import com.pan.musicplayer.R;
import com.pan.musicplayer.model.Album;
import com.pan.musicplayer.model.Playlist;
import com.pan.musicplayer.model.Song;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.net.Uri;
import java.util.ArrayList;

/**
 * 工具类，获取所有本地歌曲
 */

public class LocalResourceHelper {
    // 存放所有本地音频资源
    private ArrayList<Song> musics;
    private Context context;
    public SQLiteDatabase db;
    public ArrayList<Playlist> playlists;
    public ArrayList<Album> albums;
    public Playlist like;

    /**
     * 初始化 LocalResource，通过 MediaStore 从本地外置存储读取媒体资源，
     * MediaStore 是 Android 媒体资源数据库，
     * 里面存放了所有android设备上的媒体信息，包括视频，音频，图片等等
     * 位于/data/data/com.android.providers.media/databases/
     * @param context 上下文
     */

    public LocalResourceHelper(Context context) {
        this.context = context;
        db = new DatabaseHelper(context, "data").getWritableDatabase();
        musics = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        init();
    }



    public void init() {
        System.out.println("________________init___start____________");
        LoadMusic();
        LoadPlaylist();
        System.out.println("________________init___end______________");
    }


    private void LoadMusic() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // ContentResolver的query方法接收一下参数
        // uri 要获取的内容的uri
        // projection 从数据库中返回的列 空则返回全部列
        // selection 指定返回行的过滤器 空则返回全部行
        // selectionArgs 过滤器的参数
        // sortOrder 排序顺序
        Cursor c = context.getContentResolver().query(uri, null, null, null, null);
        // c == null 错误， cursor判空应该用 c.getCount()
        if (c.getCount() == 0) return;
        c.moveToFirst();
        do {
            // 获取对应列的索引
            final int i_id = c.getColumnIndex(MediaStore.Audio.Media._ID);
            final int i_title = c.getColumnIndex(MediaStore.Audio.Media.TITLE);
            final int i_artist = c.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            final int i_album = c.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            final int i_duartion = c.getColumnIndex(MediaStore.Audio.Media.DURATION);
            final int i_path = c.getColumnIndex(MediaStore.Audio.Media.DATA);
            final int i_albumid = c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            // 根据索引获取对应列的数据
            final int id = c.getInt(i_id);
            final int album_id = c.getInt(i_albumid);
            final long duration = c.getLong(i_duartion);
            final String title = c.getString(i_title);
            final String artist = c.getString(i_artist);
            final String album = c.getString(i_album);
            final String path = c.getString(i_path);
            // musics ArrayList<Song> 维护所有本地媒体文件
            Song song = new Song(id, title, artist, album, album_id, duration, path);
            musics.add(song);
            // 将歌曲添加到对应的专辑里面
            Album a = null;
            for (Album s : albums) {
                if (s.album_id == album_id) {
                    a = s;
                    break;
                }
            }
            // 如果专辑不存在则创建新的专辑
            if (a == null) {
                a = new Album(album, album_id);
                a.setCover(path);
                a.setArtist(artist);
                albums.add(a);

            }
            a.add(id);
        } while (c.moveToNext());
        c.close();
    }


    private void LoadPlaylist() {
        Cursor c = db.rawQuery("select * from playlist", null);
        // c == null 错误 cursor判空应该用 c.getCount()
        if (c.getCount() == 0) return;
        c.moveToFirst();
        System.out.println("cursor length: " + c.getCount());
        do {
            // 获取列的索引
            final int i_id = c.getColumnIndex(DatabaseHelper.PLAYLIST_ID);
            final int i_title = c.getColumnIndex(DatabaseHelper.PLAYLIST_TITLE);

            // 获取行上每一列的值
            String id = c.getString(i_id);
            String title = c.getString(i_title);

            // 生成播放列表
            Playlist playlist = new Playlist(id, title);
            if (title.equals(Playlist.LIKE)) {
                like = playlist;
            }
            playlists.add(playlist);
        } while (c.moveToNext());

        c = db.rawQuery("select * from song", null);

        if (c.getCount() == 0) return;
        c.moveToFirst();
        do {
            // 获取列的索引
            final int i_playlist_id = c.getColumnIndex(DatabaseHelper._PLAYLIST_ID);
            final int i_id = c.getColumnIndex(DatabaseHelper.AUDIO_ID);

            // 获取行上每一列的值
            int id = c.getInt(i_id);
            String playlist_id = c.getString(i_playlist_id);

            System.out.println("playlist_id: "+ playlist_id + " " + getSongInfo(id));

            // 根据歌曲的id将其添加到对应id的列表
            for (Playlist l : playlists) {
                if (l.getId().equals(playlist_id)) {
                    l.add(id);
                    break;
                }
            }

        } while (c.moveToNext());

        c.close();
    }

    public boolean changePlaylistTitle(String id, String title) {
        for (Playlist l : playlists) {
            if (l.getTitle().equals(title)) {
                return false;
            }
        }

        for (Playlist l : playlists) {
            if (l.getId().equals(id)) {
                l.setTitle(title);
                break;
            }
        }

        db.execSQL("update playlist set title = ? where id = ?", new String[]{title, id});
        return true;
    }

    public void removePlaylist(String id) {
        for (int i = 0; i < playlists.size(); i++) {
            if (playlists.get(i).getId().equals(id)) {
                playlists.remove(i);
                break;
            }
        }

        db.execSQL("delete from playlist where id = ?",
                new Object[]{id});
    }

    public boolean newPlaylist(String title) {

        for (Playlist l : playlists) {
            if (l.getTitle().equals(title)) {
                return false;
            }
        }

        Playlist temp = new Playlist(title);
        playlists.add(temp);
        db.execSQL("insert into playlist(id, title) values(?,?)",
                new Object[] {temp.getId(), title});
        return true;
    }


    /**
     * 获取所有本地歌曲
     * @return 所有歌曲
     */

    public ArrayList<Song> getAll() {
        assert musics != null;
        return musics;
    }

    public void addToPlaylist(int id, int playlistIndex) {
        playlists.get(playlistIndex).add(db,id);
    }

    public void removeFromPlaylist(int id, int playlistindex) {
        playlists.get(playlistindex).remove(db, id);
    }

    /**
     * 将播放列表转化为歌曲列表
     * @param list 播放列表
     * @return ArrayList<Song>  歌曲列表
     */
    public ArrayList<Song> idList2Song(Playlist list) {
        ArrayList<Song> ret = new ArrayList<>();
        ArrayList<Integer> temp = list.getList();

        for (Integer i : temp) {
            ret.add(getSongInfo(i));
        }

        return ret;
    }

    /**
     * 根据歌曲id获取歌曲封面
     * @param id 歌曲id
     * @return 歌曲封面
     */

    public Bitmap getCover(int id) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        String path = getSongInfo(id).getPath();
        mediaMetadataRetriever.setDataSource(path);
        // 获取封面数据
        byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();

        Bitmap bitmap;

        if (picture != null) {
            // 封面存在
            bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
            // 封面不存在
            Resources res = context.getResources();
            // 获取 drawable 文件夹下获取默认封面
            bitmap = BitmapFactory.decodeResource(res, R.drawable.album_default);
        }
        return bitmap;
    }



    /**
     * 获取歌曲信息
     * @param id 歌曲id
     * @return 歌曲信息
     */

    public Song getSongInfo(int id) {
        assert musics != null;
        for (Song s : musics) {
            if (s.getId() == id) {
                return s;
            }
        }
        return musics.get(0);
    }

    /**
     * 获取默认播放队列，默认播放队列根据歌曲列表生成
     * @return 默认播放队列
     */

    public ArrayList<Integer> getDefaultPlayQueue() {
        assert musics != null;
        ArrayList<Integer> queue = new ArrayList<>();
        for (Song s : musics) {
            queue.add(s.getId());
        }
        return queue;
    }
}
