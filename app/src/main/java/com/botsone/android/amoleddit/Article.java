package com.botsone.android.amoleddit;

/**
 * Created by bots on 2/18/18.
 */

public class Article {

    private String mImageUrl;

    private String mTitle;

    private String mUrl;

    private String mUserName;

    public Article(String imageUrl, String title, String url, String userName) {

        mImageUrl = imageUrl;
        mTitle = title;
        mUrl = url;
        mUserName = userName;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getUserName() {
        return mUserName;
    }

}
