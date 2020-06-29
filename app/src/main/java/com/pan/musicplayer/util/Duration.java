package com.pan.musicplayer.util;


public class Duration {
    /**
     * 将毫秒转化成字符串 m:ss 格式的字符串
     */
    public static String toString(long duration) {
        int m = (int) (duration / 60000);
        int s = (int) ((duration % 60000) / 1000);

        String min, sec;
        min = sec = "";
        min += m ;

        if (s < 10) {
            sec += "0" + s;
        } else {
            sec += s;
        }
        return min + ":" + sec;
    }
}
