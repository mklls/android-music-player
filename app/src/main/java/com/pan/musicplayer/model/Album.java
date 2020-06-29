package com.pan.musicplayer.model;

import android.database.sqlite.SQLiteDatabase;

public class Album extends Playlist {
    private String cover;
    public final int album_id;
    public String artist;

    public Album(String title, int id) {
        super(title);
        this.album_id = id;
    }

    public String getCover() {
        return cover;
    }

    public int getAlbumId() {
        return album_id;
    }

    public String getArtist() {
        return artist;
    }

    public void setCover(String path) {
        cover = path;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public void add(SQLiteDatabase db, Integer id) {
        return;
    }

    @Override
    public void remove(SQLiteDatabase db, Integer id) {
        return;
    }
}
