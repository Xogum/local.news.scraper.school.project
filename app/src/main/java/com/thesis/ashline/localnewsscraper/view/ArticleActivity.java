package com.thesis.ashline.localnewsscraper.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.thesis.ashline.localnewsscraper.R;
import com.thesis.ashline.localnewsscraper.api.RouteMaker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticleActivity extends ActionBarActivity {
    private WebView webview;
    private String URL;
    private long articleId;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            URL = b.getString("article_url");
            articleId = Long.parseLong(b.getString("article_id"));
        }
        webview = new WebView(this);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setUserAgentString("Mozilla/5.0 (Linux; U; Android 2.2; en-us; " +
                "Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) " +
                "Version/4.0 Mobile Safari/533.1");

        final Activity activity = this;
        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                activity.setProgress(progress * 100);
            }
        });
        webview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }

        });

        loadWebviewContent();
        setContentView(webview);

    }

    private void loadWebviewContent() {
        if (URL != null && URL != "") {
            webview.loadUrl(URL);
        } else {
            String summary = "<html><body>error displaying page<hr/> Ooops...</body></html>";
            webview.loadData(summary, "text/html", null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_article, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, URL);
        shareIntent.setType("text/plain");
        setShareIntent(shareIntent);


        return true;
    }
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, URL);
                shareIntent.setType("text/plain");
                setShareIntent(shareIntent);
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_with)));

                break;
            case R.id.action_save:
                handleActionSelected(item, "saves");

                return true;
            case R.id.action_favourite:
                handleActionSelected(item, "favourites");
                return true;
            case R.id.action_like:
                handleActionSelected(item, "likes");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleActionSelected(MenuItem item, String action) {
        if (item.isChecked()) {
            item.setChecked(false);
            RouteMaker.deleteArticleAction(articleId, action, 0);
            Toast.makeText(this, "article removed from " + action, Toast.LENGTH_SHORT).show();

        } else {
            item.setChecked(true);
            RouteMaker.postArticleAction(articleId, action, 0);
            Toast.makeText(this,
                    String.format(getResources().getString(R.string.added_action),
                            action),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
