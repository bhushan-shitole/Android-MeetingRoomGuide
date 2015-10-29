package com.example.synerzip.meetingRoomGuide;

import android.app.Activity;
import android.app.ListActivity;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Comparator;
import java.util.Calendar;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ListActivity implements OnItemSelectedListener {
    boolean currentEventFoundFlag = false;
    String previousTitle = "";
    String startTimeForReporting = "";
    String endTimeForReporting = "";
    String titleForReporting = "";
    String mailBodyText = "";
    String meetingRoomName = "";
    String organizer = "";


    public class CalendarList {
        String calendarName;
        String title;
        Date start;
        Date end;
        String organizer;
    }

    View mainRelativeLayout;
    CalendarListAdapter calendarListAdapter;
    List<CalendarList> calendarEventList;
    private static final String DATE_TIME_FORMAT = "h:mm a";
    Drawable newDrawable;
    Map<String, String> calendarResources = new HashMap<String, String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton sendBtn = (ImageButton) findViewById(R.id.sendEmail);
        sendBtn.setVisibility(View.GONE);
        ImageButton quickBookBtn = (ImageButton) findViewById(R.id.quickBook);
        quickBookBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TextView accountName = (TextView) findViewById(R.id.calendarName);

                String emailList = calendarResources.get(accountName.getText().toString());

                Calendar cal = Calendar.getInstance();
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra("beginTime", cal.getTimeInMillis());
                intent.putExtra("allDay", false);
                intent.putExtra("endTime", cal.getTimeInMillis() + 30 * 60 * 1000);
                intent.putExtra("title", "QuickBooking from Meeting Room Guide");
                intent.putExtra(Intent.EXTRA_EMAIL, emailList);
                startActivity(intent);

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    sendEmail();
                } catch (NullPointerException e) {
                    System.out.println("Gmail account is not configured on Device.");
                } catch (Exception e) {
                    System.out.println("Some exception Occurred.");
                }
            }
        });
        // cache current relative layout
        mainRelativeLayout = findViewById(R.id.mainRelativeLayout);

        // set current wallpaper as background to layout
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        Drawable currentWallPaper = wallpaperManager.getDrawable();
        mainRelativeLayout.setBackground(currentWallPaper);

        // Get Bing image and set as background every day
        Timer timer = new Timer();
        timer.schedule(new SetBingImageAsBackground(), 0, DateUtils.DAY_IN_MILLIS);

        /********** Display all calendar names in drop down START ****************/

          String[] EVENT_PROJECTION = new String[] {
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        };

        ArrayList<String> calendarNames = new ArrayList<String>();
        ContentResolver contentResolver = this.getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/calendars"),
                EVENT_PROJECTION, null, null, "name ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String displayName = cursor.getString(2);
                String id = cursor.getString(0);
                String ACCOUNT_NAME = cursor.getString(1);
                String OWNER_ACCOUNT = cursor.getString(3);

                calendarResources.put(displayName, OWNER_ACCOUNT);

System.out.println("calendar name = " + displayName + " id = " + id + " ACCOUNT_NAME = " + ACCOUNT_NAME + " OWNER_ACCOUNT = " + OWNER_ACCOUNT);
                // Filter primary account's calender
//                if (displayName.contains("@")) {
//                    continue;
//                }
                calendarNames.add(displayName);
            }
            cursor.close();

            // To sort with case sensitive uncomment below
