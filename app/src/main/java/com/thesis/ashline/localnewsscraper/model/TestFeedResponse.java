package com.thesis.ashline.localnewsscraper.model;

import java.util.ArrayList;

/**
 * Created by ashfire on 1/20/15.
 */

public class TestFeedResponse {
    public ArrayList<FeedItem> feed;

    public class FeedItem {
        public long id;
        public String name, status, image, profilePic, timeStamp, url;
    }
}
