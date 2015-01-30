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
    private static String baseUrl = "http://thesisapi-afromwana.rhcloud.com/";

    private static String usersUrl = baseUrl + "users";
    private static String userUrl = baseUrl + "users/%d";
    private static String userActionUrl = baseUrl + "users/%d/%s";

    private static String articlesUrl = baseUrl + "articles";
    private static String articleUrl = baseUrl + "articles/%d";
    private static String articleActionUrl = baseUrl + "articles/%d/%s";
    private static String articleUserUrl = baseUrl + "articles/%d/%s/%d";

    public static String likesAction = "likes";
    public static String favouritesAction = "favourites";
    public static String sharesAction = "shares";
    public static String savesAction = "saves";
    public static String readsAction = "reads";
    public static String verifyAction = "verify";

    public static String testUrl = "http://api.androidhive.info/feed/feed.json";

    /**
     * utility method for extracting fields and valuse from object by reflection
     *
     * @param object item to be extracted
     * @return map of extracted fields with corresponding values
     */
    private static Map<String, String> makeParameterMap(Object object) {
        Map<String, String> params = new HashMap<String, String>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                params.put(field.getName(), field.get(object).toString());
            } catch (IllegalAccessException e) {
            }
        }
        return params;
    }

    /**
     * method for sending a POST request to "/users"
     *
     * @param user
     * @return request object
     */
    public static OttoGsonRequest<User> postUser(final User user) {
        return new OttoGsonRequest<User>(ServiceLocator.EventBus, Request.Method.POST, usersUrl, User.class) {
            @Override
            protected Map<String, String> getParams() {
                return makeParameterMap(user);
            }
        };
    }
    /**
     * send a PUT request to "/users/{uid}" to edit user
     *
     * @param user
     * @return request object
     */
    public static OttoGsonRequest<User> putUser(final User user) {
        return new OttoGsonRequest<User>(ServiceLocator.EventBus,
                Request.Method.PUT,
                String.format(userUrl, user.id),
                User.class) {
            @Override
            protected Map<String, String> getParams() {
                return makeParameterMap(user);
            }
        };
    }

    /**
     * send a POST request to "/users/verify" to verify user
     *
     * @param user
     * @return request object
     */
    public static OttoGsonRequest<User> postUserVerify(final User user) {
        return new OttoGsonRequest<User>(ServiceLocator.EventBus,
                Request.Method.POST,
                String.format(userActionUrl, user.id, verifyAction),
                User.class) {
            @Override
            protected Map<String, String> getParams() {
                return makeParameterMap(user);
            }
        };
    }
    /**
     * send a GET request to "/users/{uid}/{action}" to get articles by action for user
     *
     * @param userId
     * @param action
     * @return request object
     */
    public static OttoGsonRequest<ArticleListResponse> getUserAction(long userId, String action) {
        return new OttoGsonRequest<ArticleListResponse>(ServiceLocator.EventBus,
                Request.Method.GET,
                String.format(userActionUrl, userId, action),
                ArticleListResponse.class);
    }
    /**todo likely useless
     * send a GET request to "/users/{id}" to get user info
     *
     * @param user
     * @return request object
     */
    public static OttoGsonRequest<User> getUser(final User user) {
        return new OttoGsonRequest<User>(ServiceLocator.EventBus,
                Request.Method.GET,
                String.format(userUrl, user.id),
                User.class);
    }
    /**todo make some checks so that user cant delete other users
     * send a DELETE request to "/users/{id}" to delete
     *
     * @param user
     * @return request object
     */
    public static OttoGsonRequest<User> deleteUser(final User user) {
        return new OttoGsonRequest<User>(ServiceLocator.EventBus,
                Request.Method.GET,
                String.format(userUrl, user.id),
                User.class);
    }

    /**
     * send a GET request to "/articles"
     * search
     *
     * @param search
     * @return request object
     */
    public static OttoGsonRequest<ArticleListResponse> getArticles(final Search search) {
        return new OttoGsonRequest<ArticleListResponse>(ServiceLocator.EventBus,
                Request.Method.GET,
                articlesUrl,
                ArticleListResponse.class) {
            @Override
            protected Map<String, String> getParams() {
                return makeParameterMap(search);
            }
        };
    }
    /**todo this route might not be needed if i render the article in a webview
     * send a GET request to "/articles/aid"
     * search
     *
     * @param article
     * @return request object
     */
    public static OttoGsonRequest<ActionResponse> getArticle(final Article article) {
        return new OttoGsonRequest<ActionResponse>(ServiceLocator.EventBus,
                Request.Method.GET,
                String.format(articleUrl, article.id),
                ActionResponse.class);
    }
    /**
     * send a POST request to "/articles/{aid}/{action}/{uid}"
     * search
     *
     * @param articleId
     * @param action
     * @return request object
     */
    public static OttoGsonRequest<ActionResponse> postArticleAction(long articleId, String action, long userId ) {
        return new OttoGsonRequest<ActionResponse>(ServiceLocator.EventBus,
                Request.Method.POST,
                String.format(articleUserUrl, articleId, action, userId),
                ActionResponse.class);
    }
    /**
     * send a DELETE request to "/articles/{aid}/{action}"
     * search
     *
     * @param articleId
     * @param action
     * @return request object
     */
    public static OttoGsonRequest<ActionResponse> deleteArticleAction(long articleId, String action, long userId ) {
        return new OttoGsonRequest<ActionResponse>(ServiceLocator.EventBus,
                Request.Method.DELETE,
                String.format(articleUserUrl, articleId, action, userId),
                ActionResponse.class);
    }
    /**
     * send a GET request to test url
     * search
     *
     * @return request object
     */
    public static OttoGsonRequest<TestFeedResponse> getTestFeed() {
        return new OttoGsonRequest<TestFeedResponse>(ServiceLocator.EventBus,
                Request.Method.GET,
                testUrl,
                TestFeedResponse.class);
    }
}
