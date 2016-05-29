package com.mindjet.com.musicplayer;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mindjet.com.musicplayer.Constant.AppConstant;
import com.mindjet.com.musicplayer.Constant.PlayerSource;
import com.mindjet.com.musicplayer.Utils.LrcView;
import com.mindjet.com.musicplayer.Utils.MediaUtil;


/**
 * @author Mindjet
 * @date 2016.5.20
 * 显示歌词界面
 * 控制 上一首/下一首/播放/暂停/继续/播放模式/进度调整
 */


public class LyricActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView song_name, current_progress, whole_progress;
    private Button pre, next, play, settting, show_volume_panel, move_back;
    public static LrcView lrcView;
    private SeekBar lrc_seekbar, volume_seekbar;
    private RelativeLayout volume_panel;

    private boolean isPanelShow = false;
    private boolean isFirstTimeStart = true;

    private AudioManager audioManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lrc_layout);

        initUI();

        //沉浸式titlebar
        immersiveMode();

        initVolumeControl();

        //注册广播接收器
        initBroadcastReceiver();

    }

    //做一些初始化操作
    @Override
    protected void onStart() {
        super.onStart();

        //第一次启动 LyricActivity 时，需要将此时传进来的 index 送给 LrcView
        //以后就不必了，因为在 PlayerService 判断 lrcView 存在，直接在那边传给 lrcView
        if (isFirstTimeStart) {
            isFirstTimeStart = false;
            Intent intent = getIntent();
            int index = intent.getIntExtra("index", 0);
            lrcView.setIndex(index);
            lrcView.invalidate();
        }

        //判断此时播放状态，确定 播放键 的状态 -- “播放”？“暂停”？“继续”？
        update_icon_state();

    }

    private void initVolumeControl() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume_seekbar.setMax(max);
        volume_seekbar.setProgress(current);
        volume_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            private int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.progress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }
        });
    }

    private void initBroadcastReceiver() {
        LrcActivityReceiver receiver = new LrcActivityReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstant.ActionMsg.MUSIC_CURRENT);
        filter.addAction(AppConstant.ActionMsg.UPDATE_TITLE);
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(receiver, filter);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void immersiveMode() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            Window window = getWindow();
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            );

        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

    }

    //实例化UI，添加监听器
    private void initUI() {

        song_name = (TextView) findViewById(R.id.song_name);
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

                if (PlayerSource.isServiceOnline) {
                    Intent intent = new Intent();
                    intent.setAction("com.mindjet.media.MUSIC_SERVICE");
                    intent.putExtra("progress", this.progress);
                    intent.putExtra("MSG", AppConstant.PlayerMsg.PROGRESS_CHANGE);
                    intent.setPackage("com.mindjet.com.musicplayer");
                    startService(intent);
                }
            }
        });

        pre = (Button) findViewById(R.id.lrc_previous);
        next = (Button) findViewById(R.id.lrc_next);
        play = (Button) findViewById(R.id.lrc_play);
        show_volume_panel = (Button) findViewById(R.id.lrc_volume);
        settting = (Button) findViewById(R.id.lrc_setting);
        move_back = (Button) findViewById(R.id.move_back);
        volume_seekbar = (SeekBar) findViewById(R.id.volume_control_bar);
        volume_panel = (RelativeLayout) findViewById(R.id.volume_panel);
        volume_panel.setVisibility(View.GONE);

        pre.setOnClickListener(this);
        next.setOnClickListener(this);
        play.setOnClickListener(this);
        move_back.setOnClickListener(this);
        show_volume_panel.setOnClickListener(this);
        settting.setOnClickListener(this);


        Intent intent = getIntent();
        song_name.setText(intent.getStringExtra("title"));
        whole_progress.setText(intent.getStringExtra("duration"));


    }


    //按钮点击事件
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.lrc_play:
                play_pause_continue();
                play_scale_anim(play);
                break;

            case R.id.lrc_next:
                if (PlayerSource.music_position <= PlayerSource.mp3InfoList.size() - 2) {

                    PlayerSource.music_position++;
                    PlayerSource.isFirstTime = false;
                    PlayerSource.isPlaying = true;
                    PlayerSource.isPause = false;
                    update_icon_state();

                    Intent intent = new Intent();
                    intent.setAction("com.mindjet.media.MUSIC_SERVICE");
                    intent.putExtra("url", PlayerSource.mp3InfoList.get(PlayerSource.music_position).url);
                    intent.putExtra("MSG", AppConstant.PlayerMsg.NEXT_MSG);
                    intent.setPackage("com.mindjet.com.musicplayer");
                    startService(intent);
                    play_scale_anim(next);

                } else {

                    Toast.makeText(this, "没有下一首了", Toast.LENGTH_SHORT).show();

                }
                break;

            case R.id.lrc_previous:
                if (PlayerSource.music_position > 0) {

                    PlayerSource.music_position--;
                    PlayerSource.isFirstTime = false;
                    PlayerSource.isPlaying = true;
                    PlayerSource.isPause = false;
                    update_icon_state();
                    play_scale_anim(pre);

                    Intent intent = new Intent();
                    intent.setAction("com.mindjet.media.MUSIC_SERVICE");
                    intent.putExtra("url", PlayerSource.mp3InfoList.get(PlayerSource.music_position).url);
                    intent.putExtra("MSG", AppConstant.PlayerMsg.PREVIOUS_MSG);
                    intent.setPackage("com.mindjet.com.musicplayer");
                    startService(intent);

                } else {

                    Toast.makeText(this, "没有上一首了", Toast.LENGTH_SHORT).show();

                }
                break;

            case R.id.move_back:
                play_scale_anim(move_back);
                finish();
                break;

            case R.id.lrc_volume:
                play_scale_anim(show_volume_panel);
                show_hide_volome_panel();
                break;

            case R.id.lrc_setting:
                play_scale_anim(settting);
                break;

        }

    }

    private void play_pause_continue() {
        Intent intent = new Intent();
        intent.setAction("com.mindjet.media.MUSIC_SERVICE");
        intent.setPackage("com.mindjet.com.musicplayer");
        intent.putExtra("url", PlayerSource.mp3InfoList.get(PlayerSource.music_position).url);

        if (PlayerSource.isFirstTime) {

            PlayerSource.isFirstTime = false;
            PlayerSource.isPlaying = true;
            PlayerSource.isPause = false;
            update_icon_state();

            intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);

        } else if (PlayerSource.isPlaying) {

            PlayerSource.isPlaying = false;
            PlayerSource.isPause = true;
            update_icon_state();

            intent.putExtra("MSG", AppConstant.PlayerMsg.PAUSE_MSG);


        } else if (PlayerSource.isPause) {

            PlayerSource.isPause = false;
            PlayerSource.isPlaying = true;
            update_icon_state();

            intent.putExtra("MSG", AppConstant.PlayerMsg.CONTINUE_MSG);

        }
        startService(intent);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void show_hide_volome_panel() {

        if (volume_panel.getVisibility() == View.GONE || !isPanelShow) {

            isPanelShow = true;
            ObjectAnimator.ofFloat(volume_panel, "translationY", -120f, 0f).start();
            volume_panel.setVisibility(View.VISIBLE);

        } else {

            isPanelShow = false;
            ObjectAnimator.ofFloat(volume_panel, "translationY", 0, -120f).start();

        }

    }

    //更改按键的状态
    private void update_icon_state() {

        //改变播放暂停按钮的状态
        if (PlayerSource.isPause) {
            play.setBackgroundResource(R.mipmap.play_continue_orange);
        } else if (PlayerSource.isPlaying) {
            play.setBackgroundResource(R.mipmap.pause_orange);
        }

    }

    //缩放动画
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void play_scale_anim(Button btn) {

        ObjectAnimator.ofFloat(btn, "scaleX", 1f, 0.9f).start();
        ObjectAnimator.ofFloat(btn, "scaleY", 1f, 0.9f).start();
        ObjectAnimator.ofFloat(btn, "scaleX", 0.9f, 1f).start();
        ObjectAnimator.ofFloat(btn, "scaleY", 0.9f, 1f).start();

    }


    //广播接收器，
    // 接收来自 音乐服务的广播，来更新 曲目信息 和 播放进度
    // 接收来自 音量变化的广播，更新进度条
    class LrcActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action.equals(AppConstant.ActionMsg.MUSIC_CURRENT)) {

                int current = intent.getIntExtra("current", -1);
                current_progress.setText(MediaUtil.formatTime(current));
                whole_progress.setText(MediaUtil.formatTime(PlayerSource.mp3InfoList.get(PlayerSource.music_position)
                        .duration));
                lrc_seekbar.setProgress((int) (100 * current / intent.getLongExtra("whole", -1)));

            }

            if (action.equals(AppConstant.ActionMsg.UPDATE_TITLE)) {

                song_name.setText(intent.getStringExtra("title"));

            }

            if (action.equals("android.media.VOLUME_CHANGED_ACTION")) {

                volume_seekbar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

            }

        }
    }

}

