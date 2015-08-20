package com.example.synerzip.helloworld;

import android.graphics.Bitmap;

public class BackgroundImage {

    private Bitmap image;

    public BackgroundImage(Bitmap NewImage) {
        this.image = NewImage;
    }

    public Bitmap getImage() {
        return this.image;
    }


}
