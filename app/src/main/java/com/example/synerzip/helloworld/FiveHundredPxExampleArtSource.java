/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.synerzip.helloworld;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Random;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;

import de.greenrobot.event.EventBus;


public class FiveHundredPxExampleArtSource extends RemoteMuzeiArtSource {
    private static final String TAG = "500pxExample";
    private static final String SOURCE_NAME = "FiveHundredPxExampleArtSource";

//    private static final int ROTATE_TIME_MILLIS = 3 * 60 * 60 * 1000; // rotate every 3 hours
    private static final int ROTATE_TIME_MILLIS = 30 * 1000; // rotate every 60 sec

    public static Context context;

    public FiveHundredPxExampleArtSource() {
        super(SOURCE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        String currentToken = (getCurrentArtwork() != null) ? getCurrentArtwork().getToken() : null;

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://api.500px.com")
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addQueryParam("consumer_key", Config.CONSUMER_KEY);
                    }
                })
                .setErrorHandler(new ErrorHandler() {
                    @Override
                    public Throwable handleError(RetrofitError retrofitError) {
                        int statusCode = retrofitError.getResponse().getStatus();
                        if (retrofitError.getKind() == RetrofitError.Kind.NETWORK
                                || (500 <= statusCode && statusCode < 600)) {
                            return new RetryException();
                        }
                        scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
                        return retrofitError;
                    }
                })
                .build();

        FiveHundredPxService service = restAdapter.create(FiveHundredPxService.class);
        FiveHundredPxService.PhotosResponse response = service.getPopularPhotos();

        if (response == null || response.photos == null) {
            throw new RetryException();
        }

        if (response.photos.size() == 0) {
            Log.w(TAG, "No photos returned from API.");
            scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
            return;
        }

        Random random = new Random();
        FiveHundredPxService.Photo photo;
        String token;
        while (true) {
            photo = response.photos.get(random.nextInt(response.photos.size()));
            token = Integer.toString(photo.id);
            if (response.photos.size() <= 1 || !TextUtils.equals(token, currentToken)) {
                break;
            }
        }

System.out.println("image URL: " + photo.image_url);

        context = this.getApplicationContext();
//        new GetImageFromServer().execute(photo.image_url);  // strUrl is your URL

        try {
            LayoutInflater inflater =  LayoutInflater.from(this.getApplicationContext());
            View mainView = inflater.inflate(R.layout.activity_main, null, false);

            // About StrictMode Learn More at => http://stackoverflow.com/questions/8258725/strict-mode-in-android-2-2
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Bitmap myImage = getBitmapFromURL(photo.image_url);

//            ImageReceiver imageReceiver = new ImageReceiver(myImage);
//            imageReceiver.setImage(myImage);
//            EventBus.getDefault().post(imageReceiver);

            WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
            wallpaperManager.getDrawable();

            BroadCastImage broadCastImage = new BroadCastImage(myImage);
            broadCastImage.postImage();




            //BitmapDrawable(obj) convert Bitmap object into drawable object.
//            Drawable dr = new BitmapDrawable(this.getApplicationContext().getResources(), myImage);
//            mainView.setBackground(dr);
//            mainView.setBackground(getDrawable(R.drawable.ic_launcher));

            View root = mainView.getRootView();
            root.setBackgroundColor(Color.BLACK);

//            MainActivity.setActivityBackgroundColor();

            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(i);

        } catch (Exception e) {
            e.printStackTrace();
        }

        publishArtwork(new Artwork.Builder()
                .title(photo.name)
                .byline(photo.user.fullname)
                .imageUri(Uri.parse(photo.image_url))
                .token(token)
                .viewIntent(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://500px.com/photo/" + photo.id)))
                .build());

        scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
    }

    public Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public class GetImageFromServer extends AsyncTask<String, Void, Bitmap>
    {


        private Bitmap image;

        protected void onPreExecute(){
            super.onPreExecute();

        }

        @Override
        protected Bitmap doInBackground(String... params){
            try{

                URL urli = new URL(params[0].trim());
                URLConnection ucon = urli.openConnection();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;


                image = BitmapFactory.decodeStream(ucon.getInputStream(),null,options);

            } catch (MalformedURLException e) {

                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
            }
            return image;  //<<< return Bitmap
        }
        @Override
        protected void onPostExecute(Bitmap image){

            LayoutInflater inflater =  LayoutInflater.from(context);
            View mainView = inflater.inflate(R.layout.activity_main, null, false);

            // About StrictMode Learn More at => http://stackoverflow.com/questions/8258725/strict-mode-in-android-2-2
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);


            //BitmapDrawable(obj) convert Bitmap object into drawable object.
            Drawable dr = new BitmapDrawable(context.getResources(), image);
            mainView.setBackground(dr);

        }


    }
}

