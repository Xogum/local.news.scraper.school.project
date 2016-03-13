package com.thesis.ashline.localnewsscraper.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ArticleListActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult>{

    protected static final String TAG = "location-settings";

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
    private final int TEST = 6;

    private int currentList = -1;
    private int pageOffset = 0;

    private SharedPreferences prefs;
    private Search search;
    private final String[] actions = {"", "", "reads", "likes", "favourites", "saves"};
//    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private TextView mLocationText;
    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    protected final static String KEY_LOCATION = "location";
    protected final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;
    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;
    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }
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
        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (_model.listenForResponse) {
            ServiceLocator.EventBus.register(this);
            ServiceLocator.ResponseBuffer.stopAndProcess();
        }
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        
        placeholderFragment.initialiseLists();
        onSectionAttached(currentList, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (_model.listenForResponse) {
            ServiceLocator.ResponseBuffer.startSaving();
            ServiceLocator.EventBus.unregister(this);
        }
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("Model", _model);
        outState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        outState.putParcelable(KEY_LOCATION, mCurrentLocation);
        outState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        _model = (ActivityViewModel) savedInstanceState.getSerializable("Model");
    }

    protected synchronized void buildGoogleApiClient() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
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
        int count = 1;
        Intent intent = new Intent(this, ArticleActivity.class);
        // add link to intent extras
        TextView urlTextView = (TextView) view.findViewById(R.id.txtUrl);
        TextView idTextView = (TextView) view.findViewById(R.id.txtId);
        // increase readCount
        TextView readCountTextView = (TextView) view.findViewById(R.id.readCount);
        count += Integer.parseInt(readCountTextView.getText().toString());
        readCountTextView.setText(String.valueOf(count));

        String url = urlTextView.getText().toString();
        String id = idTextView.getText().toString();

        intent.putExtra("article_url", url);
        intent.putExtra("article_id", id);

        startActivity(intent);
    }

    @Subscribe
    public void onResponseReceived(VolleyRequestSuccess<?> message) {
        _model.status = "Received #" + message.requestId;
        _model.prevResult = "#" + message.requestId + " -- ";
       if (message.response instanceof ArticleListResponse){
            ArticleListResponse response = (ArticleListResponse) message.response;
           updateUiResponseReceived(response);
       }
        else if(message.response instanceof TestFeedResponse){
           TestFeedResponse response = (TestFeedResponse) message.response;
           updateUiResponseReceived(response);
       }
        Log.d("OVDR", "Request end: " + message.requestId);
    }

    private void updateUiResponseReceived(TestFeedResponse response) {
        //todo hide loader
        //todo render shit
        this.placeholderFragment.renderTestItems(response.feed);
    }
    private void updateUiResponseReceived(ArticleListResponse response) {
        //todo hide loader
        //todo render shit
        ArrayList<Article> modifiedArticles = switchImages(response.articles);
        this.placeholderFragment.renderArticles(modifiedArticles);
    }

    private ArrayList<Article> switchImages(ArrayList<Article> articles) {
        for(Article article: articles) {
            if(article.image_link != null && article.image_link.contains("thumb")) {
                article.icon_url = article.image_link;
                article.image_link = null;
            } else {
                article.icon_url = "http://i61.tinypic.com/11wavlj.png";
            }
        }

        return articles;
    }

    @Subscribe
    public void onResponseError(VolleyRequestFailed message) {
        //todo test this
        Toast.makeText(this, "Network request Error", Toast.LENGTH_SHORT).show();
    }

    private void updateUiForRequestSent(OttoGsonRequest<?> request) {
        _model.status = "Sent #" + request.requestId;
        //        bindUi();
        //todo maybe show loader
    }

    private void updateUiForTestResponseReceived(VolleyRequestSuccess<TestFeedResponse> message) {
//        _model.status = "Received #" + message.requestId;
//        _model.prevResult = "#" + message.requestId + " -- " + message.response.feed.size();
//        bindUi();
        //todo hide loader
        //todo render shit
        this.placeholderFragment.renderTestItems(message.response.feed);
    }

    private void updateUiForArticleResponseReceived(VolleyRequestSuccess<ArticleListResponse> message) {
//        _model.status = "Received #" + message.requestId;
//        _model.prevResult = "#" + message.requestId + " -- " + message.response.articles.size();
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

    public void onSectionAttached(int number, boolean resetPage) {
        pageOffset = currentList == number && !resetPage ? pageOffset : 0;

        currentList = number;

        switch (number) {
            case SEARCH:
                dispatchRequest(SEARCH, getString(R.string.title_section1));
                break;
            case READS:
                dispatchRequest(READS, getString(R.string.title_section2));
                break;
            case LIKES:
                dispatchRequest(LIKES, getString(R.string.title_section3));;
                break;
            case FAVOURITES:
                dispatchRequest(FAVOURITES, getString(R.string.title_section4));
                break;
            case SAVES:
                dispatchRequest(SAVES, getString(R.string.title_section5));
                break;
            case TEST:
                dispatchRequest(TEST, getString(R.string.title_section7));
                break;
        }
    }

    private void dispatchRequest(int action, String title) {
        OttoGsonRequest<TestFeedResponse> testRequest;
        OttoGsonRequest<ArticleListResponse> articleRequest;

        mTitle = title;
        search = getSearch();

        switch(action) {
            case SEARCH:
                articleRequest = RouteMaker.getArticles(search);
                break;
            case TEST:
                testRequest = RouteMaker.getTestFeed();
                Log.d("OVDR", "Request begin: " + testRequest.requestId);
                ServiceLocator.VolleyRequestQueue.add(testRequest);
                updateUiForRequestSent(testRequest);
                return;
            default:
                articleRequest = RouteMaker.getUserAction(userId, actions[action], search);

        }

        Log.d("OVDR", "Request begin: " + articleRequest.requestId);
        ServiceLocator.VolleyRequestQueue.add(articleRequest);
        updateUiForRequestSent(articleRequest);

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
            case R.id.action_refresh:
                placeholderFragment.initialiseLists();
                onSectionAttached(currentList, true);
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
//            case R.id.action_reset_user:
//                SharedPreferences preferences = getSharedPreferences(LoadingActivity.USER_DATA, Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor = preferences.edit();
//                editor.clear();
//                editor.commit();
//                Toast.makeText(this, "user prefs cleared", Toast.LENGTH_SHORT).show();
//                //todo send request to backend to delete user
//                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateLocationUI();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }
    /*location shit*/
    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = true;
//                setButtonsEnabledState();
            }
        });

    }
    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }
