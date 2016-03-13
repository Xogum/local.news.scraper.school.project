package com.thesis.ashline.localnewsscraper.model;

import java.util.HashMap;
import java.util.Map;

public class Search {
/**    possible params
 *  long user_id;
 *  long category_id;
 *  String date_to;
 *  String date_from;
 *  String longitude;
 *  String latitude;
 */
    HashMap<String, Object> params;
    HashMap<String, Boolean> sortParams;

    public Search() {
        this.params = new HashMap<>();
        this.sortParams = new HashMap<>();
    }

    public String getQueryString() {
        String url = "?";
        String sortUrlSegment = "";

        for (Map.Entry<String, ?> entry : params.entrySet()) {
            Object value = entry.getValue();
            url += entry.getKey() + "=" + String.valueOf(value) + "&";
        }

        if(!sortParams.isEmpty()) {
            for(Map.Entry<String, Boolean> entry : sortParams.entrySet()) {
                sortUrlSegment += "," + entry.getKey();
                if(entry.getValue()) {
                    sortUrlSegment += "_desc";
                }
            }

            sortUrlSegment = sortUrlSegment.substring(1);
            url += "sort=" + sortUrlSegment;
        }

        return url;
    }

    public Object put(String key, Object value) {
        return params.put(key, value);
    }
    /**
     * @key - sort field
     * @value - descending or not
     */
    public Object putSortParameter(String key, Boolean value) {
        return sortParams.put(key, value);
    }

    public String getPaginationUrlSegment() {
        String url;
        String offset = String.valueOf(params.get("offset"));
        String limit = String.valueOf(params.get("limit"));

        url = "?offset=" + offset + "&limit=" + limit;

        return url;
    }
}

