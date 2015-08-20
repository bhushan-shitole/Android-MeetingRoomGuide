package com.example.synerzip.helloworld;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.view.View;
import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Comparator;
import java.util.Objects;

import android.accounts.Account;
import android.accounts.AccountManager;

import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;

public class MainActivity extends ListActivity implements OnItemSelectedListener {

    public class CalendarList {
        Integer id;
        String username;
        String title;
        Date start;
        Date end;
    }

    CalendarListAdapter calendarListAdapter;

    List<CalendarList> calendarEventList;

    // static data
    List<CalendarList> calendarStaticSillyPointData = new ArrayList<CalendarList>();
    List<CalendarList> calendarStaticLongOnData = new ArrayList<CalendarList>();
    List<CalendarList> calendarStaticMidOffData = new ArrayList<CalendarList>();
    List<CalendarList> calendarStaticExtraCoverData = new ArrayList<CalendarList>();
    List<CalendarList> calendarStaticLongOffData = new ArrayList<CalendarList>();
    List<CalendarList> calendarStaticSecondSlipData = new ArrayList<CalendarList>();

    private static final String DATE_TIME_FORMAT = "dd, HH:mm";

    ListView listView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.codelearn_list_home);

        TextView accountName = (TextView)findViewById(R.id.username);
        accountName.setText(getAccountName(this));

        /************ static data start **************/



        for(int i = 0; i< 5; i++) {
            CalendarList record = new CalendarList();
            record.id = i;
            record.title = "SillyPoint event" + i;
            record.start = new Date();
            record.end = new Date();
            record.username = "SillyPoint";

            calendarStaticSillyPointData.add(record);
        }

        for(int i = 0; i< 5; i++) {
            CalendarList record = new CalendarList();
            record.id = i;
            record.title = "LongOn event" + i;
            record.start = new Date();
            record.end = new Date();
            record.username = "LongOn";

            calendarStaticLongOnData.add(record);
        }

        for(int i = 0; i< 5; i++) {
            CalendarList record = new CalendarList();
            record.id = i;
            record.title = "MidOff event" + i;
            record.start = new Date();
            record.end = new Date();
            record.username = "MidOff";

            calendarStaticMidOffData.add(record);
        }

        for(int i = 0; i< 5; i++) {
            CalendarList record = new CalendarList();
            record.id = i;
            record.title = "ExtraCover event" + i;
            record.start = new Date();
            record.end = new Date();
            record.username = "ExtraCover";

            calendarStaticExtraCoverData.add(record);
        }

        for(int i = 0; i< 5; i++) {
            CalendarList record = new CalendarList();
            record.id = i;
            record.title = "LongOff event" + i;
            record.start = new Date();
            record.end = new Date();
            record.username = "LongOff";

            calendarStaticLongOffData.add(record);
        }

        for(int i = 0; i< 5; i++) {
            CalendarList record = new CalendarList();
            record.id = i;
            record.title = "SecondSlip event" + i;
            record.start = new Date();
            record.end = new Date();
            record.username = "SecondSlip";

            calendarStaticSecondSlipData.add(record);
        }



        /************* static data end **************/

//        calendarEventList = getDataForListView(MainActivity.this);

        calendarEventList = calendarStaticSillyPointData;
        calendarListAdapter = new CalendarListAdapter();
        setListAdapter(calendarListAdapter);

        // drop down adapter
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.users_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
         String username = (String) parent.getItemAtPosition(pos);

        switch (username) {
            case "SillyPoint": calendarEventList = calendarStaticSillyPointData;
                            break;
            case "LongOn": calendarEventList = calendarStaticLongOnData;
                break;

            case "MidOff": calendarEventList = calendarStaticMidOffData;
                break;

            case "ExtraCover": calendarEventList = calendarStaticExtraCoverData;
                break;

            case "LongOff": calendarEventList = calendarStaticLongOffData;
                break;
            case "SecondSlip": calendarEventList = calendarStaticSecondSlipData;
                break;
            default:     calendarEventList = calendarStaticSillyPointData;

        }

//        calendarEventList = calendarStaticSillyPointData;
        calendarListAdapter = new CalendarListAdapter();
        setListAdapter(calendarListAdapter);


//        calendarStaticData
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        CalendarList chapter = calendarListAdapter.getCalendarEvent(position);

        Toast.makeText(MainActivity.this, chapter.title, Toast.LENGTH_LONG).show();
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

            CalendarList chapter = calendarEventList.get(arg0);

            String Day = new SimpleDateFormat("yyyy MMM dd").format(chapter.start);
            String today = new SimpleDateFormat("yyyy MMM dd").format(new Date());

//            if (Day.contentEquals(today)){
//                System.out.println("bhushan : " + new Date().toString());
//                title.setBackgroundColor(Color.BLUE);
//                chapterDesc.setBackgroundColor(Color.BLUE);
//            }



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

    public List<CalendarList> getDataForListView(MainActivity context) {
        List calendarList = readCalendar(context);
        return calendarList;
    }

    public List readCalendar(Context context) {

        ContentResolver contentResolver = context.getContentResolver();
        List<CalendarList> calendarData = new ArrayList<CalendarList>();
        Cursor eventCursor = null;
        Cursor cursor = null;

        try {
            // Fetch a list of all calendars synced with the device, their display names and whether the
            // user has them selected for display.

            cursor = contentResolver.query(Uri.parse("content://com.android.calendar/calendars"),
                    (new String[]{"_id", "calendar_displayName", "visible"}), null, null, "_id ASC");

            HashSet<String> calendarIds = new HashSet<String>();

            if (cursor != null) {
                while (cursor.moveToNext()) {

                    final String _id = cursor.getString(0);
                    final String displayName = cursor.getString(1);
                    final Boolean selected = !cursor.getString(2).equals("0");

                    System.out.println("Id: " + _id + " Display Name: " + displayName + " Selected: " + selected);
                    calendarIds.add(_id);
                }
            }

            // For each calendar, display all the events from the previous week to the end of next week.
            for (String id : calendarIds) {

                Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
                long now = new Date().getTime();
                ContentUris.appendId(builder, now - DateUtils.WEEK_IN_MILLIS);
                ContentUris.appendId(builder, now + DateUtils.WEEK_IN_MILLIS);

                final String[] projection = new String[]
                        {CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ACCOUNT_NAME};

                eventCursor = contentResolver.query(
                        builder.build(), projection, CalendarContract.Instances.CALENDAR_ID + " = ?",
                        new String[]{"" + id}, "CALENDAR_ID ASC");

                eventCursor.moveToFirst();

                while (eventCursor.moveToNext()) {
                    CalendarList record = new CalendarList();

                    final String title = eventCursor.getString(0);
                    final Date begin = new Date(eventCursor.getLong(1));
                    final Date end = new Date(eventCursor.getLong(2));
                    final String accountName = eventCursor.getString(3);

                    System.out.println("Title: " + title + " Begin: " + begin + " End: " + end +
                            " accountName: " + accountName);

                    record.id = Integer.parseInt(id);
                    record.title = title;
                    record.start = begin;
                    record.end = end;
                    record.username = accountName;

                    calendarData.add(record);
                }
            }

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
            return e1.start.compareTo(e2.start);
        }
    };

    static String getAccountName(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = getAccount(accountManager);
        if (account == null) {
            return null;
        } else {
            return account.name;
        }
    }
    private static Account getAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        } return account;
    }

}



