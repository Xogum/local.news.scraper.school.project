package com.thesis.ashline.localnewsscraper.api;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.squareup.otto.Bus;

/**
 * Created by ashfire on 1/15/15.
 */

public class ServiceLocator {
    public static Bus EventBus;
    public static RequestQueue VolleyRequestQueue;
    public static OttoGsonResponseBuffer ResponseBuffer;
    private static boolean _isInitialized;
    public static void ensureInitialized(Context context) {
        if (!_isInitialized) {
            _isInitialized = true;
            VolleyRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
            EventBus = new Bus();
            ResponseBuffer = new OttoGsonResponseBuffer(EventBus);
        }
    }
}