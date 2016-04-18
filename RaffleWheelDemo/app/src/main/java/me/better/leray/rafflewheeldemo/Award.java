package me.better.leray.rafflewheeldemo;

import android.graphics.Bitmap;

/**
 * Created by lanusiv on 2016/3/29.
 */
public class Award {
    private int angle;
    private String name;
    private Bitmap image;
    private String bgColor;

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }
}