//            Collections.sort(calendarNames, CALENDAR_NAME_ORDER);
        }

        // drop down adapter
        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, calendarNames) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "DroidSans.ttf");
                ((TextView) v).setTypeface(externalFont);
                ((TextView) v).setTextSize(20);

                return v;
            }


            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "DroidSans.ttf");
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
    }

    protected void sendEmail() {

        //set the main intent to ACTION_SEND for looking for applications that share information
        Intent intent = new Intent(Intent.ACTION_SEND, null);

        //intent.addCategory(Intent.CATEGORY_LAUNCHER); //if you want extra filters

        //filter out apps that are able to send plain text
        intent.setType("plain/text");

        //get a list of apps that meet your criteria above
        List<ResolveInfo> pkgAppsList = this.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_RESOLVED_FILTER);

        ResolveInfo info = null;
        if (!pkgAppsList.isEmpty()) {
            for (ResolveInfo resolveInfo : pkgAppsList) {
                String packageName = resolveInfo.activityInfo.packageName;

                if (packageName.equals("com.google.android.gm")) { // Select Gmail
                    info = resolveInfo;
                }
            }
        }

        String packageName = info.activityInfo.packageName;
        String className = info.activityInfo.name;

        //set the intent to launch that specific app
        intent.setClassName(packageName, className);

        //some samples on adding more then one email address
        String aEmailList[] = { "rajesh.dhopate@synerzip.com" }; // provide admin's email id
        //Put CC/BCC if any
        // we can put organizer name in CC if requirement is like that
        // String aEmailCCList[] = { "tushar.bende@synerzip.com","bhushan.shitole@synerzip.com"};
        // String aEmailBCCList[] = { "tushar.bende@synerzip.com" };

        //all the extras that will be passed to the email app
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
        // intent.putExtra(android.content.Intent.EXTRA_CC, aEmailCCList);
        // intent.putExtra(android.content.Intent.EXTRA_BCC, aEmailBCCList);
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Meeting Room " + meetingRoomName + " NOT in use but booked.");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, mailBodyText);

        //start the app
        startActivity(intent);

    }
    class SetBingImageAsBackground extends TimerTask {
        public void run() {

            // Link to get the Bing Image of the day in JSON format
            String link = "http://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";
            mainRelativeLayout = findViewById(R.id.mainRelativeLayout);
            new SetBackgroundImageTask().execute(link);

            System.out.println("Bing image updated");
        }
    }


    public class SetBackgroundImageTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                String result = convertStreamToString(input);
                JSONObject jsonObj = new JSONObject(result);
                String imageUrl = jsonObj.getJSONArray("images").getJSONObject(0).getString("url");
                String finalLink = "http://www.bing.com" + imageUrl;

                Bitmap bmp;
                url = new URL(finalLink);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                newDrawable = new BitmapDrawable(getResources(), bmp);

                setCustomBackground();

                System.out.println("Bing Image of the day: " + finalLink);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public String convertStreamToString(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }
    }

    public void setCustomBackground() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("inside setCustomBackground");
                if (newDrawable != null) {
                    mainRelativeLayout.setBackground(newDrawable);
                } else {
                    // set current wallpaper as background to layout
                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                    Drawable currentWallPaper = wallpaperManager.getDrawable();
                    mainRelativeLayout.setBackground(currentWallPaper);
                }
            }
        });
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

        meetingRoomName = selectedCalendarName;
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

            // Set current meeting attributes (bg colour etc).
            // As It is a Current Ongoing meeting Enable the Complaint Button.
            if (meetingStartTimeConvertedToTodayInMillis < calInstanceCurrent.getTimeInMillis() && meetingEndTimeConvertedToTodayInMillis > calInstanceCurrent.getTimeInMillis()) {
                title.setTextColor(Color.WHITE);
                startDate.setTextColor(Color.WHITE);
                endDate.setTextColor(Color.WHITE);
                toDash.setTextColor(Color.WHITE);
                arg1.setBackgroundColor(arg1.getResources().getColor(R.color.material_deep_teal_500));
                titleForReporting = chapter.title;
                organizer = chapter.organizer;

                Date start = new Date(meetingStartTimeConvertedToTodayInMillis);
                Date end = new Date(meetingEndTimeConvertedToTodayInMillis);
                DateFormat formatter = new SimpleDateFormat("dd:MMM:yyyy:HH:mm:ss:a");
                startTimeForReporting = formatter.format(start);
                endTimeForReporting = formatter.format(end);
                mailBodyText = "Hi Admin,\nMeeting room " + meetingRoomName + " is booked for meeting:- " + titleForReporting + "\nStart Time:- " +
                        startTimeForReporting + " &" + " End time:- "+ endTimeForReporting +
                        " but it is not in use\nPlease confirm with the organizer " + organizer + "\nThanks,\nMeeting Room Guide";
                currentEventFoundFlag = true;
                ImageButton sendBtn = (ImageButton) findViewById(R.id.sendEmail);
                sendBtn.setVisibility(View.VISIBLE);
            }

            long compare = Integer.valueOf(calInstanceMeetingEventStart.get(Calendar.HOUR_OF_DAY)).compareTo(calInstanceCurrent.get(Calendar.HOUR_OF_DAY));

            if (compare == 0)//Means Both meeting time in hours is same hence compare minutes
            {
                compare = Integer.valueOf(calInstanceMeetingEventStart.get(Calendar.MINUTE)).compareTo(calInstanceCurrent.get(Calendar.MINUTE));
            }

            if ((compare == 1 && currentEventFoundFlag == false) || (currentEventFoundFlag == true && previousTitle == chapter.title))// no meeting event is highlighted
            {
                arg1.setBackgroundColor(arg1.getResources().getColor(R.color.material_deep_teal_500));
                currentEventFoundFlag = true;// Highlighted the next event
                previousTitle = chapter.title;
                ImageButton sendBtn = (ImageButton) findViewById(R.id.sendEmail);
                sendBtn.setVisibility(View.GONE);
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
        organizer = "";
        ContentResolver contentResolver = context.getContentResolver();
        List<CalendarList> calendarData = new ArrayList<CalendarList>();
        Cursor eventCursor = null;
        Cursor cursor = null;
        // while loading every calender we need to set Complaint button as Disabled as initial state.
        ImageButton sendBtn = (ImageButton) findViewById(R.id.sendEmail);
        sendBtn.setVisibility(View.GONE);

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
                            CalendarContract.Events.DURATION, CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ORGANIZER,
                            CalendarContract.Events.SELF_ATTENDEE_STATUS};

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

                final String selfAttendeeStatus = eventCursor.getString(7);

                // Check to hide declined events by Meeting room
                if (selfAttendeeStatus.equals("2")) {
                    continue;
                }

//                if(organizer.isEmpty())
//                {
                    organizer = eventCursor.getString(6);
//                }

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
                record.organizer = organizer;

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

    // sort calendar names alphabetically
    static final Comparator<String> CALENDAR_NAME_ORDER = new Comparator<String>() {
        public int compare(String e1, String e2) {
            return e1.compareToIgnoreCase(e2);
        }
    };

}



