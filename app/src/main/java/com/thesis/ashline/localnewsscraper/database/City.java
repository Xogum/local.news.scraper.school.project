package com.thesis.ashline.localnewsscraper.database;

/**
 * Created by ashfire on 3/9/15.
 */
public class City {

    public int id;
    public String county, country, name;

    public City(){}

    @Override
    public String toString() {
        return "City [id=" + id
            + ",name=" + name
            + ",county=" + county
            + ",country=" + country + "]";
    }
}
