package com.mindjet.com.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mindjet on 2016/5/20.
 */
public class LyricActivity extends AppCompatActivity {

    private TextView song_name, artist, current_progress, whole_progress;
    private Intent intent;
    public static LrcView lrcView;
    private boolean isFirstTime = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lrc_layout);

        initUI();

    }

    private void initUI() {

        song_name = (TextView) findViewById(R.id.song_name);
        artist = (TextView) findViewById(R.id.artist);
        current_progress = (TextView) findViewById(R.id.current_progress);
        whole_progress = (TextView) findViewById(R.id.whole_progress);
        lrcView = (LrcView) findViewById(R.id.lrcShowView);

        intent = getIntent();
        song_name.setText(intent.getStringExtra("title"));
        artist.setText(intent.getStringExtra("artist"));
        whole_progress.setText(intent.getStringExtra("duration"));

    }

    @Override
    protected void onStart() {
        super.onStart();

        //第一次启动 LyricActivity 时，需要将此时传进来的 index 送给 LrcView
        //以后就不必了，因为在 PlayerService 判断 lrcView 存在，直接在那边传给 lrcView
//        if (isFirstTime){
//            isFirstTime = false;
            intent = getIntent();
            int index = intent.getIntExtra("index",0);
            lrcView.setIndex(index);
            lrcView.invalidate();
//        }

    }
}

