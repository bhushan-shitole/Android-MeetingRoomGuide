package com.example.synerzip.meetingRoomGuide;

import android.app.ListActivity;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.CalendarContract;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Comparator;
import java.util.Calendar;

import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;

import de.greenrobot.event.EventBus;

public class MainActivity extends ListActivity implements OnItemSelectedListener {
    boolean currentEventFoundFlag = false;
    String previousTitle = "";


    public class CalendarList {
        String calendarName;
        String title;
        Date start;
        Date end;
    }

    View mainRelativeLayout;
    Bitmap bkgImage;

    CalendarListAdapter calendarListAdapter;

    List<CalendarList> calendarEventList;

    private static final String DATE_TIME_FORMAT = "h:mm a";

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register as a subscriber
        EventBus.getDefault().register(this);

        // cache current relative layout
        mainRelativeLayout = findViewById(R.id.mainRelativeLayout);

        // set current wallpaper as background to layout
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        Drawable currentWallPaper = wallpaperManager.getDrawable();
        mainRelativeLayout.setBackground(currentWallPaper);

        BroadCastBackgroundImage broadCastImage = new BroadCastBackgroundImage("changeImage");
        broadCastImage.postImage();

//        mainRelativeLayout.setBackgroundColor(getResources().getColor(R.color.highlighted_text_material_light));

        /********** Display all calendar names in drop down START ****************/
        ArrayList<String> calendarNames = new ArrayList<String>();
        ContentResolver contentResolver = this.getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/calendars"),
                (new String[]{"name"}), null, null, "name ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String displayName = cursor.getString(0);

