package com.example.synerzip.helloworld;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import de.greenrobot.event.EventBus;

public class BroadCastBackgroundImage {
    private EventBus bus = EventBus.getDefault();
    private String image;

    public BroadCastBackgroundImage(String NewImage) {
        this.image = NewImage;
    }

    public BroadCastBackgroundImage() {

    }

    public void onReceive(Context context, Intent intent) {
        // Post the event
        if (this.image != null) {
            BackgroundImageOnClick backgroundImage = new BackgroundImageOnClick(this.image);
            bus.post(backgroundImage);
            System.out.println("Receiving image not null");
        }
        System.out.println("Receiving image");
    }

    public void postImage() {
        BackgroundImageOnClick backgroundImage = new BackgroundImageOnClick(this.image);
        bus.post(backgroundImage);
        System.out.println("posting changed image");

    }
}
