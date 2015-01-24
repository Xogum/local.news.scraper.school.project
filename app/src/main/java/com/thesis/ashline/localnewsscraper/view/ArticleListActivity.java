package com.thesis.ashline.localnewsscraper.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.thesis.ashline.localnewsscraper.R;
import com.thesis.ashline.localnewsscraper.adapter.FeedListAdapter;
import com.thesis.ashline.localnewsscraper.api.OttoGsonRequest;
import com.thesis.ashline.localnewsscraper.api.RouteMaker;
import com.thesis.ashline.localnewsscraper.api.ServiceLocator;
import com.thesis.ashline.localnewsscraper.api.messages.VolleyRequestSuccess;
import com.thesis.ashline.localnewsscraper.model.ArticleListActivityViewModel;
import com.thesis.ashline.localnewsscraper.model.TestFeedResponse;
import com.thesis.ashline.localnewsscraper.model.TestFeedResponse.FeedItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ArticleListActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private ArticleListActivityViewModel _model;
    private PlaceholderFragment placeholderFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
//            ----------------------
        ServiceLocator.ensureInitialized(this);
        _model = new ArticleListActivityViewModel();
//            ----------------------
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (_model.listenForResponse) {
            ServiceLocator.EventBus.register(this);
            ServiceLocator.ResponseBuffer.stopAndProcess();
        }
        //todo maybe if loading show loader
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (_model.listenForResponse) {
            ServiceLocator.ResponseBuffer.startSaving();
            ServiceLocator.EventBus.unregister(this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("Model", _model);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        _model = (ArticleListActivityViewModel) savedInstanceState.getSerializable("Model");
    }

    public void onTestSectionSelected(int section) {
        OttoGsonRequest<TestFeedResponse> request = RouteMaker.getTestFeed();
        Log.d("OVDR", "Request begin: " + request.requestId);
        ServiceLocator.VolleyRequestQueue.add(request);
        updateUiForRequestSent(request);
    }

    public void onListenForResponseChanged(boolean isChecked) {
        if (isChecked != _model.listenForResponse) {
            _model.listenForResponse = isChecked;
            registerServiceBus(_model.listenForResponse);
            Log.d("OVDR", "Listen for response: " + _model.listenForResponse);
        }
    }

    private void registerServiceBus(boolean register) {
        if (register) {
            ServiceLocator.EventBus.register(this);
            ServiceLocator.ResponseBuffer.stopAndProcess();
        } else {
            ServiceLocator.ResponseBuffer.startSaving();
            ServiceLocator.EventBus.unregister(this);
        }
    }

    public void showDetails(final View view) {
        Intent intent = new Intent(this, ArticleActivity.class);
        // add url to intent extras
        TextView urlTextView = (TextView) view.findViewById(R.id.txtUrl);
        String url = urlTextView.getText().toString();
        intent.putExtra("article_url", "http://www.bbc.com");
        startActivity(intent);
    }

    @Subscribe
    public void onTestResponseReceived(VolleyRequestSuccess<TestFeedResponse> message) {
        Log.d("OVDR", "Request end: " + message.requestId);
        updateUiForResponseReceived(message);
    }

    private void updateUiForRequestSent(OttoGsonRequest<?> request) {
        _model.status = "Sent #" + request.requestId;
        //        bindUi();
        //todo maybe show loader
    }

    private void updateUiForResponseReceived(VolleyRequestSuccess<TestFeedResponse> message) {
        _model.status = "Received #" + message.requestId;
        _model.prevResult = "#" + message.requestId + " -- " + message.response.feed.size();
//        bindUi();
        //todo hide loader
        //todo render shit
        //todo if bla bla is 0 render message
        this.placeholderFragment.renderTestItems(message.response.feed);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        this.placeholderFragment = PlaceholderFragment.newInstance(position + 1);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, placeholderFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                Log.d("section", "1");
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                Log.d("section", "2");
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                Log.d("section", "3");
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                Log.d("section", "4");
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                Log.d("section", "5");
                break;
            case 6:
                mTitle = getString(R.string.title_section6);
                Log.d("section", "6");
                break;
            case 7:
                mTitle = getString(R.string.title_section7);
                Log.d("section", "7");
                onTestSectionSelected(7);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.article_list, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        /**
         * Textview in main fragment
         */
        private TextView textView;
        private ListView listView;
        private FeedListAdapter listAdapter;
        private List<FeedItem> feedItems;
        private String URL_FEED = "http://api.androidhive.info/feed/feed.json";
        private Context applicationContext;
        private static final String TAG = ArticleListActivity.class.getSimpleName();


        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d("zection", "" + getArguments().getInt(ARG_SECTION_NUMBER));
            View rootView = inflater.inflate(R.layout.fragment_article_list, container, false);
            applicationContext = this.getActivity().getApplicationContext();

            listView = (ListView) rootView.findViewById(R.id.list);

            feedItems = new ArrayList<FeedItem>();

            listAdapter = new FeedListAdapter(this.getActivity(), feedItems);
            listView.setAdapter(listAdapter);

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((ArticleListActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        public void renderTestItems(ArrayList<FeedItem> feed) {
            this.feedItems.addAll(feed);
            // notify data changes to list adapter
            listAdapter.notifyDataSetChanged();
        }
//        todo add response subscriptions etc and test api calls
        //todo put appropriate content on the nav drawer
    }

}
