package com.mindjet.com.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;


public class PlayerService extends Service{

    private String musicPath;
    private MediaPlayer mediaPlayer = new MediaPlayer();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mediaPlayer.isPlaying())  stop();

        musicPath = intent.getStringExtra("url");
        Toast.makeText(this, musicPath, Toast.LENGTH_SHORT).show();
        int msg = intent.getIntExtra("MSG", 0);

        if (msg==AppConstant.PlayerMsg.PLAY_MSG)
            play(0);
        else if (msg==AppConstant.PlayerMsg.PAUSE_MSG)
            pause();
        else if (msg==AppConstant.PlayerMsg.STOP_MSG)
            stop();

        return super.onStartCommand(intent, flags, startId);
    }

    //播放
    private void play(int position) {

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

        if (mediaPlayer.isPlaying())
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
        if (mediaPlayer!=null){

            mediaPlayer.stop();
            mediaPlayer.release();

        }
    }
}
