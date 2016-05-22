package com.mindjet.com.musicplayer;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;


public class PlayerActivity extends AppCompatActivity {

    private ListView musicList;
    private List<Mp3Info> mp3InfoList;
    private Button btn_pre,btn_next,btn_play,btn_shuffle,btn_repeat;
    private SeekBar seekbar_progress;
    private TextView song_name,current_progress,bar_title;
    private ImageView album;

    private boolean isPlaying = false;
    private boolean isPause = true;
    private boolean isFirstTime = true;
    private String current_musicPath = null;
    private int music_position = 0;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
        musicList.setOnCreateContextMenuListener(new MusicListItemContextMenuListener());
    }


    //实例化 widget 以及绑定监听器
    private void initUI() {

        musicList = (ListView) findViewById(R.id.listView);
        btn_next = (Button) findViewById(R.id.next);
        btn_pre = (Button) findViewById(R.id.previous);
        btn_play = (Button) findViewById(R.id.play);
        btn_repeat = (Button) findViewById(R.id.repeat);
        btn_shuffle = (Button) findViewById(R.id.shuffle);
        seekbar_progress = (SeekBar) findViewById(R.id.seekBar);
        song_name = (TextView) findViewById(R.id.song_name);
        current_progress = (TextView) findViewById(R.id.current_progress);
        album = (ImageView) findViewById(R.id.album);

        //设置伪actionbar，设置有逼格的字体
        bar_title = (TextView) findViewById(R.id.textView2);
        Typeface typeFace =Typeface.createFromAsset(getAssets(),"fonts/SCRIPTBL.TTF");
        bar_title.setTypeface(typeFace);

        MyOnClickListener listener = new MyOnClickListener();
        album.setOnClickListener(listener);
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

            String duration = MediaUtil.formatTime(mp3InfoList.get(i).duration);
            map.put("duration", "时长：" + duration);
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

                Mp3Info mp3Info = mp3InfoList.get(position);
                Intent intent = new Intent();
                intent.putExtra("url", mp3Info.url);
                intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
                intent.setAction("com.mindjet.media.MUSIC_SERVICE");
                startService(intent);

                music_position = position;
                isFirstTime = false;
                isPlaying = true;
                isPause = false;
                btn_play.setText("暂停");
                updateTitle();

            }

        }


    }

    //自定义的按钮点击监听器
    private class MyOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {

            switch (v.getId()){

                //点击 播放/暂停 按钮
                case R.id.play:
                    play_pause_continue();
                    updateTitle();
                    break;

                //点击 上一首 按钮
                case R.id.previous:
                    previous();
                    updateTitle();
                    break;

                //点击 下一首 按钮
                case R.id.next:
                    next();
                    updateTitle();
                    break;

                case R.id.album:
                    change();


            }

        }

    }

    private void change() {

        Mp3Info mp3Info = mp3InfoList.get(music_position);
        Intent intent = new Intent(PlayerActivity.this, LyricActivity.class);

        intent.putExtra("title",mp3Info.title);
        intent.putExtra("artist",mp3Info.artist);
        intent.putExtra("duration",MediaUtil.formatTime(mp3Info.duration));

        //第一次启动，需要将歌词索引传进去给 lrcView
        if (LyricActivity.lrcView==null){
            intent.putExtra("index",PlayerService.index);
        }

        startActivity(intent);


    }

    //更新 footer 的音乐信息
    private void updateTitle() {

        song_name.setText(mp3InfoList.get(music_position).title);
        current_progress.setText(MediaUtil.formatTime(mp3InfoList.get(music_position).duration));

    }

    private void play_pause_continue() {

        if (isFirstTime){

            isPlaying = true;
            isPause = false;
            isFirstTime = false;

            btn_play.setText("暂停");

            Intent intent = new Intent();
            intent.setAction("com.mindjet.media.MUSIC_SERVICE");
            intent.putExtra("url",mp3InfoList.get(music_position).url);
            intent.putExtra("MSG",AppConstant.PlayerMsg.PLAY_MSG);
            startService(intent);

        }else if (isPlaying){

            isPlaying = false;
            isPause = true;
            btn_play.setText("播放");
            Intent intent = new Intent();
            intent.setAction("com.mindjet.media.MUSIC_SERVICE");
            intent.putExtra("url",mp3InfoList.get(music_position).url);
            intent.putExtra("MSG",AppConstant.PlayerMsg.PAUSE_MSG);
            startService(intent);

        }else if (isPause){

            isPlaying = true;
            isPause = false;
            btn_play.setText("暂停");
            Intent intent = new Intent();
            intent.setAction("com.mindjet.media.MUSIC_SERVICE");
            intent.putExtra("url",mp3InfoList.get(music_position).url);
            intent.putExtra("MSG",AppConstant.PlayerMsg.CONTINUE_MSG);
            startService(intent);

        }

    }

    //处理下一首按钮的点击事件
    private void next() {

        if (music_position+1<=mp3InfoList.size()-1){

            //初始化各参数与界面
            music_position+=1;
            isFirstTime = false;
            isPlaying = true;
            isPause = false;
            btn_play.setText("暂停");

            //开启服务
            Mp3Info mp3Info = mp3InfoList.get(music_position);
            Intent intent = new Intent();
            intent.putExtra("url",mp3Info.url);
            intent.putExtra("MSG",AppConstant.PlayerMsg.NEXT_MSG);
            intent.setAction("com.mindjet.media.MUSIC_SERVICE");
            startService(intent);

        }else {

            Toast.makeText(this, "没有下一首了", Toast.LENGTH_SHORT).show();

        }

    }

    //处理上一首按钮的点击事件
    private void previous() {

        if (music_position-1>=0){

            //初始化各参数与界面
            music_position-=1;
            isFirstTime = false;
            isPlaying = true;
            isPause = false;
            btn_play.setText("暂停");

            //开启服务
            Mp3Info mp3Info = mp3InfoList.get(music_position);
            Intent intent = new Intent();
            intent.putExtra("url",mp3Info.url);
            intent.putExtra("MSG",AppConstant.PlayerMsg.PREVIOUS_MSG);
            intent.setAction("com.mindjet.media.MUSIC_SERVICE");
            startService(intent);

        }else {

            Toast.makeText(this, "没有上一首了", Toast.LENGTH_SHORT).show();

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private class MusicListItemContextMenuListener implements View.OnCreateContextMenuListener {

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

            Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(50);

            //调出对话框
            musicListItemDialog();

        }
    }

    private void musicListItemDialog() {



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //退出 app 时，将 Service 关闭，不然的话会提示“停止运行”
        Intent intent = new Intent(PlayerActivity.this, PlayerService.class);
        stopService(intent);

    }

}
