package com.pan.musicplayer.model;

import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.UUID;

public class Playlist {
    public static String LIKE = "我喜欢";

    protected String id;
    protected String title;
    protected ArrayList<Integer> list;

    public Playlist(String id, String title) {
        this.id = id;
        this.title = title;
        this.list = new ArrayList<>();
    }

    public Playlist(String title) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.list = new ArrayList<Integer>();
    }

    public boolean contains(Integer id) {
        return list.contains(id);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public int length() {
        return list.size();
    }

    public void add(int id) {
        list.add(id);
    }

    public void add(SQLiteDatabase db, Integer id) {
        db.execSQL("insert into song(playlist_id, id) values(?, ?)",
                new Object[]{this.id, id});
        list.add(id);
    }

    public void remove(SQLiteDatabase db, Integer id) {
        db.execSQL("delete from song where id = ?",
                new Object[]{id});
        list.remove(id);
    }

    public ArrayList<Integer> getList() {
        return list;
    }

    public void setList(ArrayList<Integer> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        String header = "{" + "id :" + id + ", title: "+ title;

        StringBuilder tmp = new StringBuilder(", content: ");

        for (Integer id: list) {
            tmp.append(id).append(", ");
        }

        return header + tmp + "}";
    }
}
