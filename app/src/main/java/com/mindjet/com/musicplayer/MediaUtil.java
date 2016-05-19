package com.mindjet.com.musicplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mindjet on 2016/5/19.
 */
public class MediaUtil {

    public static List<Mp3Info> getMp3InfoList(Context context) {

        List<Mp3Info> mp3InfoList = new ArrayList<Mp3Info>();

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore
                .Audio.Media.DEFAULT_SORT_ORDER);

        for (int i = 0; i < cursor.getCount(); i++) {

            Mp3Info mp3Info = new Mp3Info();

            cursor.moveToNext();

            mp3Info.id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            mp3Info.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            mp3Info.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            mp3Info.duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            mp3Info.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            mp3Info.url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            mp3Info.isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));

            mp3InfoList.add(mp3Info);

        }

        return mp3InfoList;

    }

    //convert time from millisecond to minute:second
    public static String formatTime(long duration) {

        long second = duration / 1000;
        long minute = second / 60;
        second = second % 60;
        return minute+":"+second;

    }


}
