package com.thesis.ashline.localnewsscraper.api;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.squareup.otto.Bus;
import com.thesis.ashline.localnewsscraper.api.messages.VolleyRequestFailed;

/**
 * Created by ashfire on 1/15/15.
 */
public class OttoErrorListener implements Response.ErrorListener {
    public int RequestId;
    Bus _eventBus;
    public OttoErrorListener(Bus eventBus, int requestId) {
        RequestId = requestId;
        _eventBus = eventBus;
    }
    public void onErrorResponse(VolleyError error) {
        VolleyRequestFailed message = new VolleyRequestFailed(RequestId, error);
        _eventBus.post(message);
    }
}