package com.thesis.ashline.localnewsscraper.api;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.thesis.ashline.localnewsscraper.api.messages.VolleyRequestFailed;
import com.thesis.ashline.localnewsscraper.api.messages.VolleyRequestSuccess;

import java.util.ArrayList;

/**
 * Created by ashfire on 1/15/15.
 */

/**
 * Class supporting catching and saving an HTTP responses temporarily
 */
public class OttoGsonResponseBuffer {
    private Bus _eventBus;
    private ArrayList _messagesSaved = new ArrayList();
    public OttoGsonResponseBuffer(Bus eventBus) {
        _eventBus = eventBus;
    }
    /** Starts saving any incoming responses or errors until stopped */
    public void startSaving() {
        _eventBus.register(this);
    }
    /** Sends out buffer and stops storing new */
    public void stopAndProcess() {
        safeUnregister();
        for (Object message : _messagesSaved) {
            _eventBus.post(message);
        }
        _messagesSaved.clear();
    }
    /** Clears buffers and stops storing new*/
    public void stopAndPurge() {
        _eventBus.unregister(this);
        _messagesSaved.clear();
    }
    @Subscribe
    public void onHttpResponseReceived(VolleyRequestSuccess message) {
        _messagesSaved.add(message);
    }
    @Subscribe
    public void onHttpResponseReceived(VolleyRequestFailed message) {
        _messagesSaved.add(message);
    }
    private void safeUnregister() {
        try {
            _eventBus.unregister(this);
        }
        catch (Exception e) {
        }
    }
}