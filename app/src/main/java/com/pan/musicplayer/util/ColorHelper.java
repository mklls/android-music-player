package com.pan.musicplayer.util;

import android.graphics.Color;
import androidx.palette.graphics.Palette;


public class ColorHelper {
    public static boolean isLightColor(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        if (darkness < 0.2) {
            return true; // It's a light color
        } else {
            return false; // It's a dark color
        }
    }

    public static int getDarkBackgroundColor(Palette palette, int defaultColor) {
        int color = palette.getDominantColor(defaultColor);

        if (ColorHelper.isLightColor(color))
            color = palette.getDarkMutedColor(defaultColor);
        else return color;

        if (ColorHelper.isLightColor(color))
            color = palette.getMutedColor(defaultColor);
        else return color;

        if (ColorHelper.isLightColor(color))
            color = palette.getDarkVibrantColor(defaultColor);
        else return color;

        if (ColorHelper.isLightColor(color))
            color = palette.getVibrantColor(defaultColor);

        return color;
    }
}
