package com.example.synerzip.helloworld;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.view.View;
import android.app.Activity;

public class MainActivity extends Activity {

    ListView listView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.codelearn_list_home);

        Button simpleAdapter = (Button)findViewById(R.id.button1);
        Button baseAdapter = (Button)findViewById(R.id.button2);
        Button listAct = (Button)findViewById(R.id.button3);

        simpleAdapter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent simple = new Intent(MainActivity.this,ListViewWithSimpleAdapter.class);
                startActivity(simple);

            }
        });

        baseAdapter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent base = new Intent(MainActivity.this,ListViewWithBaseAdapter.class);
                startActivity(base);

            }
        });

        listAct.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent base = new Intent(MainActivity.this, ListViewWithListActivity.class);
                startActivity(base);

            }
        });


//        listView = (ListView) findViewById(R.id.listView);
//
////         Defined Array values to show in ListView
//        String[] values = new String[] { "Android List View",
//                "Adapter implementation",
//                "Simple List View In Android",
//                "Create List View Android",
//                "Android Example",
//                "List View Source Code",
//                "List View Array Adapter",
//                "Android Example List View"
//        };
//
//
//        // Define a new Adapter
//        // First parameter - Context
//        // Second parameter - Layout for the row
//        // Third parameter - ID of the TextView to which the data is written
//        // Forth - the Array of data
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1, android.R.id.text1, values);
//
//
//        // Assign adapter to ListView
//        listView.setAdapter(adapter);
//
//        // ListView Item Click Listener
//        listView.setOnItemClickListener(new OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//
//                // ListView Clicked item index
//                int itemPosition     = position;
//
//                // ListView Clicked item value
//                String  itemValue    = (String) listView.getItemAtPosition(position);
//
//                // Show Alert
//                Toast.makeText(getApplicationContext(),
//                        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
//                        .show();
//
//            }
//        });

    }





}



