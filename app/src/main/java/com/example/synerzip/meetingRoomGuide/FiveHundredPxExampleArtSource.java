package com.example.synerzip.meetingRoomGuide;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;

import java.io.IOException;
import java.util.Random;

import de.greenrobot.event.EventBus;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;

public class FiveHundredPxExampleArtSource extends RemoteMuzeiArtSource {
    private static final String TAG = "500pxExample";
    private static final String SOURCE_NAME = "FiveHundredPxExampleArtSource";

//    private static final int ROTATE_TIME_MILLIS = 3 * 60 * 60 * 1000; // rotate every 3 hours
    private static final int ROTATE_TIME_MILLIS = 30 * 1000; // rotate every 60 sec
//    private static final int ROTATE_TIME_MILLIS = 10000; // rotate every 10 sec

    private static Boolean isRegistered = false;
    private static final EventBus bus = EventBus.getDefault();

    public FiveHundredPxExampleArtSource() {
        super(SOURCE_NAME);
    }

        @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
        if (!bus.isRegistered(this) && !isRegistered) {
            bus.register(this);
            isRegistered = true;
            System.out.println("Inside create of 500px class");
        }
    }

    @Override
    public void onDestroy() {
        // Unregister
        super.onDestroy();
        System.out.println("Inside destroy of 500px class");
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
                        if (retrofitError != null) {
                            int statusCode = retrofitError.getResponse().getStatus();
                            if (retrofitError.getKind() == RetrofitError.Kind.NETWORK
                                    || (500 <= statusCode && statusCode < 600)) {
//                            return new RetryException();
//                            return retrofitError;
                            }
                            scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
                            return retrofitError;
                        } else {
                            return null;
                        }
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

            Bitmap myImage = getBitmapFromURL(photo.image_url);
            BroadCastImage broadCastImage = new BroadCastImage(myImage);
            broadCastImage.postImage();

        // To set current image as system background wallpaper, uncomment following code

//        publishArtwork(new Artwork.Builder()
//                .title(photo.name)
//                .byline(photo.user.fullname)
//                .imageUri(Uri.parse(photo.image_url))
//                .token(token)
//                .viewIntent(new Intent(Intent.ACTION_VIEW,
//                        Uri.parse("http://500px.com/photo/" + photo.id)))
//                .build());

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

    // This function is get called when data is posted through EventBus(callback method)
    public void onEvent(BackgroundImageOnClick backgroundImage){
        if (backgroundImage != null) {
            unscheduleUpdate();
            System.out.println("Inside onEvent of FiveHundredPx image");
            new SetBackgroundImageTask().execute();
        }
    }

    public class SetBackgroundImageTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                onTryUpdate(0);
            } catch (RetryException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}

