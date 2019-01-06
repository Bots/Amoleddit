package com.botsone.android.amoleddit;

import android.app.Activity;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getName();
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "tempImage";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    Uri uri3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final Context context = DetailActivity.this;
        final String destinationFileName = SAMPLE_CROPPED_IMAGE_NAME + ".png";
        final Uri destinationUri = Uri.fromFile(new File(getCacheDir(), destinationFileName));

        final Intent gotIntent = getIntent();
        final String value = gotIntent.getStringExtra("key");
        final Uri parsedUri = Uri.parse(value);

        final com.getbase.floatingactionbutton.FloatingActionButton actionA = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_a);

        final SimpleDraweeView draweeView = (SimpleDraweeView) findViewById(R.id.detail_view);
        draweeView.setDrawingCacheEnabled(true);
        draweeView.buildDrawingCache();
        draweeView.setImageURI(value);

        FloatingActionsMenu fam = (FloatingActionsMenu) findViewById(R.id.multiple_actions);

        actionA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Start the crop activity
                UCrop.of(parsedUri, destinationUri)
                        .start(DetailActivity.this);

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            showDialog(resultUri);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }

    private void showDialog(Uri resultUri) {
        DialogActivity cdd = new DialogActivity(this, resultUri);
        cdd.show();
    }

    public class DialogActivity extends Dialog implements
            android.view.View.OnClickListener {

        public Activity c;
        private Uri resultUri;
        public Dialog d;
        public Button regularWallpaper, lockscreenWallpaper, bothWallpapers;

        public DialogActivity(Activity a, Uri resultUri) {
            super(a);
            // TODO Auto-generated constructor stub
            this.c = a;
            this.resultUri = resultUri;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog);
            regularWallpaper = (Button) findViewById(R.id.btn_wallpaper);
            lockscreenWallpaper = (Button) findViewById(R.id.btn_lockscreen_wallpaper);
            bothWallpapers = (Button) findViewById(R.id.btn_both_wallpapers);
            regularWallpaper.setOnClickListener(this);
            lockscreenWallpaper.setOnClickListener(this);
            bothWallpapers.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_wallpaper:
                    WallpaperManager wm = WallpaperManager.getInstance(DetailActivity.this);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(DetailActivity.this.getContentResolver(), resultUri);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            wm.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                            Toast.makeText(DetailActivity.this, "Wallpaper Set.", Toast.LENGTH_SHORT).show();
                            c.finish();
                        } else {
                            Toast.makeText(DetailActivity.this, "Not available on this version of Android.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    c.finish();
                    break;

                case R.id.btn_lockscreen_wallpaper:
                    WallpaperManager wm2 = WallpaperManager.getInstance(DetailActivity.this);
                    try {
                        Bitmap bitmap2 = MediaStore.Images.Media.getBitmap(DetailActivity.this.getContentResolver(), resultUri);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            wm2.setBitmap(bitmap2, null, true, WallpaperManager.FLAG_LOCK);
                            Toast.makeText(DetailActivity.this, "Wallpaper Set.", Toast.LENGTH_SHORT).show();
                            c.finish();
                        } else {
                            Toast.makeText(DetailActivity.this, "Not available on this version of Android.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case R.id.btn_both_wallpapers:
                    WallpaperManager wm3 = WallpaperManager.getInstance(DetailActivity.this);

                    try {
                        Bitmap bitmap3 = MediaStore.Images.Media.getBitmap(DetailActivity.this.getContentResolver(), resultUri);
                        wm3.setBitmap(bitmap3);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            dismiss();
        }
    }

}
