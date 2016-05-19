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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PlayerActivity extends AppCompatActivity {

    private ListView musicList;
    private List<Mp3Info> mp3InfoList;
    private Button btn_pre,btn_next,btn_play,btn_shuffle,btn_repeat;
    private SeekBar song_progress;
    private TextView song_name,current_progress;

    private boolean isPlaying = false;
    private boolean isPause = true;
    private boolean isFirstTime = true;
    private String current_musicPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_playeractivity);

        initUI();


        //从数据库拉取出 mp3 数据并返回一个 List<Mp3Info> 对象作为适配器的资源
        mp3InfoList = MediaUtil.getMp3InfoList(getApplicationContext());
        current_musicPath = mp3InfoList.get(0).url;

        //为适配器绑定资源并且将改适配器设置给 listView
        setListAdapter(mp3InfoList);

        //为 listView 每个项目添加点击事件
        musicList.setOnItemClickListener(new MusicListItemClickListener());
    }

    //实例化 widget 以及绑定监听器
    private void initUI() {

        musicList = (ListView) findViewById(R.id.listView);
        btn_next = (Button) findViewById(R.id.next);
        btn_pre = (Button) findViewById(R.id.previous);
        btn_play = (Button) findViewById(R.id.play);
        btn_repeat = (Button) findViewById(R.id.repeat);
        btn_shuffle = (Button) findViewById(R.id.shuffle);
        song_progress = (SeekBar) findViewById(R.id.seekBar);
        song_name = (TextView) findViewById(R.id.song_name);
        current_progress = (TextView) findViewById(R.id.current_progress);

        MyOnClickListener listener = new MyOnClickListener();
        btn_next.setOnClickListener(listener);
        btn_play.setOnClickListener(listener);
        btn_pre.setOnClickListener(listener);
        btn_shuffle.setOnClickListener(listener);
        btn_repeat.setOnClickListener(listener);

    }

    //为 musicList 设置监听器
    private void setListAdapter(List<Mp3Info> mp3InfoList) {

        List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();

        for (int i = 0; i < mp3InfoList.size(); i++) {

            Map<String, String> map = new HashMap<String, String>();

            map.put("title", mp3InfoList.get(i).title);
            map.put("artist", "艺术家：" + mp3InfoList.get(i).artist);
            map.put("duration", "时长：" + MediaUtil.formatTime(mp3InfoList.get(i).duration));
            map.put("size", String.valueOf(mp3InfoList.get(i).size));
            map.put("url", mp3InfoList.get(i).url);


            mapList.add(map);

        }

        SimpleAdapter adapter = new SimpleAdapter(this, mapList, R.layout.list_item, new String[]{"title", "artist",
                "duration"}, new int[]{R.id.title, R.id.artist, R.id.duration});

        musicList.setAdapter(adapter);
    }

    //listView 的项目监听器，点击后开启播放服务
    private class MusicListItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (mp3InfoList != null) {

                Toast.makeText(PlayerActivity.this, "asdhias", Toast.LENGTH_SHORT).show();
                Mp3Info mp3Info = mp3InfoList.get(position);
                Intent intent = new Intent();
                Log.i("LOG", mp3Info.toString());
                intent.putExtra("url", mp3Info.url);
                intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
                intent.setClass(PlayerActivity.this, PlayerService.class);
                startService(intent);

                current_musicPath = mp3Info.url;
                btn_play.setText("暂停");
                isPlaying = true;
                isPause = false;

            }

        }


    }

    //自定义的按钮点击监听器
    private class MyOnClickListener implements View.OnClickListener{


        @Override
        public void onClick(View v) {

            switch (v.getId()){

                case R.id.play:
                    if (isFirstTime){

                        isPlaying = true;
                        isPause = false;
                        isFirstTime = false;

                        btn_play.setText("暂停");

                        Intent intent = new Intent();
                        intent.setAction("com.mindjet.media.MUSIC_SERVICE");
                        intent.putExtra("url",current_musicPath);
                        intent.putExtra("MSG",AppConstant.PlayerMsg.PLAY_MSG);
                        startService(intent);

                    }else if (isPlaying){

                        isPlaying = false;
                        isPause = true;
                        btn_play.setText("播放");
                        Intent intent = new Intent();
                        intent.setAction("com.mindjet.media.MUSIC_SERVICE");
                        intent.putExtra("url",current_musicPath);
                        intent.putExtra("MSG",AppConstant.PlayerMsg.PAUSE_MSG);
                        startService(intent);

                    }else if (isPause){

                        isPlaying = true;
                        isPause = false;
                        btn_play.setText("暂停");
                        Intent intent = new Intent();
                        intent.setAction("com.mindjet.media.MUSIC_SERVICE");
                        intent.putExtra("url",current_musicPath);
                        intent.putExtra("MSG",AppConstant.PlayerMsg.PLAY_MSG);
                        startService(intent);

                    }
                    break;

            }

        }
    }
}
