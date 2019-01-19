package com.botsone.android.amoleddit;

/**
 * Created by bots on 2/18/18.
 */

public class Article {

    private String mImageUrl;

    private String mTitle;

    private String mUrl;

    private String mUserName;

    private String mResolution;

    private String mPermalink;

    public Article(String imageUrl, String title, String url, String userName, String resolution, String permalink) {

        mImageUrl = imageUrl;
        mTitle = title;
        mUrl = url;
        mUserName = userName;
        mResolution = resolution;
        mPermalink = permalink;
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

    public String getResolution() {
        return mResolution;
    }

    public String getPermalink() {
        return mPermalink;
    }

}
