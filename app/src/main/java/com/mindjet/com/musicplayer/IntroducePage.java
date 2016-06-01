package com.mindjet.com.musicplayer;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

/**
 * @author Mindjet
 * @date 2016/5/31
 */
public class IntroducePage extends AppCompatActivity{

    private Handler handler = new Handler();

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introduce);

        Window window = getWindow();

        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                startActivity(new Intent(IntroducePage.this,PlayerActivity.class));
                overridePendingTransition(R.anim.alpha_in,R.anim.alpha_out);
                finish();

            }
        },1500);

    }
}
