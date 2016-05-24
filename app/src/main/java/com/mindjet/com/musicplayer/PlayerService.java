package com.mindjet.com.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.animation.AnimationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PlayerService extends Service {


    private MediaPlayer mediaPlayer = new MediaPlayer();
    private static int current_progress; //记录当前播放到某首歌的哪个位置（毫秒）
    private String musicPath; //当前播放歌曲的路径

    public List<LrcContent> lrcContentList = new ArrayList<LrcContent>(); //存放歌词列表对象
    public static int index = 0; //当前时间对应的歌词索引

    private boolean isFirstTimeSetList = true; //是否第一次为 lrcView 设置资源
    private boolean music_change_need2refresh = true; //播放完成后是否要进行 lrcView 的资源更新

    //更新进度条旁的时间
    private android.os.Handler handler = new android.os.Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 1) {

                if (mediaPlayer != null) {

                    current_progress = mediaPlayer.getCurrentPosition();
                    Intent intent = new Intent();
                    intent.putExtra("current", current_progress);
                    intent.putExtra("whole", PlayerState.mp3InfoList.get(PlayerState.music_position).duration);
                    intent.setAction(AppConstant.ActionMsg.MUSIC_CURRENT);
                    sendBroadcast(intent);
                    handler.sendEmptyMessageDelayed(1, 1000);

                }

            }

        }

    };


    //为 mediaplayer 添加播放结束监听器
    @Override
    public void onCreate() {


        //监听音乐播放器结束后该如何继续
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                if (!PlayerState.isPause) {

                    switch (PlayerState.mode) {

                        case 1:
                            play(0);
                            break;

                        case 2:

                            PlayerState.music_position++;
                            if (PlayerState.music_position > PlayerState.mp3InfoList.size() - 1)
                                PlayerState.music_position = 0;

                            musicPath = PlayerState.mp3InfoList.get(PlayerState.music_position).url;
                            current_progress = 0;
                            music_change_need2refresh = true;
                            PlayerState.isPlaying = true;
                            PlayerState.isPause = false;
                            play(0);
                            updateTitle();
                            break;

                        case 3:

                            PlayerState.music_position = (int) Math.floor(Math.random() * PlayerState.mp3InfoList
                                    .size());
                            musicPath = PlayerState.mp3InfoList.get(PlayerState.music_position).url;
                            current_progress = 0;
                            music_change_need2refresh = true;
                            PlayerState.isPlaying = true;
                            PlayerState.isPause = false;
                            play(0);
                            updateTitle();

                            break;
                    }
                }

            }
        });


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //每次 startService 都会调用此方法
    //解析 intent
    //根据 intent 的 MSG 来执行相应的操作，上一首/下一首/播放/暂停
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mediaPlayer.isPlaying()) stop();

        if (intent.getStringExtra("url") != null) musicPath = intent.getStringExtra("url");
        int msg = intent.getIntExtra("MSG", 0);

        switch (msg) {

            case AppConstant.PlayerMsg.PLAY_MSG:
                updateTitle();
                play(0);
                break;
            case AppConstant.PlayerMsg.PAUSE_MSG:
                pause();
                break;
            case AppConstant.PlayerMsg.STOP_MSG:
                stop();
                break;
            case AppConstant.PlayerMsg.CONTINUE_MSG:
                continue_play();
                break;
            case AppConstant.PlayerMsg.PREVIOUS_MSG:
                previous();
                break;
            case AppConstant.PlayerMsg.NEXT_MSG:
                next();
                break;
            case AppConstant.PlayerMsg.PROGRESS_CHANGE:

                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                //在 mediaplayer 暂停时，利用 getDuration 得到的结果是错误的
                long duration = PlayerState.mp3InfoList.get(PlayerState.music_position).duration;
                current_progress = (int) (intent.getIntExtra("progress", 0) * duration / 100);
                play(current_progress);
                handler.sendEmptyMessage(1);

                PlayerState.isPause = false;
                PlayerState.isPlaying = true;
                break;

        }
        isFirstTimeSetList = true;

        return super.onStartCommand(intent, flags, startId);
    }


    private void next() {

        PlayerState.music_position--;
        musicPath = PlayerState.mp3InfoList.get(PlayerState.music_position).url;
        long duration = PlayerState.mp3InfoList.get(PlayerState.music_position).duration;
        play((int) duration);

    }

    private void previous() {

        updateTitle();
        play(0);
        current_progress = 0;

    }

    //继续播放
    private void continue_play() {

        play(current_progress);

    }

    //播放
    private void play(int position) {

        initLrc();
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(musicPath);
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new MyPreparedListener(position));

            //发送广播通知 activity 更新进度等
            handler.sendEmptyMessage(1);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //暂停
    private void pause() {

        current_progress = mediaPlayer.getCurrentPosition();
        mediaPlayer.pause();

    }

    //停止
    private void stop() {

        if (mediaPlayer != null) {

            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    //发送广播，更新两个 activity 的正在播放信息
    private void updateTitle() {

        Intent intent = new Intent();
        intent.setAction(AppConstant.ActionMsg.UPDATE_TITLE);
        intent.putExtra("title", PlayerState.mp3InfoList.get(PlayerState.music_position).title);
        intent.putExtra("artist", PlayerState.mp3InfoList.get(PlayerState.music_position).artist);
        sendBroadcast(intent);

    }

    //在播放前获取歌词，并且为 LrcView 设置歌词资源和索引
    private void initLrc() {

        handler.removeCallbacks(mRunnable);

        //新建歌词处理的类，并根据歌曲的url开始读歌词
        LrcProcess lrcProcess = new LrcProcess();
        lrcProcess.readLRC(musicPath);

        //将歌词送到 List<LrcContent> 对象中
        lrcContentList = lrcProcess.getLrcContentList();

        //资源还是在 intent 中传给 LyricActivity 再给 lrcView 比较妥当

        //利用 handler 循环更新歌词
        handler.post(mRunnable);


    }

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            index = lrcIndex();

            if (LyricActivity.lrcView != null) {

                if (isFirstTimeSetList || music_change_need2refresh) {
                    isFirstTimeSetList = false;
                    music_change_need2refresh = false;
                    LyricActivity.lrcView.setmLrcList(lrcContentList);
                    LyricActivity.lrcView.setAnimation(AnimationUtils.loadAnimation(PlayerService.this, R.anim
                            .alpha_z));
                }

                //设置歌词
                LyricActivity.lrcView.setIndex(index);

                //invalidate 是调用 onDraw 方法
                LyricActivity.lrcView.invalidate();

            }

            //更新 UI
            handler.postDelayed(mRunnable, 100);


        }
    };

    //取得此时对应歌词的行号
    private int lrcIndex() {

        current_progress = mediaPlayer.getCurrentPosition();
        int duration = mediaPlayer.getDuration();

        if (current_progress < duration) {

            //通过比较 current——progress 和每一行 LrcTime
            // 1.若 current progress < 第一行的 LrcTime，那么此时返回第一行的索引 0
            // 2.若 current progress > 最后一行的 LrcTime，那么此时返回最后一行的索引 lrcContentList.size()-1
            // 3.其他情况下，若 current progress > 某一行的 lrcTime 而小于下一行的 LrcTime，那么返回这一行的索引 i

            for (int i = 0; i < lrcContentList.size(); i++) {
                if (i < lrcContentList.size() - 1) {
                    //第一种情况
                    if (current_progress < lrcContentList.get(i).getLrcTime() && i == 0) index = i;
                    //第三种情况
                    if (current_progress > lrcContentList.get(i).getLrcTime() && current_progress < lrcContentList
                            .get(i + 1).getLrcTime())
                        index = i;
                }
                //第二种情况
                if (i == lrcContentList.size() - 1 && current_progress > lrcContentList.get(i).getLrcTime()) index = i;
            }

        }
        return index;

    }


    //监听器，监听是否 prepare 完成，完成即播放音乐
    private class MyPreparedListener implements MediaPlayer.OnPreparedListener {

        private int position;

        public MyPreparedListener(int position) {
            this.position = position;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {

            mediaPlayer.start();
            if (position > 0)
                mediaPlayer.seekTo(position);

        }
    }


    //销毁时，释放 mediaplayer
    @Override
    public void onDestroy() {

        mediaPlayer.stop();
        mediaPlayer.release();

    }


}

