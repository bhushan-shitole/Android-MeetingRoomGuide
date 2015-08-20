package com.example.synerzip.helloworld;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import de.greenrobot.event.EventBus;

public class BroadCastImage extends BroadcastReceiver {

    private EventBus bus = EventBus.getDefault();
    private Bitmap image;

    public BroadCastImage(Bitmap NewImage) {
        this.image = NewImage;
    }

    public BroadCastImage() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Post the event
        if (this.image != null) {
            ImageReceiver imageReceiver = new ImageReceiver(this.image);
            bus.post(imageReceiver);
            System.out.println("Receiving image not null");
        }
        System.out.println("Receiving image");
    }

    public void postImage() {
        ImageReceiver imageReceiver = new ImageReceiver(this.image);
        bus.post(imageReceiver);
        System.out.println("posting image");

    }
}
