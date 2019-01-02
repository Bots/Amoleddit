package com.botsone.android.amoleddit;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.facebook.drawee.view.DraweeView;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.IOException;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final SimpleDraweeView draweeView = (SimpleDraweeView) findViewById(R.id.detail_view);

        Intent intent = getIntent();

        final String value = intent.getStringExtra("key");

        draweeView.setImageURI(value);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        final Context context = DetailActivity.this;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Show an alertDialog to offer choice between Home screen wallpaper,
                //Lock screen wallpaper, or both

                CustomDialogClass cdd=new CustomDialogClass(DetailActivity.this);
                cdd.show();

//                draweeView.setDrawingCacheEnabled(true);
//                draweeView.buildDrawingCache();
//                Bitmap bitmap = draweeView.getDrawingCache();

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
