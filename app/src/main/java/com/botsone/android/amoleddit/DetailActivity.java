package com.botsone.android.amoleddit;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getName();
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "tempImage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final Intent gotIntent = getIntent();
        final String value = gotIntent.getStringExtra("key");
        final String title = gotIntent.getStringExtra("title");
        final String commentsUri = "https://www.reddit.com" + gotIntent.getStringExtra("permalink");
        final Uri parsedUri = Uri.parse(value);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);

        final String destinationFileName = SAMPLE_CROPPED_IMAGE_NAME + ".png";
        final Uri destinationUri = Uri.fromFile(new File(getCacheDir(), destinationFileName));

        final FloatingActionsMenu menuMultipleActions = findViewById(R.id.multiple_actions);
        final com.getbase.floatingactionbutton.FloatingActionButton actionA = findViewById(R.id.action_a);
        final com.getbase.floatingactionbutton.FloatingActionButton actionB = findViewById(R.id.action_b);
        final com.getbase.floatingactionbutton.FloatingActionButton actionC = findViewById(R.id.action_c);
        final com.getbase.floatingactionbutton.FloatingActionButton actionD = findViewById(R.id.action_d);

        final SimpleDraweeView draweeView = findViewById(R.id.detail_view);
        draweeView.setDrawingCacheEnabled(true);
        draweeView.buildDrawingCache();
        draweeView.setImageURI(value);

        actionA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the crop activity
                UCrop.of(parsedUri, destinationUri)
                        .start(DetailActivity.this);

            }
        });

        actionB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context context = DetailActivity.this;
                Permissions.check(context, Manifest.permission.WRITE_EXTERNAL_STORAGE, "Storage access is needed to save image", new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        Bitmap bitmap = draweeView.getDrawingCache();
                        saveImage(bitmap);
                        menuMultipleActions.collapse();
                    }

                    @Override
                    public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                        super.onDenied(context, deniedPermissions);
                        Toast.makeText(DetailActivity.this, "Storage access is needed to save image", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });

        actionC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context context = DetailActivity.this;
                Permissions.check(context, Manifest.permission.WRITE_EXTERNAL_STORAGE, "Storage access is needed to temporarily store the image so that it can be shared", new PermissionHandler() {
                    @Override
                    public void onGranted() {
                        File cacheDir = getBaseContext().getCacheDir();
                        File f = new File(cacheDir, "pic");
                        FileInputStream fis = null;
                        Uri uri = null;
                        try {
                            fis = new FileInputStream(f);
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Bitmap bitmap = BitmapFactory.decodeStream(fis);
                        if (bitmap != null) {
                            uri = getImageUri(DetailActivity.this, bitmap);
                        } else {
                            Toast.makeText(DetailActivity.this, getString(R.string.could_not_complete_action), Toast.LENGTH_SHORT).show();
                            menuMultipleActions.collapse();
                        }
                        // Share bitmap
                        if (uri != null) {
                            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, "Sharing wallpaper from Amoleddit");
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.setType("image/*");

                            startActivity(intent);
                            menuMultipleActions.collapse();
                        } else {
                            Toast.makeText(DetailActivity.this, getString(R.string.could_not_complete_action), Toast.LENGTH_SHORT).show();
                            menuMultipleActions.collapse();
                        }
                    }

                    @Override
                    public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                        super.onDenied(context, deniedPermissions);
                        Toast.makeText(DetailActivity.this, getString(R.string.storage_error), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        actionD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(commentsUri));
                startActivity(i);
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        //Boolean isSDSupportedDevice = Environment.isExternalStorageRemovable();
        String path = null;

        if (isSDPresent) {
            // yes SD-card is present
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);


        }
        if (path != null) {
            return Uri.parse(path);
        } else {
            Toast.makeText(this, "Sorry external storage is needed to complete this action", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private void saveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File myDir = new File(root);
        if (!myDir.exists()) {
            boolean result = myDir.mkdirs();
            Log.d("MyActivity", "mkdirs: " + result);
        }
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(this, "File saved to /sdcard/Downloads", Toast.LENGTH_SHORT).show();
            DownloadManager downloadManager = (DownloadManager) this.getSystemService(DOWNLOAD_SERVICE);
            downloadManager.addCompletedDownload(file.getName(), file.getName(), true, "image/*", file.getAbsolutePath(), file.length(), true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent mediaScanIntent = new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file); //out is your output file
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
            } else {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://" + Environment.getExternalStorageDirectory())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            showDialog(resultUri);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                cropError.printStackTrace();
            }
        }
    }

    private void showDialog(Uri resultUri) {
        DialogActivity cdd = new DialogActivity(this, resultUri);
        cdd.show();
    }

    public class DialogActivity extends Dialog implements
            android.view.View.OnClickListener {
        private Activity c;
        private Uri resultUri;
        private Button regularWallpaper, lockscreenWallpaper, bothWallpapers;

        private DialogActivity(Activity a, Uri resultUri) {
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
            regularWallpaper = findViewById(R.id.btn_wallpaper);
            lockscreenWallpaper = findViewById(R.id.btn_lockscreen_wallpaper);
            bothWallpapers = findViewById(R.id.btn_both_wallpapers);
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
                            Toast.makeText(DetailActivity.this, "Not available on this version of Android. Try setting both wallpapers at the same time.", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(DetailActivity.this, "Not available on this version of Android. Try setting both wallpapers at the same time.", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(DetailActivity.this, "Wallpaper Set.", Toast.LENGTH_SHORT).show();
                        c.finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            dismiss();
        }
    }
}
