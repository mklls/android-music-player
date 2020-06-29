package com.pan.musicplayer.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import com.pan.musicplayer.R;

public class CoverHelper {
    public static Bitmap getCover(String path, Context context) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
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
}
