package com.pan.musicplayer.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pan.musicplayer.model.Playlist;

import java.util.UUID;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static Integer Version = 1;

    public static String PLAYLIST_ID    = "id";
    public static String PLAYLIST_TITLE = "title";
    public static String _PLAYLIST_ID   = "playlist_id";
    public static String AUDIO_ID       = "id";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory cursorFactory, int version) {
        super(context, name, cursorFactory, version);
    }

    public DatabaseHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }

    public DatabaseHelper(Context context, String name) {
        this(context, name, Version);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO 创建数据库后，对数据库的操作
        String sql = "create table playlist("
                + PLAYLIST_ID + " text primary key, "
                + PLAYLIST_TITLE + " text)";
        db.execSQL(sql);
        db.execSQL("insert into playlist("
                + PLAYLIST_ID +", "
                + PLAYLIST_TITLE + ") values (?, ?)",
                new Object[]{UUID.randomUUID().toString(), Playlist.LIKE});

        sql = "create table song(" + _PLAYLIST_ID +" text, "+ AUDIO_ID +" integer)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO 更改数据库版本的操作
        System.out.println("new version" + newVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // TODO 每次成功打开数据库后首先被执行
    }
}
