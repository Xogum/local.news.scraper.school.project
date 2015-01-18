package com.thesis.ashline.localnewsscraper.api;

/**
 * Created by ashfire on 1/17/15.
 */
import com.android.volley.Request;
import com.thesis.ashline.localnewsscraper.model.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class RouteMaker {
    private String baseUrl = "http://thesisapi-afromwana.rhcloud.com/";

    private String usersUrl = baseUrl + "users";
    private String userUrl = baseUrl + "users/%d";
    private String userActionUrl = baseUrl + "users/%d/%s";

    private String articlesUrl = baseUrl + "articles";
    private String articleUrl = baseUrl + "articles/%d";
    private String articleActionUrl = baseUrl + "articles/%d/%s";
    private String articleUserUrl = baseUrl + "articles/%d/%s/%d";

    /**
     * utility method for extracting fields and valuse from object by reflection
     * @param object item to be extracted
     * @return map of extracted fields with corresponding values
     */
    private Map<String, String> makeParameterMap(Object object){
        Map<String, String> params = new HashMap<String, String>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields){
            try {
                params.put(field.getName(), field.get(object).toString());
            }catch(IllegalAccessException e){}
        }
        return params;
    }

    /**
     * method for sending a POST request to "/users"
     * @param user
     * @return request object
     */
    public OttoGsonRequest<User> postUsers(final User user){
        return new OttoGsonRequest<User>(ServiceLocator.EventBus, Request.Method.POST, usersUrl, User.class){
            @Override
            protected Map<String, String> getParams(){
                return makeParameterMap(user);
            }
        };
    }
}