//            updateUI();//todo save location
        }
    }
    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Check if the device's location settings are adequate for the app's needs using the
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} method, with the results provided through a {@code PendingResult}.
     */
    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }
    /**
     * The callback invoked when
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} is called. Examines the
     * {@link com.google.android.gms.location.LocationSettingsResult} object and determines if
     * location settings are adequate. If they are not, begins the process of presenting a location
     * settings dialog to the user.
     */
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(ArticleListActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            SharedPreferences settings = getSharedPreferences(LoadingActivity.USER_DATA, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putFloat("user_location_lng", (float) mCurrentLocation.getLongitude());
            editor.putFloat("user_location_lat", (float) mCurrentLocation.getLatitude());
            editor.commit();
        }else{
            checkLocationSettings();
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
//                setButtonsEnabledState();
            }
        });
    }
    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateLocationUI();
    }

    public Search getSearch() {
        /**    possible params
         *  long user_id;
         *  long category_id;
         *  String date_to;
         *  String date_from;
         *  String longitude;
         *  String latitude;
         */

        String location;
        String[] coordinates;

        search = new Search();

        if(prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(this);
        }

        search.put("user_id", userId);
        search.put("category_id", prefs.getString("category", ""));
        search.put("radius", prefs.getString("radius", ""));
        search.put("date_from", prefs.getString("date_from", ""));
        // pagination
        search.put("offset", pageOffset * Integer.parseInt(prefs.getString("page_size", "10")));
        search.put("limit", prefs.getString("page_size", "10"));

        if(!prefs.getBoolean("date_to_today", false)) {
            search.put("date_to", prefs.getString("date_to", ""));
        }

        //  get location
        location = prefs.getString("location", "");

        if(!"".equals(location)) {
            coordinates = location.split(",");
            search.put("lat", coordinates[0]);
            search.put("lng", coordinates[1]);
        }

        // sorting
        if(prefs.getBoolean("order_by_likes", false)) {
            search.putSortParameter("likes", prefs.getBoolean("order_by_likes_descending", false));
        }

        if(prefs.getBoolean("order_by_reads", false)) {
            search.putSortParameter("readz", prefs.getBoolean("order_by_reads_descending", false));
        }

        if(prefs.getBoolean("order_by_distance", false)) {
            search.putSortParameter("distance", prefs.getBoolean("order_by_distance_descending", false));
        }

        if(prefs.getBoolean("order_by_date", false)) {
            search.putSortParameter("date", prefs.getBoolean("order_by_date_descending", false));
        }

        return search;
    }

    private void fetchMoreArticles() {
        pageOffset += 1;
        onSectionAttached(currentList, false);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements AdapterView.OnItemClickListener {
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
        private Button btnLoadMore;
        private View loadingSpinner;
        private ArticleListAdapter articleListAdapter;
        private FeedListAdapter feedListAdapter;
        private List<FeedItem> feedItems;
        private String URL_FEED = "http://api.androidhive.info/feed/feed.json";
        private Context applicationContext;
        private static final String TAG = ArticleListActivity.class.getSimpleName();
        private List<Article> articleList;
        private ArticleListActivity parentActivity;


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
            parentActivity = (ArticleListActivity) this.getActivity();

            listView = (ListView) rootView.findViewById(R.id.list);
            listView.setEmptyView(rootView.findViewById(R.id.emptyElement));
            listView.setOnItemClickListener(this);

            btnLoadMore = (Button) inflater.inflate(R.layout.load_more_button, listView, false);
            btnLoadMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fetchMoreArticles();
                }
            });

            loadingSpinner = inflater.inflate(R.layout.loading_spinner, listView, false);
            listView.addFooterView(loadingSpinner, null, false);

            listView.addFooterView(btnLoadMore);

            loadingSpinner.setVisibility(View.GONE);

            initialiseLists();

            return rootView;
        }

        // onclicklistener for list items
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            int count = 1;
            Intent intent = new Intent(this.getActivity(), ArticleActivity.class);
            Article article = (Article) adapterView.getAdapter().getItem(i);

            // increase readCount
            TextView readCountTextView = (TextView) view.findViewById(R.id.readCount);
            count += Integer.parseInt(readCountTextView.getText().toString());
            readCountTextView.setText(String.valueOf(count));

            intent.putExtra("article_url", article.link);
            intent.putExtra("article_id", article.id);
            intent.putExtra("iliked", article.iliked);
            intent.putExtra("isaved", article.isaved);
            intent.putExtra("ifavourited", article.ifavourited);

            startActivity(intent);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((ArticleListActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER), true);
        }

        private void initialiseLists() {
            feedItems = new ArrayList<FeedItem>();
            articleList = new ArrayList<Article>();

            feedListAdapter = new FeedListAdapter(this.getActivity(), feedItems);
            articleListAdapter = new ArticleListAdapter(this.getActivity(), articleList);

            listView.setAdapter(articleListAdapter);
        }

        public void fetchMoreArticles() {
            btnLoadMore.setVisibility(View.GONE);
            loadingSpinner.setVisibility(View.VISIBLE);
            parentActivity.fetchMoreArticles();
        }

        public void renderTestItems(ArrayList<FeedItem> feed) {
            listView.setAdapter(feedListAdapter);
            this.feedItems.addAll(feed);
            // notify data changes to list adapter
            feedListAdapter.notifyDataSetChanged();
        }

        public void renderArticles(ArrayList<Article> articles) {
            //todo handle different responses, shares/likes etc

            this.articleList.addAll(articles);
            // notify data changes to list adapter
            articleListAdapter.notifyDataSetChanged();

            if(articles.size() == 0) {
                btnLoadMore.setVisibility(View.GONE);
            } else {
                btnLoadMore.setVisibility(View.VISIBLE);
            }

            loadingSpinner.setVisibility(View.GONE);

        }
//        todo add response subscriptions etc and test api calls
        //todo put appropriate content on the nav drawer
    }

}
