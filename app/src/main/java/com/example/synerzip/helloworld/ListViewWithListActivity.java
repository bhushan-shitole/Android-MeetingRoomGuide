package com.example.synerzip.helloworld;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * @author Pranay Airan
 * 
 */
public class ListViewWithListActivity extends ListActivity {

//	public class codeLeanChapter {
//		String title;
//		String title;
//	}

    public class CalendarList {
        String id;
        //		String username;
//		Boolean selected;
        String title;
        Date start;
        Date end;
        Boolean allDay;
    }


    CodeLearnAdapter chapterListAdapter;

    List<CalendarList> codeLeanChapterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view_list_activity);

        codeLeanChapterList = getDataForListView(ListViewWithListActivity.this);


        chapterListAdapter = new CodeLearnAdapter();

        setListAdapter(chapterListAdapter);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        CalendarList chapter = chapterListAdapter.getCodeLearnChapter(position);
		
		Toast.makeText(ListViewWithListActivity.this, chapter.title, Toast.LENGTH_LONG).show();
    }
    public class CodeLearnAdapter extends BaseAdapter {


//    	List<codeLeanChapter> codeLeanChapterList = getDataForListView();

//        List<CalendarList> codeLeanChapterList = getDataForListView();


        @Override
		public int getCount() {
			// TODO Auto-generated method stub
			return codeLeanChapterList.size();
		}

		@Override
		public CalendarList getItem(int arg0) {
			// TODO Auto-generated method stub
			return codeLeanChapterList.get(arg0);
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
				LayoutInflater inflater = (LayoutInflater) ListViewWithListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				arg1 = inflater.inflate(R.layout.listitem, arg2,false);
			}
			
			TextView title = (TextView)arg1.findViewById(R.id.textView1);
			TextView chapterDesc = (TextView)arg1.findViewById(R.id.textView2);

            CalendarList chapter = codeLeanChapterList.get(arg0);
			
			title.setText(chapter.title);
			chapterDesc.setText(chapter.title);
			
			return arg1;
		}
		
		public CalendarList getCodeLearnChapter(int position)
		{
			return codeLeanChapterList.get(position);
		}

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_view_with_simple_adapter, menu);
        return true;
    }
    
    public List<CalendarList> getDataForListView(ListViewWithListActivity context)
    {


		List calendarList = readCalendar(context);

//    	List<codeLeanChapter> codeLeanChaptersList = new ArrayList<codeLeanChapter>();
//
//    	for(int i=0;i<10;i++)
//    	{
//
//    		codeLeanChapter chapter = new codeLeanChapter();
//    		chapter.title = "Chapter "+i;
//    		chapter.title = "This is description for chapter "+i;
//    		codeLeanChaptersList.add(chapter);
//    	}
//
    	return calendarList;
    	
    }

    public List readCalendar(Context context) {

        ContentResolver contentResolver = context.getContentResolver();
        List<CalendarList> calendarData = new ArrayList<CalendarList>();


        // Fetch a list of all calendars synced with the device, their display names and whether the
        // user has them selected for display.

        final Cursor cursor = contentResolver.query(Uri.parse("content://com.android.calendar/calendars"),
                (new String[] { "_id", "calendar_displayName", "visible" }), null, null, null);
        // For a full list of available columns see http://tinyurl.com/yfbg76w

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
                    { CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY};

            Cursor eventCursor =   contentResolver.query(
                    builder.build(), projection, CalendarContract.Instances.CALENDAR_ID  + " = ?",
                    new String[]{""+id}, "startDay ASC, startMinute ASC");

            eventCursor.moveToFirst();




            while (eventCursor.moveToNext()) {
                CalendarList record = new CalendarList();

                final String title = eventCursor.getString(0);
                final Date begin = new Date(eventCursor.getLong(1));
                final Date end = new Date(eventCursor.getLong(2));
                final Boolean allDay = !eventCursor.getString(3).equals("0");

                System.out.println("Title: " + title + " Begin: " + begin + " End: " + end +
                        " All Day: " + allDay);

//				record.id = id;
                record.title = title;
                record.start = begin;
//				record.end = end;
//				record.allDay = allDay;

                calendarData.add(record);
            }
        }
        return calendarData;
    }

    /************************************************
     * utility part
     */
    private static final String DATE_TIME_FORMAT = "yyyy MMM dd, HH:mm:ss";
    @SuppressLint("SimpleDateFormat")
    @SuppressWarnings("deprecation")
    public static String getDateTimeStr(int p_delay_min) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
        if (p_delay_min == 0) {
            return sdf.format(cal.getTime());
        } else {
            Date l_time = cal.getTime();
//			l_time.setMinutes(l_time.getMinutes() + p_delay_min);
            cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
            return sdf.format(l_time);
        }
    }
    public static String getDateTimeStr(String p_time_in_millis) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
        Date l_time = new Date(Long.parseLong(p_time_in_millis));
        return sdf.format(l_time);
    }

}
