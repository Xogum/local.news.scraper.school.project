package com.thesis.ashline.localnewsscraper.api.messages;

import com.android.volley.VolleyError;
/**
 * Created by ashfire on 1/15/15.
 */

public class VolleyRequestFailed {
    public int requestId;
    public VolleyError error;
    public VolleyRequestFailed(int requestId, VolleyError error) {
        this.requestId = requestId;
        this.error = error;
    }
}