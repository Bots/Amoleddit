package com.botsone.android.amoleddit;

import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.botsone.android.amoleddit.ArticleActivity.LOG_TAG;

/**
 * Created by bots on 2/18/18.
 */

public final class QueryUtils {

    private QueryUtils() {
    }

    /**
     * Return a list of {@link Article} objects that has been built up from
     * parsing a JSON response.
     */

    private static List<Article> extractFeatureFromJson(String articleJson) {

        //if the json string is empty or null then return early
        if (TextUtils.isEmpty(articleJson)) {
            return null;
        }

        // Create an empty ArrayList to add articles to
        List<Article> articles = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(articleJson);

            // Get the data object
            JSONObject data = baseJsonResponse.getJSONObject("data");

            // Get the children array
            JSONArray children = data.getJSONArray("children");

            for (int i = 1; i < children.length(); i++) {

                // Get the current article
                JSONObject currentArticle = children.getJSONObject(i);

                // Get the data for the current object
                JSONObject currentData = currentArticle.getJSONObject("data");

                // Get title
                String title = currentData.getString("title");

                String resolution;
                // Get the resolution if available, otherwise set it to not available
                if (title.contains("[")) {
                    resolution = StringUtils.substringBetween(title, "[", "]");
                    // remove res from title
                    title = title.replaceAll("\\[.*\\]","");
                } else if (title.contains("(")) {
                    resolution = StringUtils.substringBetween(title, "(", ")");
                    // remove res from title
                    title = title.replaceAll("\\(.*\\)","");
                }
                else {
                    resolution = "Not available";
                }


                // Get url
                String url = currentData.getString("url");

                // Get userName
                String userName = currentData.getString("author");

                // Get preview object
                if(currentData.has("preview")) {
                    JSONObject previewObject = currentData.getJSONObject("preview");

                    // Get images array
                    JSONArray imagesArray = previewObject.getJSONArray("images");

                    //Get zero object
                    JSONObject zeroObject = imagesArray.getJSONObject(0);

                    // Get resolutions array
                    JSONArray resolutionsArray = zeroObject.getJSONArray("resolutions");

                    // Get object X

                    if (resolutionsArray.length() >= 6) {
                        JSONObject number = resolutionsArray.getJSONObject(5);
                        String imageUrl = number.getString("url");
                        Article article = new Article(imageUrl, title, url, userName, resolution);
                        articles.add(article);

                    } if (resolutionsArray.length() == 5) {
                        JSONObject number = resolutionsArray.getJSONObject(4);
                        String imageUrl = number.getString("url");
                        Article article = new Article(imageUrl, title, url, userName, resolution);
                        articles.add(article);

                    } if (resolutionsArray.length() == 4) {
                        JSONObject number = resolutionsArray.getJSONObject(3);
                        String imageUrl = number.getString("url");
                        Article article = new Article(imageUrl, title, url, userName, resolution);
                        articles.add(article);

                    } if (resolutionsArray.length() == 3) {
                        JSONObject number = resolutionsArray.getJSONObject(2);
                        String imageUrl = number.getString("url");
                        Article article = new Article(imageUrl, title, url, userName, resolution);
                        articles.add(article);

                    } if (resolutionsArray.length() == 2) {
                        JSONObject number = resolutionsArray.getJSONObject(1);
                        String imageUrl = number.getString("url");
                        Article article = new Article(imageUrl, title, url, userName, resolution);
                        articles.add(article);

                    } if (resolutionsArray.length() == 1) {
                        JSONObject number = resolutionsArray.getJSONObject(0);
                        String imageUrl = number.getString("url");
                        Article article = new Article(imageUrl, title, url, userName, resolution);
                        articles.add(article);

                    }
                }
            }

        } catch (JSONException e) {

            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the article JSON results", e);
        }
        return articles;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL", e);
        }
        return url;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "ERROR Response Code" + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the article JSON results.", e);
        } finally {
            if (urlConnection == null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Query the Guardian dataset and return a list of {@link Article} objects.
     */
    public static List<Article> fetchArticleData(String requestUrl) {

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the http request", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Article}s
        List<Article> articles = extractFeatureFromJson(jsonResponse);

        // Return the list of {@link Article}s
        return articles;
    }

}
