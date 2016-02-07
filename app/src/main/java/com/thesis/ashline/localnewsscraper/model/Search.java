package com.thesis.ashline.localnewsscraper.model;

import java.util.HashMap;
import java.util.Map;

public class Search {
/**    possible params
  *  long user_id;
  *  long category_id;
  *  //todo int place_id
  *  String date_to;
  *  String latitude;
  *  String longitude;
  *  String date_from;
 */
    HashMap<String, Object> params;

    public Search() {
        this.params = new HashMap<>();
    }

    public String getQueryString() {
        String url = "?";

        for (Map.Entry<String, ?> entry : params.entrySet()) {
            Object value = entry.getValue();
            url += entry.getKey() + "=" + String.valueOf(value) + "&";
        }

        return url;
    }

    public Object put(String key, Object value) {
        return params.put(key, value);
    }

}

