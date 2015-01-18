package com.thesis.ashline.localnewsscraper.api;

import com.squareup.otto.Bus;
/**
 * Created by ashfire on 1/15/15.
 */
/** GsonRequest which passes the result on to an Otto Message Bus */
public class OttoGsonRequest<T> extends GsonRequest<T> {
    /** Request ID counter for this session */
    private static int _idCounter = 1;
    /** A ID for this request, unique for the lifetime of the process (given that you do < 2BN requests) */
    public int requestId;
    public OttoGsonRequest(Bus eventBus,int method, String url, Class<T> classType) {
        super(method,
                url,
                classType,
                new OttoSuccessListener<T>(eventBus, _idCounter),
                new OttoErrorListener(eventBus, _idCounter));
        requestId = _idCounter;
        _idCounter++;
    }
}