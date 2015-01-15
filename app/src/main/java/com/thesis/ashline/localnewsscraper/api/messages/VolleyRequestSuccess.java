package com.thesis.ashline.localnewsscraper.api.messages;

/**
 * Created by ashfire on 1/15/15.
 */

public class VolleyRequestSuccess<T> {
    public T response;
    public int requestId;
    public VolleyRequestSuccess(int requestId, T response) {
        this.requestId = requestId;
        this.response = response;
    }
}
