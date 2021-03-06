package com.botsone.android.amoleddit;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bots on 2/18/18.
 */

public class ArticleActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Article>>, BillingProcessor.IBillingHandler {

    public static final String LOG_TAG = ArticleActivity.class.getName();
    private static final int ARTICLE_LOADER_ID = 0;
    private TextView mEmptyStateTextView;
    private String sorter = "hot";
    private String REDDIT_REQUEST_URL =
            "https://www.reddit.com/r/amoledbackgrounds/" + sorter + "/.json?limit=100&raw_json=1";
    private ArticleAdapter mAdapter;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private int check = 0;
    GridView articleListView;
    String donation1 = "one_dollar_donation";
    BillingProcessor bp;
    String parsedUri;
    String title;
    String permalink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_activity);

        mySwipeRefreshLayout = findViewById(R.id.swiperefresh);
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Spinner toolbarSpinner = findViewById(R.id.toolbar_spinner);
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this).build();
        Fresco.initialize(this, config);

        toolbarSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());

        // Find a reference to the {@link GridView} in the layout
        articleListView = findViewById(R.id.grid);

        // Define empty textview for when no data is returned
        mEmptyStateTextView = findViewById(R.id.empty_view);

        articleListView.setEmptyView(mEmptyStateTextView);

        // Create a new adapter that takes an empty list of articles as input
        mAdapter = new ArticleAdapter(this, new ArrayList<Article>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        articleListView.setAdapter(mAdapter);

        // Set an item click listener on the ListView, which sends an intent to the system
        // to open the detail view.
        articleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Find the current article that was clicked on
                Article currentArticle = mAdapter.getItem(i);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                //Uri articleUri = Uri.parse(currentArticle.getUrl());

                if (currentArticle != null) {
                    parsedUri = currentArticle.getImageUrl();
                    title = currentArticle.getTitle();
                    permalink = currentArticle.getPermalink();
                }

                // Create a new intent to send user to detail activity
                final Intent detailIntent = new Intent(ArticleActivity.this, DetailActivity.class);

                ImagePipeline imagePipeline = Fresco.getImagePipeline();
                Uri uri = null;
                if (currentArticle != null) {
                    uri = Uri.parse(currentArticle.getImageUrl());
                }

                ImageRequest request = ImageRequestBuilder
                        .newBuilderWithSource(uri).build();

                imagePipeline.prefetchToBitmapCache(request, this);

                DataSource<CloseableReference<CloseableImage>>
                        dataSource = imagePipeline.fetchDecodedImage(request, this);

                dataSource.subscribe(
                        new BaseBitmapDataSubscriber() {
                            @Override
                            public void onNewResultImpl(@Nullable Bitmap bitmap) {
                                // Pass bitmap to system, which makes a copy of the bitmap.
                                File cacheDir = getBaseContext().getCacheDir();
                                File f = new File(cacheDir, "pic");

                                try {
                                    FileOutputStream out = new FileOutputStream(
                                            f);
                                    if (bitmap != null) {
                                        bitmap.compress(
                                                Bitmap.CompressFormat.JPEG,
                                                100, out);
                                    }
                                    out.flush();
                                    out.close();

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                // No need to do any cleanup.
                            }

                            @Override
                            public void onFailureImpl(DataSource dataSource) {
                                // No cleanup required here.
                            }
                        }, CallerThreadExecutor.getInstance());


                detailIntent.putExtra("key", parsedUri);
                detailIntent.putExtra("title", title);
                detailIntent.putExtra("permalink", permalink);


                // Send the intent to launch a new activity
                startActivity(detailIntent);

            }

        });

        loadData();

        bp = new BillingProcessor(this, getString(R.string.google_key), this);
        bp.initialize();

        /*
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        myUpdateOperation();
                    }
                }
        );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_item:
                myUpdateOperation();
                break;
            case R.id.goto_reddit_item:
                String url = "http://www.reddit.com/r/amoledbackgrounds";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            case R.id.share_app:
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Amoleddit");
                    String shareMessage= "\nCheck out this wallpaper app called Amoleddit!\n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch(Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.donate:
                bp.consumePurchase(donation1);
                bp.purchase(this, donation1);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void myUpdateOperation() {
        getLoaderManager().destroyLoader(ARTICLE_LOADER_ID);
        getLoaderManager().initLoader(ARTICLE_LOADER_ID, null, this);
        mySwipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public Loader<List<Article>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        return new ArticleLoader(this, REDDIT_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> articles) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No articles found."
        mEmptyStateTextView.setText(R.string.no_articles);

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

    private class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            check++;
            if (check > 1) {
                switch (position) {
                    case 0:
                        Toast.makeText(ArticleActivity.this, "Sorted by Hot", Toast.LENGTH_SHORT).show();
                        sorter = "hot";
                        mAdapter.notifyDataSetChanged();
                        REDDIT_REQUEST_URL = "https://www.reddit.com/r/amoledbackgrounds/" + sorter + "/.json?limit=100&raw_json=1";
                        myUpdateOperation();
                        break;
                    case 1:
                        Toast.makeText(ArticleActivity.this, "Sorted by New", Toast.LENGTH_SHORT).show();
                        sorter = "new";
                        mAdapter.notifyDataSetChanged();
                        REDDIT_REQUEST_URL = "https://www.reddit.com/r/amoledbackgrounds/" + sorter + "/.json?limit=100&raw_json=1";
                        myUpdateOperation();
                        break;
                    case 2:
                        Toast.makeText(ArticleActivity.this, "Sorted by Controversial", Toast.LENGTH_SHORT).show();
                        sorter = "controversial";
                        mAdapter.notifyDataSetChanged();
                        REDDIT_REQUEST_URL = "https://www.reddit.com/r/amoledbackgrounds/" + sorter + "/.json?limit=100&raw_json=1";
                        myUpdateOperation();
                        break;
                    case 3:
                        Toast.makeText(ArticleActivity.this, "Sorted by Top", Toast.LENGTH_SHORT).show();
                        sorter = "top";
                        mAdapter.notifyDataSetChanged();
                        REDDIT_REQUEST_URL = "https://www.reddit.com/r/amoledbackgrounds/" + sorter + "/.json?limit=100&raw_json=1";
                        myUpdateOperation();
                        break;
                    case 4:
                        Toast.makeText(ArticleActivity.this, "Sorted by Rising", Toast.LENGTH_SHORT).show();
                        sorter = "rising";
                        mAdapter.notifyDataSetChanged();
                        REDDIT_REQUEST_URL = "https://www.reddit.com/r/amoledbackgrounds/" + sorter + "/.json?limit=100&raw_json=1";
                        myUpdateOperation();
                        break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private void loadData() {

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
            View loadingIndicator = findViewById(R.id.loading_indicator);

            loadingIndicator.setVisibility(View.GONE);

            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }

    }

    // IBillingHandler implementation

    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
    }

    @Override
    public void onProductPurchased(@NonNull String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        /*
         * Called when some error occurred. See Constants class for more details
         *
         * Note - this includes handling the case where the user canceled the buy dialog:
         * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
         */
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }
}
