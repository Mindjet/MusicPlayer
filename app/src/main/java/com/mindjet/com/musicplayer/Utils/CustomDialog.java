package com.mindjet.com.musicplayer.Utils;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.mindjet.com.musicplayer.R;

/**
 * @author Mindjet
 * @date 2016/5/27
 * 定义musicList长按时出现的对话框
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CustomDialog extends DialogFragment {

    private TextView song_name, artist, duration, location, year, album;
    private String str_name;
    private String str_artist;
    private String str_duration;
    private String str_location;
    private String str_year;

    private String str_album;

    public void setStr_year(String str_year) {
        this.str_year = str_year;
    }

    public void setStr_album(String str_album) {
        this.str_album = str_album;
    }

    public void setStr_name(String str_name) {
        this.str_name = str_name;
    }

    public void setStr_artist(String str_artist) {
        this.str_artist = str_artist;
    }

    public void setStr_duration(String str_duration) {
        this.str_duration = str_duration;
    }

    public void setStr_location(String str_location) {
        this.str_location = str_location;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = inflater.inflate(R.layout.customdialog, container, false);

        song_name = (TextView) view.findViewById(R.id.info_song_name);
        artist = (TextView) view.findViewById(R.id.info_artist);
        duration = (TextView) view.findViewById(R.id.info_duration);
        location = (TextView) view.findViewById(R.id.info_loaction);
        year = (TextView) view.findViewById(R.id.info_year);
        album = (TextView) view.findViewById(R.id.info_album);

        song_name.setText("歌名：" + str_name);
        artist.setText("艺术家：" + str_artist);
        album.setText("专辑："+str_album);
        year.setText("年份："+str_year);
        duration.setText("时长：" + str_duration);
        location.setText("位置：" + str_location);

        return view;
    }
}