                // Filter primary account's calender
                if (displayName.contains("@")) {
                    continue;
                }
                calendarNames.add(displayName);
                System.out.println("Calendar name: " + displayName);
            }
            cursor.close();
        }

        // drop down adapter
        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, calendarNames){

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont=Typeface.createFromAsset(getAssets(), "DroidSans.ttf");
                ((TextView) v).setTypeface(externalFont);
                ((TextView) v).setTextSize(20);

                return v;
            }


            public View getDropDownView(int position,  View convertView,  ViewGroup parent) {
                View v =super.getDropDownView(position, convertView, parent);

                Typeface externalFont=Typeface.createFromAsset(getAssets(), "DroidSans.ttf");
                ((TextView) v).setTypeface(externalFont);
                ((TextView) v).setTextSize(20);

                return v;
            }
        };

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        /********** Display all calendar names in drop down END ****************/

        // set first calender's name at top right
        TextView accountName = (TextView) findViewById(R.id.calendarName);
        accountName.setText(calendarNames.get(0));
        accountName.setTypeface(Typeface.createFromAsset(getAssets(),
                "DroidSans.ttf"));

        // Display first calender's events in list view
        calendarEventList = getDataForListView(MainActivity.this, calendarNames.get(0));
        calendarListAdapter = new CalendarListAdapter();
        setListAdapter(calendarListAdapter);

        Button changeImageBtn = (Button) findViewById(R.id.changeImage);
        changeImageBtn.setTypeface(Typeface.createFromAsset(getAssets(),
                "DroidSans.ttf"));

        changeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Inside onclick button");
                BroadCastBackgroundImage broadCastImage = new BroadCastBackgroundImage("changeImage");
                broadCastImage.postImage();
            }
        });
    }

    // This function is get called when data is posted through EventBus(callback method)
    public void onEvent(BackgroundImage backgroundImage) {
        // set latest background image to current layout
        bkgImage = backgroundImage.getImage();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Drawable dr = new BitmapDrawable(getResources(), bkgImage);
                mainRelativeLayout.setBackground(dr);
            }
        });
    }

    protected void onDestroy() {
        // Unregister
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        currentEventFoundFlag = false;
        previousTitle = "";
        // An item was selected. You can retrieve the selected item using
        String selectedCalendarName = (String) parent.getItemAtPosition(pos);

        // Display selected calendar name in top right
        TextView calendarName = (TextView) findViewById(R.id.calendarName);
        calendarName.setText(selectedCalendarName);


        // Fetch and display selected calendar's events in list view
        calendarEventList = getDataForListView(MainActivity.this, selectedCalendarName);
        calendarListAdapter = new CalendarListAdapter();
        setListAdapter(calendarListAdapter);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
//        CalendarList chapter = calendarListAdapter.getCalendarEvent(position);
//        Toast.makeText(MainActivity.this, chapter.title, Toast.LENGTH_LONG).show();
    }

    public class CalendarListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return calendarEventList.size();
        }

        @Override
        public CalendarList getItem(int arg0) {
            // TODO Auto-generated method stub
            return calendarEventList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {

            if (arg1 == null) {
                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                arg1 = inflater.inflate(R.layout.listitem, arg2, false);
            }

            TextView title = (TextView) arg1.findViewById(R.id.title);
            TextView startDate = (TextView) arg1.findViewById(R.id.start);
            TextView endDate = (TextView) arg1.findViewById(R.id.end);
            TextView toDash = (TextView) arg1.findViewById(R.id.to);

            // set font face
            title.setTypeface(Typeface.createFromAsset(getAssets(),
                    "DroidSans-Bold.ttf"));
            startDate.setTypeface(Typeface.createFromAsset(getAssets(),
                    "DroidSans.ttf"));
            endDate.setTypeface(Typeface.createFromAsset(getAssets(),
                    "DroidSans.ttf"));
            toDash.setTypeface(Typeface.createFromAsset(getAssets(),
                    "DroidSans.ttf"));


            CalendarList chapter = calendarEventList.get(arg0);

            title.setTextColor(Color.BLACK);
            startDate.setTextColor(Color.BLACK);
            endDate.setTextColor(Color.BLACK);
            toDash.setTextColor(Color.BLACK);
            arg1.setBackgroundColor(Color.TRANSPARENT);

            Calendar calInstanceCurrent = Calendar.getInstance(); // creates calendar
            calInstanceCurrent.setTime(new Date(new Date().getTime())); // sets calendar time/date

            Calendar calInstanceMeetingEventStart = Calendar.getInstance();
            calInstanceMeetingEventStart.setTime(new Date(chapter.start.getTime()));

            Calendar calInstanceMeetingEventEnd = Calendar.getInstance();
            calInstanceMeetingEventEnd.setTime(new Date(chapter.end.getTime()));

            if (!DateUtils.isToday(calInstanceMeetingEventStart.getTimeInMillis())) {
                calInstanceMeetingEventStart.set(calInstanceCurrent.get(Calendar.YEAR), calInstanceCurrent.get(Calendar.MONTH), calInstanceCurrent.get(Calendar.DAY_OF_MONTH));
                calInstanceMeetingEventEnd.set(calInstanceCurrent.get(Calendar.YEAR), calInstanceCurrent.get(Calendar.MONTH), calInstanceCurrent.get(Calendar.DAY_OF_MONTH));
            }
            long meetingStartTimeConvertedToTodayInMillis = calInstanceMeetingEventStart.getTimeInMillis();
            long meetingEndTimeConvertedToTodayInMillis = calInstanceMeetingEventEnd.getTimeInMillis();

            // Set current meeting attributes (bg colour etc)
            if (meetingStartTimeConvertedToTodayInMillis < calInstanceCurrent.getTimeInMillis() && meetingEndTimeConvertedToTodayInMillis > calInstanceCurrent.getTimeInMillis()) {
                title.setTextColor(Color.WHITE);
                startDate.setTextColor(Color.WHITE);
                endDate.setTextColor(Color.WHITE);
                toDash.setTextColor(Color.WHITE);
                arg1.setBackgroundColor(arg1.getResources().getColor(R.color.material_deep_teal_500));
                currentEventFoundFlag = true;
            }


            long compare = Integer.valueOf(calInstanceMeetingEventStart.get(Calendar.HOUR_OF_DAY)).compareTo(calInstanceCurrent.get(Calendar.HOUR_OF_DAY));

            if(compare == 0)//Means Both meeting time in hours is same hence compare minutes
            {
                compare = Integer.valueOf(calInstanceMeetingEventStart.get(Calendar.MINUTE)).compareTo(calInstanceCurrent.get(Calendar.MINUTE));
            }

            if ((compare == 1 && currentEventFoundFlag == false) || (currentEventFoundFlag == true && previousTitle == chapter.title))// no meeting event is highlighted
            {
                arg1.setBackgroundColor(arg1.getResources().getColor(R.color.material_deep_teal_500));
                currentEventFoundFlag = true;// Highlighted the next event
                previousTitle = chapter.title;
            }

            title.setText(chapter.title);
            startDate.setText(new SimpleDateFormat(DATE_TIME_FORMAT).format(calInstanceMeetingEventStart.getTime()));
            endDate.setText(new SimpleDateFormat(DATE_TIME_FORMAT).format(calInstanceMeetingEventEnd.getTime()));

            return arg1;
        }

        public CalendarList getCalendarEvent(int position) {
            return calendarEventList.get(position);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_view_with_simple_adapter, menu);
        return true;
    }

    public List<CalendarList> getDataForListView(MainActivity context, String calendarName) {
        List calendarList = readCalendar(context, calendarName);
        return calendarList;
    }

    public List readCalendar(Context context, String calendarName) {

        ContentResolver contentResolver = context.getContentResolver();
        List<CalendarList> calendarData = new ArrayList<CalendarList>();
        Cursor eventCursor = null;
        Cursor cursor = null;

        try {
            // Fetch all events of selected calendar
            Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
            long now = new Date().getTime();


            Calendar calInstanceE = Calendar.getInstance(); // creates calendar
            calInstanceE.setTime(new Date(now));
            long currentHourOfDay = calInstanceE.get(Calendar.HOUR_OF_DAY);

            ContentUris.appendId(builder, now);
            ContentUris.appendId(builder, (now + (24 - currentHourOfDay) * DateUtils.HOUR_IN_MILLIS));

            final String[] projection = new String[]
                    {CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART,
                            CalendarContract.Events.DTEND, CalendarContract.Events.ACCOUNT_NAME,
                            CalendarContract.Events.DURATION, CalendarContract.Events.ACCESS_LEVEL};

            eventCursor = contentResolver.query(
                    builder.build(), projection, CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ?",
                    new String[]{"" + calendarName}, "DTSTART ASC");

            eventCursor.moveToFirst();
            do {
                CalendarList record = new CalendarList();

                String title = eventCursor.getString(0);
                final Date begin = new Date(eventCursor.getLong(1));
                Date end = new Date(eventCursor.getLong(2));
                final String accountName = eventCursor.getString(3);
                final String duration = eventCursor.getString(4);
                final Long accessLevel = eventCursor.getLong(5);


                if (duration != null) {
                    // Calculation Logic for Meeting end time
                    int durationInSeconds = Integer.parseInt(duration.substring(1, duration.length() - 1));
                    Calendar calInstance = Calendar.getInstance(); // creates calendar
                    calInstance.setTime(new Date(eventCursor.getLong(1))); // sets calendar time/date
                    calInstance.add(Calendar.SECOND, durationInSeconds); // Add Seconds from Duration
                    end = calInstance.getTime();
                }

                if (accessLevel == 2) //Means private Meeting
                {
                    title = "Busy";
                }

                if (title.isEmpty()) // Untitled event
                {
                    title = "Untitled event";
                }

                System.out.println("Title: " + title + " Begin: " + begin + " End: " + end +
                        " accountName: " + accountName + " accessLevel: " + accessLevel);

                record.title = title;
                record.start = begin;
                record.end = end;
                record.calendarName = accountName;

                calendarData.add(record);
            } while (eventCursor.moveToNext());

            Collections.sort(calendarData, SENIORITY_ORDER);

        } catch (Exception ex) {

        } finally {
            try {
                if (eventCursor != null && !eventCursor.isClosed()) {
                    eventCursor.close();
                }

                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }

            } catch (Exception ex) {
            }
        }

        return calendarData;
    }

    static final Comparator<CalendarList> SENIORITY_ORDER = new Comparator<CalendarList>() {
        public int compare(CalendarList e1, CalendarList e2) {
            int compare = e1.start.compareTo(e2.start);
            if (compare == 0) {//Means both events start time is different
                return compare;
            } else {
                Calendar calInstanceE1 = Calendar.getInstance(); // creates calendar
                calInstanceE1.setTime(new Date(e1.start.getTime())); // sets calendar time/date

                Calendar calInstanceE2 = Calendar.getInstance(); // creates calendar
                calInstanceE2.setTime(new Date(e2.start.getTime())); // sets calendar time/date

                compare = Integer.valueOf(calInstanceE1.get(Calendar.HOUR_OF_DAY)).compareTo(calInstanceE2.get(Calendar.HOUR_OF_DAY));
                if (compare == 0)//Means Both meeting time in hours is same hence compare minutes
                {
                    compare = Integer.valueOf(calInstanceE1.get(Calendar.MINUTE)).compareTo(calInstanceE2.get(Calendar.MINUTE));
                }
            }

            return compare;
        }
    };

}



