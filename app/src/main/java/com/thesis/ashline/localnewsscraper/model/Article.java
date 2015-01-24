package com.thesis.ashline.localnewsscraper.model;

/**
 * Created by ashfire on 1/16/15.
 */
public class Article {
    public long id;
    public String date;
    public String title;
    public String url;
    public String summary; //TODO add to backend
    public long category_id;
    public int reads;
    public int likes;
    public int shares;
    public int favourites;
    public String latitude;
    public String longitude;
    public String image_url;
    public String icon_url;//TODO add to backend
}
