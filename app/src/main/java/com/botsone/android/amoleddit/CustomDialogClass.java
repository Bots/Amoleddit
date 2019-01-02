package com.botsone.android.amoleddit;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class CustomDialogClass extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button wallpaper, lockscreen_wallpaper, both_wallpapers;

    public CustomDialogClass(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog);
        wallpaper = (Button) findViewById(R.id.btn_wallpaper);
        lockscreen_wallpaper = (Button) findViewById(R.id.btn_lockscreen_wallpaper);
        both_wallpapers = (Button) findViewById(R.id.btn_both_wallpapers);
        wallpaper.setOnClickListener(this);
        lockscreen_wallpaper.setOnClickListener(this);
        both_wallpapers.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_wallpaper:
                dismiss();
                break;
            case R.id.btn_lockscreen_wallpaper:
                dismiss();
                break;
            case R.id.btn_both_wallpapers:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}
