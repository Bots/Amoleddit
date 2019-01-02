package com.botsone.android.amoleddit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.facebook.drawee.view.SimpleDraweeView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by bots on 2/18/18.
 */

public class ArticleAdapter extends ArrayAdapter<Article> {

    public ArticleAdapter(Context context, List<Article> articles) {
        super(context, 0, articles);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;

        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.article_list_item, parent, false);
        }



        // Get article object at current position
        Article currentArticle = getItem(position);

        TextView titleText = (TextView) listItemView.findViewById(R.id.title_tv);
        TextView userText = (TextView) listItemView.findViewById(R.id.username_tv);

        // Display image for current article
        SimpleDraweeView draweeView = (SimpleDraweeView) listItemView.findViewById(R.id.image_view);
        draweeView.setImageURI(currentArticle.getImageUrl());

        //Display the title and username under the image
        titleText.setText(currentArticle.getTitle());
        userText.setText(currentArticle.getUserName());

        return listItemView;
    }

    private String formatDate(String date) {
        SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault());
        String formattedDate = "";

        try {
            Date formatDate = sdfSource.parse(date);
            formattedDate = formatDate.toString();
            return formattedDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
    }

}
