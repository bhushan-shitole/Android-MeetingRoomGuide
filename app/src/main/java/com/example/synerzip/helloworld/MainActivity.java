package com.example.synerzip.helloworld;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import java.util.TimeZone;

import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;

import de.greenrobot.event.EventBus;

public class MainActivity extends ListActivity implements OnItemSelectedListener {

    public class CalendarList {
        String username;
        String title;
        Date start;
        Date end;
    }

    private Boolean isRegister = false;
    View mainRelativeLayout;
    Bitmap bkgImage;

    CalendarListAdapter calendarListAdapter;

    List<CalendarList> calendarEventList;

    private static final String DATE_TIME_FORMAT = "h:mm a";

    ListView listView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register as a subscriber
        EventBus.getDefault().register(this);
        isRegister = true;

        // cache current relative layout
        mainRelativeLayout = findViewById(R.id.mainRelativeLayout);

        /********** Display all calendar names in drop down START ****************/
        ArrayList <String> calendarNames = new ArrayList<String>();
        ContentResolver contentResolver = this.getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/calendars"),
                (new String[]{"_id", "calendar_displayName", "visible"}), null, null, "_id ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                final String _id = cursor.getString(0);
                final String displayName = cursor.getString(1);
                calendarNames.add(displayName);
            }
            cursor.close();
            Collections.sort(calendarNames, CALENDAR_NAME_ORDER);
        }

        // drop down adapter
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, calendarNames);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        /********** Display all calendar names in drop down END ****************/

        // set first calender's name at top right
        TextView accountName = (TextView)findViewById(R.id.username);
        accountName.setText(calendarNames.get(0));

        // Display first calender's events in list view
        calendarEventList = getDataForListView(MainActivity.this, calendarNames.get(0));
        calendarListAdapter = new CalendarListAdapter();
        setListAdapter(calendarListAdapter);


    }

    public void onEvent(ImageReceiver imageReceiver){
        // set latest background image to current layout
        bkgImage = imageReceiver.getImage();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Drawable dr = new BitmapDrawable(getResources(), bkgImage);
                mainRelativeLayout.setBackground(dr);
            }
        });
        System.out.println("Inside onEvent");
    }

    protected void onDestroy() {
        // Unregister
        super.onDestroy();
        if (isRegister) {
            EventBus.getDefault().unregister(this);
            isRegister = false;
        }
        System.out.println("Inside destroy");
    }

    @Override
    public void onStart(){
        super.onStart();
        if (!isRegister) {
            EventBus.getDefault().register(this);
        }
        System.out.println("Inside start");
    }

    @Override
    public void onStop(){
        super.onStop();
        if (isRegister) {
            EventBus.getDefault().unregister(this);
            isRegister = false;
        }
        System.out.println("Inside stop");
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
         String selectedCalendarName = (String) parent.getItemAtPosition(pos);

        // Display selected calendar name in top right
        TextView calendarName = (TextView)findViewById(R.id.username);
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

            if(arg1==null)
            {
                LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                arg1 = inflater.inflate(R.layout.listitem, arg2,false);
            }

            TextView title = (TextView)arg1.findViewById(R.id.title);
            TextView startDate = (TextView)arg1.findViewById(R.id.start);
            TextView endDate = (TextView)arg1.findViewById(R.id.end);
            TextView duration = (TextView)arg1.findViewById(R.id.to);

            CalendarList chapter = calendarEventList.get(arg0);

            String Day = new SimpleDateFormat("dd").format(chapter.start);
            String today = new SimpleDateFormat("dd").format(new Date());
            long now = new Date().getTime();

            title.setTextColor(Color.BLACK);
            startDate.setTextColor(Color.BLACK);
            endDate.setTextColor(Color.BLACK);
            duration.setTextColor(Color.BLACK);
            arg1.setBackgroundColor(Color.TRANSPARENT);

            // Show current event's text white
            if (DateUtils.isToday(chapter.start.getTime())) {
                if ((chapter.start.getTime() < now) && (chapter.end.getTime() > now)) {
                    title.setTextColor(Color.WHITE);
                    startDate.setTextColor(Color.WHITE);
                    endDate.setTextColor(Color.WHITE);
                    duration.setTextColor(Color.WHITE);

                    arg1.setBackgroundColor(arg1.getResources().getColor(R.color.material_deep_teal_500));
                }
            }

            title.setText(chapter.title);
            startDate.setText(new SimpleDateFormat(DATE_TIME_FORMAT).format(chapter.start));
            endDate.setText(new SimpleDateFormat(DATE_TIME_FORMAT).format(chapter.end));

            return arg1;
        }

        public CalendarList getCalendarEvent(int position)
        {
            return calendarEventList.get(position);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_view_with_simple_adapter, menu);
        return true;
    }

    public List<CalendarList> getDataForListView(MainActivity context, String username) {
        List calendarList = readCalendar(context, username);
        return calendarList;
    }

    public List readCalendar(Context context, String username) {

        ContentResolver contentResolver = context.getContentResolver();
        List<CalendarList> calendarData = new ArrayList<CalendarList>();
        Cursor eventCursor = null;
        Cursor cursor = null;

        try {
                Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
                long now = new Date().getTime();
                ContentUris.appendId(builder, now);
                ContentUris.appendId(builder, now + DateUtils.DAY_IN_MILLIS);

                final String[] projection = new String[]
                        {CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART,
                         CalendarContract.Events.DTEND, CalendarContract.Events.ACCOUNT_NAME, CalendarContract.Events.CALENDAR_TIME_ZONE};

                eventCursor = contentResolver.query(
                        builder.build(), projection, CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ?",
                        new String[]{"" + username}, "DTSTART ASC");

            eventCursor.moveToFirst();
            do {
                CalendarList record = new CalendarList();

                final String title = eventCursor.getString(0);
                final Date begin = new Date(eventCursor.getLong(1));
//                final Date end = new Date(eventCursor.getLong(2));
                final String accountName = eventCursor.getString(3);
                final String duration = eventCursor.getString(4);

                Calendar cal = Calendar.getInstance();
                cal.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
                cal.setTimeInMillis(eventCursor.getLong(2));

                final Date end = cal.getTime();

//                SimpleDateFormat endFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
//                TimeZone tzInAmerica = TimeZone.getTimeZone("Asia/Calcutta");
//                endFormat.setTimeZone(tzInAmerica);
//                endFormat.format(eventCursor.getLong(2));



//                long mTime = end.getTime();
//                System.out.println("isToday: " + DateUtils.isToday(mTime));
                System.out.println("day: " + cal.get(Calendar.DAY_OF_MONTH));
                System.out.println("month: " + cal.get(Calendar.MONTH));
                System.out.println("year: " + cal.get(Calendar.YEAR));
                System.out.println("HR: " + cal.get(Calendar.HOUR));
                System.out.println("Min: " + cal.get(Calendar.MINUTE));
                System.out.println("CALENDAR_TIME_ZONE: " + cal.getTimeZone());

                System.out.println("Title: " + title + " Begin: " + begin + " End: " + end +
                        " accountName: " + accountName);

                record.title = title;
                record.start = begin;
                record.end = end;
                record.username = accountName;

                calendarData.add(record);
            } while (eventCursor.moveToNext());

            Collections.sort(calendarData, SENIORITY_ORDER);

        } catch(Exception ex) {

        } finally {
            try {
                if(eventCursor != null && !eventCursor.isClosed()) {
                    eventCursor.close();
                }

                if(cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }

            } catch(Exception ex) {}
        }

        return calendarData;
    }

    static final Comparator<CalendarList> SENIORITY_ORDER = new Comparator<CalendarList>() {
        public int compare(CalendarList e1, CalendarList e2) {
            int compare = e1.start.compareTo(e2.start);
            if (compare == 0) {
                Calendar e1Start = Calendar.getInstance();
                e1Start.setTime(e1.start);
                Calendar e2Start = Calendar.getInstance();
                e2Start.setTime(e2.start);
                compare = Integer.valueOf(e1Start.get(Calendar.MINUTE)).compareTo(e1Start.get(Calendar.MINUTE));
            }
            return compare;
        }
    };

    // sort calendar names alphabetically
    static final Comparator<String> CALENDAR_NAME_ORDER = new Comparator<String>() {
        public int compare(String e1, String e2) {
            return  e1.compareToIgnoreCase(e2);
        }
    };

}



