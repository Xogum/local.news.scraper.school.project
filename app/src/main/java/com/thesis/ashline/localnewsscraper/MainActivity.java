package com.thesis.ashline.localnewsscraper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.thesis.ashline.localnewsscraper.database.DB;
import com.thesis.ashline.localnewsscraper.view.ArticleListActivity;
import com.thesis.ashline.localnewsscraper.view.LoadingActivity;
import com.thesis.ashline.localnewsscraper.view.RegistrationActivity;
import com.thesis.ashline.localnewsscraper.view.TestDBActivity;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {
    DB db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // init preferences
        initialiseSettings();
        db = new DB(this);
//        testDB();
        if (userExists()) {
            openArticleList();
        } else {
            try {
                db.create();
                openRegistration();
            } catch (IOException e) {
                //show alert
            }
        }
    }

    private boolean userExists() {
        SharedPreferences settings = getSharedPreferences(LoadingActivity.USER_DATA, Context.MODE_PRIVATE);
        if (settings.getLong("user_id", 0) != 0)
            return true;
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_activity_list:
                openArticleList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openArticleList() {
        Intent intent = new Intent(this, ArticleListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void testDB() {
        Intent intent = new Intent(this, TestDBActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadDB() {
        Intent intent = new Intent(this, TestDBActivity.class);
        startActivity(intent);
        finish();
    }

    private void openRegistration() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void initialiseSettings() {
        PreferenceManager.setDefaultValues(this, R.xml.pref_date, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_page_size, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_sorting, false);
    }
}
