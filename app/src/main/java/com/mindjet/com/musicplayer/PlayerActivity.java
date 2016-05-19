package com.mindjet.com.musicplayer;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PlayerActivity extends AppCompatActivity {

    private ListView musicList;
    private List<Mp3Info> mp3InfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_playeractivity);

        initUI();
        musicList.setOnItemClickListener(new MusicListItemClickListener());

//        mp3infoList =
        mp3InfoList = getMp3InfoList();

        setListAdapter(mp3InfoList);
    }

    private void initUI() {

        musicList = (ListView) findViewById(R.id.listView);

    }

    private List<Mp3Info> getMp3InfoList() {

        List<Mp3Info> mp3InfoList = new ArrayList<Mp3Info>();

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

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

    private void setListAdapter(List<Mp3Info> mp3InfoList) {

        List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();

        for (int i = 0; i < mp3InfoList.size(); i++) {

            Map<String, String> map = new HashMap<String, String>();

            map.put("title", mp3InfoList.get(i).title);
            map.put("artist", "艺术家："+mp3InfoList.get(i).artist);
            map.put("duration", "时长："+String.valueOf(mp3InfoList.get(i).duration/1000)+'s');
            map.put("size", String.valueOf(mp3InfoList.get(i).size));
            map.put("url", mp3InfoList.get(i).url);

            mapList.add(map);

        }

        SimpleAdapter adapter = new SimpleAdapter(this, mapList, R.layout.list_item, new String[]{"title", "artist", "duration"}, new int[]{R.id.title,R.id.artist,R.id.duration});

        musicList.setAdapter(adapter);
    }


    private class MusicListItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (mp3InfoList!=null){

                Mp3Info mp3Info = mp3InfoList.get(position);
                Intent intent = new Intent();
                Log.i("LOG",mp3Info.toString());
                intent.putExtra("url",mp3Info.url);
                intent.putExtra("MSG",AppConstant.PlayerMsg.PLAY_MSG);
                intent.setClass(PlayerActivity.this,PlayerService.class);
                startService(intent);

            }

        }


    }
}
