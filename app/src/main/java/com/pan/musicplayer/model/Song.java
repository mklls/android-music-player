package com.pan.musicplayer.model;

import java.io.Serializable;
import java.util.HashMap;

public class Song implements Serializable {
    private final int id;
    private final long duration;
    private final String title;
    private final String artist;
    private final String album;
    private final String path;
    private final int album_id;

    public Song(int id, String title, String artist, String album, int album_id, long duration, String path) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.path = path;
        this.album_id = album_id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getPath() {
        return path;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "{id: " + getId()
                + " title: " + getTitle()
                + " artist: " + getArtist()
                + " album: " + getAlbum()
                + " path: " + getPath()
                + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if((obj == null) || (obj.getClass() != this.getClass()))
            return false;
        return this.id == ((Song) obj).getId();
    }
}
