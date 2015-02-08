package com.thesis.ashline.localnewsscraper.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.thesis.ashline.localnewsscraper.R;
import com.thesis.ashline.localnewsscraper.adapter.ArticleListAdapter;
import com.thesis.ashline.localnewsscraper.adapter.FeedListAdapter;
import com.thesis.ashline.localnewsscraper.api.OttoGsonRequest;
import com.thesis.ashline.localnewsscraper.api.RouteMaker;
import com.thesis.ashline.localnewsscraper.api.ServiceLocator;
import com.thesis.ashline.localnewsscraper.api.messages.VolleyRequestFailed;
import com.thesis.ashline.localnewsscraper.api.messages.VolleyRequestSuccess;
import com.thesis.ashline.localnewsscraper.model.ActivityViewModel;
import com.thesis.ashline.localnewsscraper.model.Article;
import com.thesis.ashline.localnewsscraper.model.ArticleListResponse;
import com.thesis.ashline.localnewsscraper.model.Search;
import com.thesis.ashline.localnewsscraper.model.TestFeedResponse;
import com.thesis.ashline.localnewsscraper.model.TestFeedResponse.FeedItem;

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

    private ActivityViewModel _model;
    private PlaceholderFragment placeholderFragment;
    private long userId, articleId;
    private final int SEARCH = 1;
    private final int READS = 2;
    private final int LIKES = 3;
    private final int FAVOURITES = 4;
    private final int SAVES = 5;
    private final int SHARES = 6;
    private final int TEST = 7;
    private final String[] actions = {"", "", "reads", "likes", "favourites", "saves", "shares"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
//            ----------------------
        ServiceLocator.ensureInitialized(this);
        _model = new ActivityViewModel();
//            ----------------------
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        SharedPreferences settings = getSharedPreferences(LoadingActivity.USER_DATA, Context.MODE_PRIVATE);
        userId = settings.getLong("user_id", 0);
        if (userId == 0) {
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        }
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
        _model = (ActivityViewModel) savedInstanceState.getSerializable("Model");
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
        TextView idTextView = (TextView) view.findViewById(R.id.txtId);
        String url = urlTextView.getText().toString();
        String id = idTextView.getText().toString();
        intent.putExtra("article_url", "http://www.bbc.com");
        intent.putExtra("article_id", id);
        startActivity(intent);
    }


    @Subscribe
    public void onArticleResponseReceived(VolleyRequestSuccess<ArticleListResponse> message) {
        Log.d("OVDR", "Request end: " + message.requestId);
        updateUiForArticleResponseReceived(message);
    }

    @Subscribe
    public void onTestResponseReceived(VolleyRequestSuccess<TestFeedResponse> message) {
        Log.d("OVDR", "Request end: " + message.requestId);
        updateUiForTestResponseReceived(message);
    }

    @Subscribe
    public void onResponseError(VolleyRequestFailed what) {
        //todo test this
    }

    private void updateUiForRequestSent(OttoGsonRequest<?> request) {
        _model.status = "Sent #" + request.requestId;
        //        bindUi();
        //todo maybe show loader
    }

    private void updateUiForTestResponseReceived(VolleyRequestSuccess<TestFeedResponse> message) {
        _model.status = "Received #" + message.requestId;
        _model.prevResult = "#" + message.requestId + " -- " + message.response.feed.size();
//        bindUi();
        //todo hide loader
        //todo render shit
        this.placeholderFragment.renderTestItems(message.response.feed);
    }

    private void updateUiForArticleResponseReceived(VolleyRequestSuccess<ArticleListResponse> message) {
        _model.status = "Received #" + message.requestId;
        _model.prevResult = "#" + message.requestId + " -- " + message.response.articles.size();
//        bindUi();
        //todo hide loader
        //todo render shit
        this.placeholderFragment.renderArticles(message.response.articles);
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
        OttoGsonRequest<TestFeedResponse> testRequest;
        OttoGsonRequest<ArticleListResponse> articleRequest;
        switch (number) {
            case SEARCH:
                mTitle = getString(R.string.title_section1);
//                onActionSelected(SEARCH);
                Search search = new Search();
                //todo fill search
                //add userid to search, for logging
                articleRequest = RouteMaker.getArticles(search);
                Log.d("OVDR", "Request begin: " + articleRequest.requestId);
                ServiceLocator.VolleyRequestQueue.add(articleRequest);
                updateUiForRequestSent(articleRequest);
                break;
            case READS:
                mTitle = getString(R.string.title_section2);
//                onActionSelected(READS);
                articleRequest = RouteMaker.getUserAction(userId, actions[READS]);
                Log.d("OVDR", "Request begin: " + articleRequest.requestId);
                ServiceLocator.VolleyRequestQueue.add(articleRequest);
                updateUiForRequestSent(articleRequest);
                break;
            case LIKES:
                mTitle = getString(R.string.title_section3);
//                onActionSelected(LIKES);
                articleRequest = RouteMaker.getUserAction(userId, actions[LIKES]);
                Log.d("OVDR", "Request begin: " + articleRequest.requestId);
                ServiceLocator.VolleyRequestQueue.add(articleRequest);
                updateUiForRequestSent(articleRequest);
                break;
            case FAVOURITES:
                mTitle = getString(R.string.title_section4);
//                onActionSelected(FAVOURITES);
                articleRequest = RouteMaker.getUserAction(userId, actions[FAVOURITES]);
                Log.d("OVDR", "Request begin: " + articleRequest.requestId);
                ServiceLocator.VolleyRequestQueue.add(articleRequest);
                updateUiForRequestSent(articleRequest);
                break;
            case SAVES:
                mTitle = getString(R.string.title_section5);
//                onActionSelected(SAVES);
                articleRequest = RouteMaker.getUserAction(userId, actions[SAVES]);
                Log.d("OVDR", "Request begin: " + articleRequest.requestId);
                ServiceLocator.VolleyRequestQueue.add(articleRequest);
                updateUiForRequestSent(articleRequest);
                break;
            case SHARES:
                mTitle = getString(R.string.title_section6);
//                onActionSelected(SHARES);
                articleRequest = RouteMaker.getUserAction(userId, actions[SHARES]);
                Log.d("OVDR", "Request begin: " + articleRequest.requestId);
                ServiceLocator.VolleyRequestQueue.add(articleRequest);
                updateUiForRequestSent(articleRequest);
                break;
            case 7:
                mTitle = getString(R.string.title_section7);
//                onActionSelected(TEST);
                testRequest = RouteMaker.getTestFeed();
                Log.d("OVDR", "Request begin: " + testRequest.requestId);
                ServiceLocator.VolleyRequestQueue.add(testRequest);
                updateUiForRequestSent(testRequest);
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
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_example:
                SharedPreferences preferences = getSharedPreferences(LoadingActivity.USER_DATA, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.commit();
                Toast.makeText(this, "user prefs cleared", Toast.LENGTH_SHORT).show();
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
        private ArticleListAdapter articleListAdapter;
        private FeedListAdapter feedListAdapter;
        private List<FeedItem> feedItems;
        private String URL_FEED = "http://api.androidhive.info/feed/feed.json";
        private Context applicationContext;
        private static final String TAG = ArticleListActivity.class.getSimpleName();
        private List<Article> articleList;


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
            listView.setEmptyView(rootView.findViewById(R.id.emptyElement));
            feedItems = new ArrayList<FeedItem>();
            articleList = new ArrayList<Article>();

            feedListAdapter = new FeedListAdapter(this.getActivity(), feedItems);
            articleListAdapter = new ArticleListAdapter(this.getActivity(), articleList);

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((ArticleListActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        public void renderTestItems(ArrayList<FeedItem> feed) {
            listView.setAdapter(feedListAdapter);
            this.feedItems.addAll(feed);
            // notify data changes to list adapter
            feedListAdapter.notifyDataSetChanged();
        }

        public void renderArticles(ArrayList<Article> articles) {
            //todo handle different responses, shares/likes etc
            listView.setAdapter(articleListAdapter);
            this.articleList.addAll(articles);
            // notify data changes to list adapter
            articleListAdapter.notifyDataSetChanged();

        }
//        todo add response subscriptions etc and test api calls
        //todo put appropriate content on the nav drawer
    }

}
