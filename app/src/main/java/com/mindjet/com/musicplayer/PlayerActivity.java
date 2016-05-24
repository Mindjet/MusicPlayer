package com.mindjet.com.musicplayer;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author mindjet
 * @date 2016.5.19
 * 音乐播放器主界面
 * 显示歌曲列表
 * 控制 上一首/下一首/播放/暂停/继续/进度调整/播放模式/跳转
 */

public class PlayerActivity extends AppCompatActivity {

    private ListView musicList;

    private Button btn_pre, btn_next, btn_play, btn_shuffle, btn_repeat;
    private SeekBar seekbar_progress;
    private TextView song_name;
    private TextView current_progress;
    private ImageView album;

    private PlayerActivityReceiver receiver;


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_playeractivity);

        //执行 findViewById，绑定监听器，为listView设置适配器
        initUI();

        receiver = new PlayerActivityReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstant.ActionMsg.MUSIC_CURRENT);
        filter.addAction(AppConstant.ActionMsg.UPDATE_TITLE);
        registerReceiver(receiver, filter);


    }


    //初始化控件
    @Override
    protected void onStart() {
        super.onStart();

        //TODO 判断播放状态来控制 UI
        //TODO 后期将设计为图形按钮，通过 广播机制 来更新
        if (PlayerState.isPlaying) btn_play.setText("暂停");
        if (PlayerState.isPause) btn_play.setText("继续");
        if (PlayerState.isFirstTime) btn_play.setText("播放");
    }

    //销毁 service
    @Override
    protected void onDestroy() {
        super.onDestroy();

        //退出 app 时，将 Service 关闭，不然的话会提示“停止运行”
        Intent intent = new Intent(PlayerActivity.this, PlayerService.class);
        stopService(intent);
        unregisterReceiver(receiver);

    }


    //实例化 widget 以及绑定监听器
    private void initUI() {

        //设置伪actionbar，设置有逼格的字体
        TextView bar_title = (TextView) findViewById(R.id.textView2);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/SCRIPTBL.TTF");
        if (bar_title != null) {
            bar_title.setTypeface(typeFace);
        }

        //按钮组部分
        btn_next = (Button) findViewById(R.id.next);
        btn_pre = (Button) findViewById(R.id.previous);
        btn_play = (Button) findViewById(R.id.play);
        btn_repeat = (Button) findViewById(R.id.repeat);
        btn_shuffle = (Button) findViewById(R.id.shuffle);

        //footer 部分
        seekbar_progress = (SeekBar) findViewById(R.id.seekBar);
        seekbar_progress.setOnSeekBarChangeListener(new MySeekBarListener());
        song_name = (TextView) findViewById(R.id.song_name);
        current_progress = (TextView) findViewById(R.id.current_progress);
        current_progress.setText("00:00");
        album = (ImageView) findViewById(R.id.album);


        MyOnClickListener listener = new MyOnClickListener();
        album.setOnClickListener(listener);
        btn_next.setOnClickListener(listener);
        btn_play.setOnClickListener(listener);
        btn_pre.setOnClickListener(listener);
        btn_shuffle.setOnClickListener(listener);
        btn_repeat.setOnClickListener(listener);


        musicList = (ListView) findViewById(R.id.listView);
        //从数据库拉取出 mp3 数据并返回一个 List<Mp3Info> 对象作为适配器的资源
        PlayerState.mp3InfoList = MediaUtil.getMp3InfoList(getApplicationContext());

        //为适配器绑定资源并且将改适配器设置给 listView
        setListAdapter(PlayerState.mp3InfoList);

        //为 listView 每个项目添加点击事件
        musicList.setOnItemClickListener(new MusicListItemClickListener());
        musicList.setOnCreateContextMenuListener(new MusicListItemContextMenuListener());

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

            if (PlayerState.mp3InfoList != null) {

                Mp3Info mp3Info = PlayerState.mp3InfoList.get(position);
                Intent intent = new Intent();
                intent.putExtra("url", mp3Info.url);
                intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
                intent.setAction("com.mindjet.media.MUSIC_SERVICE");
                startService(intent);

                PlayerState.music_position = position;
                PlayerState.isFirstTime = false;
                PlayerState.isPlaying = true;
                PlayerState.isPause = false;
                btn_play.setText("暂停");

            }

        }

    }

    //自定义的按钮点击监听器
    private class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                //点击 播放/暂停 按钮
                case R.id.play:
                    play_pause_continue();
                    break;

                //点击 上一首 按钮
                case R.id.previous:
                    previous();
                    break;

                //点击 下一首 按钮
                case R.id.next:
                    next();
                    break;

                case R.id.album:
                    change();
                    break;

                case R.id.repeat:
                    //TODO 后期与 lyricactivity相同方法包装
                    if (PlayerState.mode != 1) {
                        PlayerState.mode = 1;
                        btn_repeat.setText("顺序");
                        Toast.makeText(PlayerActivity.this, "当前播放模式：单曲循环", Toast.LENGTH_SHORT).show();
                    } else if (PlayerState.mode == 1) {
                        PlayerState.mode = 2;
                        btn_repeat.setText("单曲");
                        Toast.makeText(PlayerActivity.this, "当前播放模式：顺序播放", Toast.LENGTH_SHORT).show();
                    }

                    break;

                case R.id.shuffle:

                    //TODO 后期与 lyricactivity相同方法包装
                    if (PlayerState.mode != 3) {
                        PlayerState.mode = 3;
                        btn_shuffle.setText("顺序");
                        Toast.makeText(PlayerActivity.this, "当前播放模式：随机播放", Toast.LENGTH_SHORT).show();
                    } else if (PlayerState.mode == 3) {
                        PlayerState.mode = 2;
                        btn_shuffle.setText("随机");
                        Toast.makeText(PlayerActivity.this, "当前播放模式：顺序播放", Toast.LENGTH_SHORT).show();
                    }

                    break;

            }

        }

    }

    //跳转到 LyricActivity
    private void change() {

        Mp3Info mp3Info = PlayerState.mp3InfoList.get(PlayerState.music_position);
        Intent intent = new Intent(PlayerActivity.this, LyricActivity.class);

        intent.putExtra("title", mp3Info.title);
        intent.putExtra("artist", mp3Info.artist);
        intent.putExtra("duration", MediaUtil.formatTime(mp3Info.duration));

        //第一次启动，需要将歌词索引传进去给 lrcView
        if (LyricActivity.lrcView == null) {
            intent.putExtra("index", PlayerService.index);
        }

        startActivity(intent);

    }

    //处理 播放/暂停/继续 按钮
    private void play_pause_continue() {

        if (PlayerState.isFirstTime) {

            PlayerState.isPlaying = true;
            PlayerState.isPause = false;
            PlayerState.isFirstTime = false;

            btn_play.setText("暂停");

            Intent intent = new Intent();
            intent.setAction("com.mindjet.media.MUSIC_SERVICE");
            intent.putExtra("url", PlayerState.mp3InfoList.get(PlayerState.music_position).url);
            intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
            startService(intent);

        } else if (PlayerState.isPlaying) {

            PlayerState.isPlaying = false;
            PlayerState.isPause = true;
            btn_play.setText("播放");
            Intent intent = new Intent();
            intent.setAction("com.mindjet.media.MUSIC_SERVICE");
            intent.putExtra("MSG", AppConstant.PlayerMsg.PAUSE_MSG);
            startService(intent);

        } else if (PlayerState.isPause) {

            PlayerState.isPlaying = true;
            PlayerState.isPause = false;
            btn_play.setText("暂停");
            Intent intent = new Intent();
            intent.setAction("com.mindjet.media.MUSIC_SERVICE");
            intent.putExtra("MSG", AppConstant.PlayerMsg.CONTINUE_MSG);
            startService(intent);

        }

    }

    //处理 下一首 按钮的点击事件
    private void next() {

        if (PlayerState.music_position + 1 <= PlayerState.mp3InfoList.size() - 1) {

            //初始化各参数与界面
            PlayerState.music_position += 1;
            PlayerState.isFirstTime = false;
            PlayerState.isPlaying = true;
            PlayerState.isPause = false;
            btn_play.setText("暂停");

            //开启服务
            Mp3Info mp3Info = PlayerState.mp3InfoList.get(PlayerState.music_position);
            Intent intent = new Intent();
            intent.putExtra("url", mp3Info.url);
            intent.putExtra("MSG", AppConstant.PlayerMsg.NEXT_MSG);
            intent.setAction("com.mindjet.media.MUSIC_SERVICE");
            startService(intent);

        } else {

            Toast.makeText(this, "没有下一首了", Toast.LENGTH_SHORT).show();

        }

    }

    //处理 上一首 按钮的点击事件
    private void previous() {

        if (PlayerState.music_position - 1 >= 0) {

            //初始化各参数与界面
            PlayerState.music_position -= 1;
            PlayerState.isFirstTime = false;
            PlayerState.isPlaying = true;
            PlayerState.isPause = false;
            btn_play.setText("暂停");

            //开启服务
            Mp3Info mp3Info = PlayerState.mp3InfoList.get(PlayerState.music_position);
            Intent intent = new Intent();
            intent.putExtra("url", mp3Info.url);
            intent.putExtra("MSG", AppConstant.PlayerMsg.PREVIOUS_MSG);
            intent.setAction("com.mindjet.media.MUSIC_SERVICE");
            startService(intent);

        } else {

            Toast.makeText(this, "没有上一首了", Toast.LENGTH_SHORT).show();

        }

    }


    //长按音乐列表项的监听事件，
    // TODO 长按弹出对话框未完成
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

    //用来监听来自 service 的广播，以更新标题和进度条
    class PlayerActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action.equals(AppConstant.ActionMsg.MUSIC_CURRENT)) {

                int current = intent.getIntExtra("current", -1);
                current_progress.setText(MediaUtil.formatTime(current));
                seekbar_progress.setProgress((int) (100 * current / intent.getLongExtra("whole", -1)));

            }

            if (action.equals(AppConstant.ActionMsg.UPDATE_TITLE)) {

                song_name.setText(intent.getStringExtra("title"));

            }

        }
    }

    //监听进度条，停止滑动后将位置发送给 service 更新歌曲进度
    private class MySeekBarListener implements SeekBar.OnSeekBarChangeListener {

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
            intent.putExtra("MSG", AppConstant.PlayerMsg.PROGRESS_CHANGE);
            intent.putExtra("progress", this.progress);
            startService(intent);

        }
    }

    //重新keydown方法，防止按返回键将程序杀掉
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            moveTaskToBack(true);
            return true;

        }

        return super.onKeyDown(keyCode, event);
    }

}
