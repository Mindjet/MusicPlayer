package com.mindjet.com.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Mindjet
 * @date 2016.5.20
 * 显示歌词界面
 * 控制 上一首/下一首/播放/暂停/继续/播放模式/进度调整
 */


public class LyricActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView song_name, artist, current_progress, whole_progress;
    private Button pre, next, play, search, queue, repeat, shuffle;
    private Intent intent;
    public static LrcView lrcView;
    private boolean isFirstTimeStart = true;
    private SeekBar lrc_seekbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lrc_layout);

        initUI();

        //注册广播接收器
        LrcActivityReceiver receiver = new LrcActivityReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstant.ActionMsg.MUSIC_CURRENT);
        filter.addAction(AppConstant.ActionMsg.UPDATE_TITLE);
        registerReceiver(receiver,filter);

    }

    //实例化UI，添加监听器
    private void initUI() {

        song_name = (TextView) findViewById(R.id.song_name);
        artist = (TextView) findViewById(R.id.artist);
        current_progress = (TextView) findViewById(R.id.current_progress);
        whole_progress = (TextView) findViewById(R.id.whole_progress);
        lrcView = (LrcView) findViewById(R.id.lrcShowView);

        lrc_seekbar = (SeekBar) findViewById(R.id.lrc_seekBar);
        lrc_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            private int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    this.progress = progress;
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Intent intent = new Intent();
                intent.setAction("com.mindjet.media.MUSIC_SERVICE");
                intent.putExtra("progress", this.progress);
                intent.putExtra("MSG", AppConstant.PlayerMsg.PROGRESS_CHANGE);
                intent.setPackage("com.mindjet.com.musicplayer");
                startService(intent);
            }
        });

        pre = (Button) findViewById(R.id.lrc_previous);
        next = (Button) findViewById(R.id.lrc_next);
        play = (Button) findViewById(R.id.lrc_play);
        search = (Button) findViewById(R.id.lrc_search);
        queue = (Button) findViewById(R.id.lrc_queue);
        repeat = (Button) findViewById(R.id.lrc_repeat_music);
        shuffle = (Button) findViewById(R.id.lrc_shuffle_music);

        pre.setOnClickListener(this);
        next.setOnClickListener(this);
        play.setOnClickListener(this);
        search.setOnClickListener(this);
        queue.setOnClickListener(this);
        repeat.setOnClickListener(this);
        shuffle.setOnClickListener(this);


        intent = getIntent();
        song_name.setText(intent.getStringExtra("title"));
        artist.setText(intent.getStringExtra("artist"));
        whole_progress.setText(intent.getStringExtra("duration"));

    }

    //做一些初始化操作
    @Override
    protected void onStart() {
        super.onStart();

        //第一次启动 LyricActivity 时，需要将此时传进来的 index 送给 LrcView
        //以后就不必了，因为在 PlayerService 判断 lrcView 存在，直接在那边传给 lrcView
        if (isFirstTimeStart) {
            isFirstTimeStart = false;
            intent = getIntent();
            int index = intent.getIntExtra("index", 0);
            lrcView.setIndex(index);
            lrcView.invalidate();
        }

        //判断此时播放状态，确定 播放键 的状态 -- “播放”？“暂停”？“继续”？
        //TODO 后期将设计为图形按钮，通过 广播机制 来更新
        if (PlayerState.isFirstTime) play.setText("播放");
        if (PlayerState.isPlaying) play.setText("暂停");
        if (PlayerState.isPause) play.setText("继续");

    }


    //按钮点击事件
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.lrc_play:

                if (PlayerState.isFirstTime) {

                    PlayerState.isFirstTime = false;
                    PlayerState.isPlaying = true;
                    PlayerState.isPause = false;
                    play.setText("暂停");
                    Intent intent = new Intent();
                    intent.putExtra("url",PlayerState.mp3InfoList.get(PlayerState.music_position).url);
                    intent.setAction("com.mindjet.media.MUSIC_SERVICE");
                    intent.putExtra("MSG",AppConstant.PlayerMsg.PLAY_MSG);
                    intent.setPackage("com.mindjet.com.musicplayer");
                    startService(intent);

                }else if (PlayerState.isPlaying) {

                    PlayerState.isPlaying = false;
                    PlayerState.isPause = true;
                    play.setText("继续");
                    Intent intent = new Intent();
                    intent.setAction("com.mindjet.media.MUSIC_SERVICE");
                    intent.putExtra("MSG", AppConstant.PlayerMsg.PAUSE_MSG);
                    intent.setPackage("com.mindjet.com.musicplayer");
                    startService(intent);

                } else if (PlayerState.isPause) {

                    PlayerState.isPause = false;
                    PlayerState.isPlaying = true;
                    play.setText("暂停");
                    Intent intent = new Intent();
                    intent.setAction("com.mindjet.media.MUSIC_SERVICE");
                    intent.putExtra("MSG",AppConstant.PlayerMsg.CONTINUE_MSG);
                    intent.setPackage("com.mindjet.com.musicplayer");
                    startService(intent);

                }
                break;

            case R.id.lrc_next:
                if (PlayerState.music_position<=PlayerState.mp3InfoList.size()-2){

                    PlayerState.music_position++;
                    PlayerState.isFirstTime = false;
                    PlayerState.isPlaying = true;
                    PlayerState.isPause = false;

                    Intent intent = new Intent();
                    intent.setAction("com.mindjet.media.MUSIC_SERVICE");
                    intent.putExtra("url",PlayerState.mp3InfoList.get(PlayerState.music_position).url);
                    intent.putExtra("MSG",AppConstant.PlayerMsg.NEXT_MSG);
                    intent.setPackage("com.mindjet.com.musicplayer");
                    startService(intent);

                }else {

                    Toast.makeText(this, "没有下一首了", Toast.LENGTH_SHORT).show();

                }
                break;

            case R.id.lrc_previous:
                if (PlayerState.music_position>0){

                    PlayerState.music_position--;
                    PlayerState.isFirstTime=false;
                    PlayerState.isPlaying = true;
                    PlayerState.isPause = false;

                    Intent intent = new Intent();
                    intent.setAction("com.mindjet.media.MUSIC_SERVICE");
                    intent.putExtra("url",PlayerState.mp3InfoList.get(PlayerState.music_position).url);
                    intent.putExtra("MSG",AppConstant.PlayerMsg.PREVIOUS_MSG);
                    intent.setPackage("com.mindjet.com.musicplayer");
                    startService(intent);

                }else {

                    Toast.makeText(this, "没有上一首了", Toast.LENGTH_SHORT).show();

                }
                break;

            case R.id.lrc_repeat_music:
                //TODO 后期将该方法与 playeractivity的相同方法包装
                if (PlayerState.mode!=1){
                    PlayerState.mode=1;
                    repeat.setText("顺序");
                    Toast.makeText(LyricActivity.this, "当前播放模式：单曲循环", Toast.LENGTH_SHORT).show();
                }else if (PlayerState.mode==1){
                    PlayerState.mode=2;
                    repeat.setText("单曲");
                    Toast.makeText(LyricActivity.this, "当前播放模式：顺序播放", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.lrc_shuffle_music:
                //TODO 后期将该方法与 playeractivity的相同方法包装
                if (PlayerState.mode!=3){
                    PlayerState.mode=3;
                    shuffle.setText("顺序");
                    Toast.makeText(LyricActivity.this, "当前播放模式：随机播放", Toast.LENGTH_SHORT).show();
                }else if (PlayerState.mode==3){
                    PlayerState.mode=2;
                    shuffle.setText("随机");
                    Toast.makeText(LyricActivity.this, "当前播放模式：顺序播放", Toast.LENGTH_SHORT).show();
                }
                break;

        }

    }


    //广播接收器，接收来自 音乐服务的广播，来更新 曲目信息 和 播放进度
    class LrcActivityReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action.equals(AppConstant.ActionMsg.MUSIC_CURRENT)){

                int current = intent.getIntExtra("current",-1);
                current_progress.setText(MediaUtil.formatTime(current));
                whole_progress.setText(MediaUtil.formatTime(PlayerState.mp3InfoList.get(PlayerState.music_position).duration));
                lrc_seekbar.setProgress((int) (100*current/intent.getLongExtra("whole",-1)));

            }

            if (action.equals(AppConstant.ActionMsg.UPDATE_TITLE)){

                song_name.setText(intent.getStringExtra("title"));
                artist.setText(intent.getStringExtra("artist"));

            }

        }
    }

}

