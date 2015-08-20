package com.example.synerzip.helloworld;

import android.graphics.Bitmap;

import de.greenrobot.event.EventBus;

public class ImageReceiver {

    private EventBus bus = EventBus.getDefault();
    private Bitmap image;

    public ImageReceiver(Bitmap NewImage) {
        this.image = NewImage;
    }

    public Bitmap getImage() {
        return this.image;
    }


}
