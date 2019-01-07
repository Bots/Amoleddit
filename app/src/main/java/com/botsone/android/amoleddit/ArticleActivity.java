package com.botsone.android.amoleddit;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bots on 2/18/18.
 */

public class ArticleActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Article>> {

    public static final String LOG_TAG = ArticleActivity.class.getName();

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;

    /**
     * Constant value for the article loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int ARTICLE_LOADER_ID = 0;

    /**
     * URL for article data from the guardian
     */
    private static final String REDDIT_REQUEST_URL =
            "https://www.reddit.com/r/amoledbackgrounds/hot/.json?limit=100&raw_json=1";

    /**
     * Adapter for the list of articles
     */
    private ArticleAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_activity);

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find a reference to the {@link ListView} in the layout
        GridView articleListView = (GridView) findViewById(R.id.grid);

        // Define empty textview for when no data is returned
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        articleListView.setEmptyView(mEmptyStateTextView);

        // Create a new adapter that takes an empty list of articles as input
        mAdapter = new ArticleAdapter(this, new ArrayList<Article>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        articleListView.setAdapter(mAdapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with the full article.
        articleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Find the current article that was clicked on
                Article currentArticle = mAdapter.getItem(i);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                //Uri articleUri = Uri.parse(currentArticle.getUrl());

                String parsedUri = currentArticle.getUrl();

                // Create a new intent to send user to detail activity
                Intent detailIntent = new Intent(ArticleActivity.this, DetailActivity.class);

                detailIntent.putExtra("key", parsedUri);

                // Send the intent to launch a new activity
                startActivity(detailIntent);

            }

        });
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection,fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the loaderManager to interact with loaders
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(ARTICLE_LOADER_ID, null, this);
        } else {
            // Otherwise show error
            // First hide the loading indicator so that the error message will be visible
            View loadingIndicator = (View) findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }

        Fresco.initialize(this);

    }

    @Override
    public Loader<List<Article>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        return new ArticleLoader(this, REDDIT_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articles) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = (View) findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No articles found."
        mEmptyStateTextView.setText(R.string.no_articles);

        // Clear the adapter of previous article data
        mAdapter.clear();

        // If there is a valid list of {@link Article}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (articles != null && !articles.isEmpty()) {
            mAdapter.addAll(articles);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

}
