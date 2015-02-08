package com.thesis.ashline.localnewsscraper.view;

import com.squareup.otto.Subscribe;
import com.thesis.ashline.localnewsscraper.api.OttoGsonRequest;
import com.thesis.ashline.localnewsscraper.api.RouteMaker;
import com.thesis.ashline.localnewsscraper.api.ServiceLocator;
import com.thesis.ashline.localnewsscraper.api.messages.VolleyRequestFailed;
import com.thesis.ashline.localnewsscraper.api.messages.VolleyRequestSuccess;
import com.thesis.ashline.localnewsscraper.model.ActivityViewModel;
import com.thesis.ashline.localnewsscraper.model.User;
import com.thesis.ashline.localnewsscraper.model.UserResponse;
import com.thesis.ashline.localnewsscraper.view.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.thesis.ashline.localnewsscraper.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class LoadingActivity extends Activity {
    private BroadcastReceiver receiver;
    public static final String USER_DATA = "user_data";
    public static final int REGISTER_MODE = 1;
    private ActivityViewModel _model;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_loading);
        setTitle(getResources().getString(R.string.loading));

        ServiceLocator.ensureInitialized(this);
        _model = new ActivityViewModel();

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        // SMS shit//////////////////////////////
        Bundle b = getIntent().getExtras();
        if (b != null) {
            int mode = b.getInt("mode");
            switch (mode) {
                case REGISTER_MODE:
                    final String phonenumber = b.getString("phone");
                    final String username = b.getString("username");
                    IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
                    receiver = new BroadcastReceiver() {

                        @Override
                        public void onReceive(Context context, Intent intent) {
                            Bundle extras = intent.getExtras();

                            if (extras == null)
                                return;

                            Object[] pdus = (Object[]) extras.get("pdus");
                            SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdus[0]);
                            String origNumber = msg.getOriginatingAddress();
//                            String msgBody = msg.getMessageBody();
//                checkIf sms came from app
                            if (PhoneNumberUtils.compare(phonenumber, origNumber)) {
                                Toast.makeText(getApplicationContext(), "Are same", Toast.LENGTH_LONG).show();
                                //send user registration call
                                postUserRegistration(username, phonenumber);

                                //store user id in preferences
                                //proceed to article list
                            }
                        }
                    };
                    registerReceiver(receiver, filter);
                    sendSms(phonenumber);
                    break;

            }
        }
    }

    private void postUserRegistration(String username, String phonenumber) {
        OttoGsonRequest<UserResponse> userRequest;
        User user = new User();
        user.username = username;
        user.phone_number = phonenumber;
        userRequest = RouteMaker.postUser(user);
        ServiceLocator.VolleyRequestQueue.add(userRequest);
    }

    @Subscribe
    public void onUserResponseReceived(VolleyRequestSuccess<UserResponse> message) {
        Log.d("OVDR", "Request end: " + message.requestId);
        saveUserToPreferences(message.response.user);
        openArticleList();
//        updateUiForTestResponseReceived(message);

    }

    @Subscribe
    public void onResponseError(VolleyRequestFailed message) {
        //todo test this
        VolleyRequestFailed f = message;
        Toast.makeText(this, "Network request Error", Toast.LENGTH_SHORT).show();
        openRegistration();
    }

    private void openArticleList() {
        Intent intent = new Intent(this, ArticleListActivity.class);
        startActivity(intent);
    }

    private void openRegistration() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    private void saveUserToPreferences(User user) {
        SharedPreferences settings = getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("user_id", user.id);
        editor.putString("user_username", user.username);
        editor.putString("user_phonenumber", user.phone_number);
        editor.commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
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

    private void registerServiceBus(boolean register) {
        if (register) {
            ServiceLocator.EventBus.register(this);
            ServiceLocator.ResponseBuffer.stopAndProcess();
        } else {
            ServiceLocator.ResponseBuffer.startSaving();
            ServiceLocator.EventBus.unregister(this);
        }
    }

    private void sendSms(String number) {
        String msg = getResources().getString(R.string.verificationMessage);
        SmsManager sm = SmsManager.getDefault();
        try {
            sm.sendTextMessage(number, null, msg, null, null);
        } catch (Exception e) {
            Toast.makeText(this, "Your sms has failed...",
                    	                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            openRegistration();
        }
    }
}
