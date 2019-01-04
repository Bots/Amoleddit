package com.botsone.android.amoleddit;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.drawee.view.DraweeView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;

import java.io.File;
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

        draweeView.setDrawingCacheEnabled(true);
        draweeView.buildDrawingCache();
        draweeView.setImageURI(value);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        final Context context = DetailActivity.this;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Bitmap bitmap = draweeView.getDrawingCache();
                if (bitmap != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Set Wallpaper");
                    builder.setItems(new CharSequence[]
                                    {"Homescreen", "Lockscreen", "Both"},
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item
                                    switch (which) {
                                        case 0:
                                            WallpaperManager wm1 = WallpaperManager.getInstance(getApplicationContext());
                                            try {
                                                wm1.setBitmap(bitmap);
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                            Toast.makeText(context, R.string.wallpaperSet, Toast.LENGTH_SHORT).show();
                                            break;
                                        case 1:
                                            WallpaperManager wm2 = WallpaperManager.getInstance(getApplicationContext());
                                            try {
                                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                                                    // Do something for N and above versions
                                                    wm2.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                                                    Toast.makeText(context, R.string.wallpaperSet, Toast.LENGTH_SHORT).show();
                                                } else{
                                                    // do something for phones running an SDK before N
                                                    Toast.makeText(context, R.string.sorryMessage, Toast.LENGTH_SHORT).show();
                                                }
                                            }catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                            break;
                                        case 2:
                                            WallpaperManager wm3 = WallpaperManager.getInstance(getApplicationContext());
                                            try {
                                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                                                    // Do something for N and above versions
                                                    wm3.setBitmap(bitmap);
                                                    wm3.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                                                    Toast.makeText(context, R.string.wallpaperSet, Toast.LENGTH_SHORT).show();
                                                } else{
                                                    // do something for phones running an SDK before N
                                                    Toast.makeText(context, R.string.sorryMessage, Toast.LENGTH_SHORT).show();
                                                }
                                            }catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                            break;

                                    }
                                }
                            });
                    builder.create().show();

                }


            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public Uri getCurrentArticleUri() {
        Intent intent = getIntent();
        final String value = intent.getStringExtra("key");
        Uri uri = Uri.parse(value);
        return uri;
    }

}
