package com.mindjet.com.musicplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.xml.datatype.Duration;


public class PlayerService extends Service{

    private String musicPath;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private int current_progress; //记录当前播放到某首歌的哪个位置（毫秒）
    private int duration;
    private LrcProcess lrcProcess; //处理歌词
    public List<LrcContent> lrcContentList = new ArrayList<LrcContent>(); //存放歌词列表对象
    public static int index = 0;
    private boolean isFirstTimeSetList = true;


    private android.os.Handler handler = new android.os.Handler(){

        @Override
        public void handleMessage(Message msg) {



        }

    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mediaPlayer.isPlaying())  stop();

        musicPath = intent.getStringExtra("url");
        int msg = intent.getIntExtra("MSG", 0);

        switch (msg){

            case AppConstant.PlayerMsg.PLAY_MSG:
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

        }
        isFirstTimeSetList = true;

        return super.onStartCommand(intent, flags, startId);
    }

    private void initLrc(){

        handler.removeCallbacks(mRunnable);

        //新建歌词处理的类，并根据歌曲的url开始读歌词
        lrcProcess = new LrcProcess();
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

            if (LyricActivity.lrcView!=null){

                if (isFirstTimeSetList){
                    isFirstTimeSetList = false;
                    LyricActivity.lrcView.setmLrcList(lrcContentList);
                    System.out.println("set list"+lrcContentList);
                    LyricActivity.lrcView.setAnimation(AnimationUtils.loadAnimation(PlayerService.this,R.anim.alpha_z));
                }

                //设置歌词
                LyricActivity.lrcView.setIndex(index);

                //invalidate 是调用 onDraw 方法
                LyricActivity.lrcView.invalidate();

            }

            //更新 UI
            handler.postDelayed(mRunnable,100);


        }
    };


    //利用 current_progress 和 duration，返回此时对应歌词的行号
    private int lrcIndex() {

        current_progress = mediaPlayer.getCurrentPosition();
        duration = mediaPlayer.getDuration();

        if (current_progress<duration){

            //通过比较 current——progress 和每一行 LrcTime
            // 1.若 current progress < 第一行的 LrcTime，那么此时返回第一行的索引 0
            // 2.若 current progress > 最后一行的 LrcTime，那么此时返回最后一行的索引 lrcContentList.size()-1
            // 3.其他情况下，若 current progress > 某一行的 lrcTime 而小于下一行的 LrcTime，那么返回这一行的索引 i

            for (int i=0;i<lrcContentList.size();i++){
                if (i<lrcContentList.size()-1){
                    //第一种情况
                    if (current_progress<lrcContentList.get(i).getLrcTime()&&i==0) index = i;
                    //第三种情况
                    if (current_progress>lrcContentList.get(i).getLrcTime()&&current_progress<lrcContentList.get(i+1).getLrcTime()) index = i;
                }
                //第二种情况
                if (i==lrcContentList.size()-1&&current_progress>lrcContentList.get(i).getLrcTime()) index=i;
            }

        }
        return index;

    }

    private void next() {

        play(0);
        current_progress = 0;

    }

    private void previous() {

        play(0);
        current_progress = 0;

    }

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
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //暂停
    private void pause(){

//        if (mediaPlayer.isPlaying())
            current_progress = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();

    }

    //停止
    private void stop(){

        if (mediaPlayer!=null){

            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    //监听器，在 prepare 后监听是否 prepare 完成，完成即播放音乐
    private class MyPreparedListener implements MediaPlayer.OnPreparedListener {

        private int position;

        public MyPreparedListener(int position) {
            this.position = position;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {

            mediaPlayer.start();
            if (position>0)
                mediaPlayer.seekTo(position);

        }
    }

    @Override
    public void onDestroy() {

            mediaPlayer.stop();
            mediaPlayer.release();


    }
}

