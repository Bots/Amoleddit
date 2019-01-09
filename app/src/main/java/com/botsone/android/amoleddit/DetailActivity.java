package com.botsone.android.amoleddit;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class DetailActivity extends AppCompatActivity{

    private static final String TAG = DetailActivity.class.getName();
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "tempImage";
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final Intent gotIntent = getIntent();
        final String value = gotIntent.getStringExtra("key");
        final String title = gotIntent.getStringExtra("title");
        final Uri parsedUri = Uri.parse(value);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);

        final String destinationFileName = SAMPLE_CROPPED_IMAGE_NAME + ".png";
        final Uri destinationUri = Uri.fromFile(new File(getCacheDir(), destinationFileName));

        final FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        final com.getbase.floatingactionbutton.FloatingActionButton actionA = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_a);
        final com.getbase.floatingactionbutton.FloatingActionButton actionB = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_b);
        final com.getbase.floatingactionbutton.FloatingActionButton actionC = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_c);


        final SimpleDraweeView draweeView = (SimpleDraweeView) findViewById(R.id.detail_view);
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
                if (ContextCompat.checkSelfPermission(DetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    // Request for permission
                    ActivityCompat.requestPermissions(DetailActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                }else {
                    Bitmap bitmap = draweeView.getDrawingCache();
                    saveImage(bitmap);
                    menuMultipleActions.collapse();
                }
            }

        });

        actionC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = Bitmap.createBitmap(draweeView.getDrawingCache());

                File f = saveBitmapToFile(bitmap);

                try {
                    final Uri newUri = convertFileToContentUri(DetailActivity.this, f);

                    Intent intentShareFile = new Intent(Intent.ACTION_SEND);

                    if (f.exists()) {
                        intentShareFile.setType("image/*");
                        intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intentShareFile.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        intentShareFile.putExtra(Intent.EXTRA_STREAM, newUri);

                        intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                                "Sharing File From Amoleddit...");
                        intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File From Amoleddit...");

                        startActivity(Intent.createChooser(intentShareFile, "Share File"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                menuMultipleActions.collapse();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected static Uri convertFileToContentUri(Context context, File file) throws Exception {

        //Uri localImageUri = Uri.fromFile(localImageFile); // Not suitable as it's not a content Uri

        ContentResolver cr = context.getContentResolver();
        String imagePath = file.getAbsolutePath();
        String imageName = null;
        String imageDescription = null;
        String uriString = MediaStore.Images.Media.insertImage(cr, imagePath, imageName, imageDescription);
        return Uri.parse(uriString);
    }

    private File saveBitmapToFile(Bitmap bitmap1) {

        //save bitmap to file f
        File cacheDir = getBaseContext().getCacheDir();
        final File f = new File(cacheDir, "pic.jpg");

        try {
            FileOutputStream out = new FileOutputStream(
                    f);
            bitmap1.compress(
                    Bitmap.CompressFormat.JPEG,
                    100, out);
            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
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
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ())
            file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(this, "File saved to /sdcard/Downloads", Toast.LENGTH_SHORT).show();
            DownloadManager downloadManager = (DownloadManager)this.getSystemService(this.DOWNLOAD_SERVICE);
            downloadManager.addCompletedDownload(file.getName(), file.getName(), true, "image/*", file.getAbsolutePath(),file.length(),true);
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
