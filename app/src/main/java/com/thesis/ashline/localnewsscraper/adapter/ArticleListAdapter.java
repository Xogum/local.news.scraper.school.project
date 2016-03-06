package com.thesis.ashline.localnewsscraper.adapter;

/**
 * Created by ashfire on 1/17/15.
 */

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.thesis.ashline.localnewsscraper.R;
import com.thesis.ashline.localnewsscraper.api.AppController;
import com.thesis.ashline.localnewsscraper.model.Article;
import com.thesis.ashline.localnewsscraper.view.FeedImageView;

public class ArticleListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<Article> articles;
    private Context applicationContext;
    ImageLoader imageLoader;

    public ArticleListAdapter(Activity activity, List<Article> articles) {
        this.activity = activity;
        this.applicationContext = activity.getApplicationContext();
        this.articles = articles;
        this.imageLoader = AppController.getInstance(this.applicationContext).getImageLoader();
    }

    @Override
    public int getCount() {
        return articles.size();
    }

    @Override
    public Object getItem(int location) {
        return articles.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.feed_item, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance(applicationContext).getImageLoader();

        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView timestamp = (TextView) convertView
                .findViewById(R.id.timestamp);
        TextView statusMsg = (TextView) convertView
                .findViewById(R.id.txtStatusMsg);
        TextView url = (TextView) convertView.findViewById(R.id.txtUrl);
        TextView id = (TextView) convertView.findViewById(R.id.txtId);

        TextView likeCount = (TextView) convertView.findViewById(R.id.likeCount);
        TextView readCount = (TextView) convertView.findViewById(R.id.readCount);
        TextView saveCount = (TextView) convertView.findViewById(R.id.saveCount);

        NetworkImageView profilePic = (NetworkImageView) convertView
                .findViewById(R.id.profilePic);
        FeedImageView feedImageView = (FeedImageView) convertView
                .findViewById(R.id.feedImage1);

        Article item = articles.get(position);

        name.setText(item.title);

        id.setText(String.valueOf(item.id));

        // date
        // Converting timestamp into x ago format
//        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
//                (long)(Float.parseFloat(item.date)*1000),
//                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        timestamp.setText(item.date);

        // action counts
        likeCount.setText(String.valueOf(item.likes));
        saveCount.setText(String.valueOf(item.saves));
        readCount.setText(String.valueOf(item.readz));

        // Check for empty summary
        if (!TextUtils.isEmpty(item.summary)) {
            statusMsg.setText(item.summary);
            statusMsg.setVisibility(View.VISIBLE);
        } else {
            // status is empty, remove from view
            statusMsg.setVisibility(View.GONE);
        }

        // Checking for null feed link
        if (item.link != null) {
            url.setText(Html.fromHtml("<a href=\"" + item.link + "\">"
                    + item.link + "</a> "));

            // Making link clickable
            url.setMovementMethod(LinkMovementMethod.getInstance());
            url.setVisibility(View.VISIBLE);
        } else {
            // link is null, remove from the view
            url.setVisibility(View.GONE);
        }

        // user profile pic
        profilePic.setImageUrl(item.icon_url, imageLoader);

        // Feed image
        if (item.image_link != null) {
            feedImageView.setImageUrl(item.image_link, imageLoader);
            feedImageView.setVisibility(View.VISIBLE);
            feedImageView
                    .setResponseObserver(new FeedImageView.ResponseObserver() {
                        @Override
                        public void onError() {
                        }

                        @Override
                        public void onSuccess() {
                        }
                    });
        } else {
            feedImageView.setVisibility(View.GONE);
        }

        return convertView;
    }

}

