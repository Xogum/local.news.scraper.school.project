package com.thesis.ashline.localnewsscraper.view;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.thesis.ashline.localnewsscraper.R;
import com.thesis.ashline.localnewsscraper.database.City;
import com.thesis.ashline.localnewsscraper.database.DB;

import java.io.IOException;
import java.util.List;

public class TestDBActivity extends ActionBarActivity {

    private TextView txtBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_db);
        this.txtBox = (TextView)findViewById(R.id.txtTestDB);
        testPreLoadedSQLiteDb();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test_db, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void testPreLoadedSQLiteDb() {

        DB db = new DB(this);

        // copy assets DB to app DB.
        try {
            db.create();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }


        // get all locations
        if ( db.open() ) {

            List<City> locations = db.getCities();

            String ctry = locations.get(0).country;
            String cty = locations.get(0).county;
            String name = locations.get(0).name;

            txtBox.setText( ctry + ":" + cty + ":" + name);
        } else {
            // error opening DB.
            txtBox.setText("hapana hapana");
        }
    }
}
