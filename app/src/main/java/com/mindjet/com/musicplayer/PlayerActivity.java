package com.mindjet.com.musicplayer;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mindjet.com.musicplayer.Constant.AppConstant;
import com.mindjet.com.musicplayer.Constant.PlayerSource;
import com.mindjet.com.musicplayer.Utils.CustomDialog;
import com.mindjet.com.musicplayer.Utils.MediaUtil;
import com.mindjet.com.musicplayer.ItemBean.Mp3Info;
import com.mindjet.com.musicplayer.Utils.MusicListAdapter;
import com.mindjet.com.musicplayer.Utils.PhoneCallListener;


/**
 * @author mindjet
 * @date 2016.5.19
 * 音乐播放器主界面
 * 显示歌曲列表
 * 控制 上一首/下一首/播放/暂停/继续/进度调整/播放模式/跳转
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PlayerActivity extends AppCompatActivity {

    private ListView musicList;

    private Button btn_pre, btn_next, btn_play, btn_shuffle, btn_repeat;
    private SeekBar seekbar_progress;
    private TextView song_name;
    private TextView current_progress;
    private ImageView album;

    private PlayerActivityReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_playeractivity);

        //沉浸式状态栏
        immersiveMode();

        //执行 findViewById，绑定监听器，为listView设置适配器
        initUI();

        //注册广播接收器
        initBroadcastReceiver();

        //注册电话监听器
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        manager.listen(new PhoneCallListener(getApplicationContext()), PhoneStateListener.LISTEN_CALL_STATE);

        //开启子线程去遍历整个内存文件夹，找出后缀为 .lrc 的文件
        new Thread() {
            @Override
            public void run() {

                PlayerSource.getLrcFile(Environment.getExternalStorageDirectory());

            }
        }.start();

    }

    private void initBroadcastReceiver() {
        receiver = new PlayerActivityReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstant.ActionMsg.MUSIC_CURRENT);
        filter.addAction(AppConstant.ActionMsg.UPDATE_TITLE);
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

    //初始化控件
    @Override
    protected void onStart() {
        super.onStart();

        //判断播放状态来控制 UI
        change_icon_state();

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
        PlayerSource.mp3InfoList = MediaUtil.getMp3InfoList(getApplicationContext());

        //实例化自定义的适配器并绑定
        MusicListAdapter adapter = new MusicListAdapter(PlayerSource.mp3InfoList, getApplicationContext());
        musicList.setAdapter(adapter);

        //为 listView 每个项目添加点击事件
        musicList.setOnItemClickListener(new MusicListItemClickListener());

        //为 listView 每个项目添加长按事件
        musicList.setOnItemLongClickListener(new MyItemLongClickListener());

    }


    /*-----------------------------------------控制按钮事件----------------------------------------------*/

    //处理 播放/暂停/继续 按钮
    private void play_pause_continue() {

        Intent intent = new Intent();
        intent.setAction("com.mindjet.media.MUSIC_SERVICE");

        if (PlayerSource.isFirstTime) {

            PlayerSource.isPlaying = true;
            PlayerSource.isPause = false;
            PlayerSource.isFirstTime = false;
            intent.putExtra("url", PlayerSource.mp3InfoList.get(PlayerSource.music_position).url);
            intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);


        } else if (PlayerSource.isPlaying) {

            PlayerSource.isPlaying = false;
            PlayerSource.isPause = true;
            intent.putExtra("MSG", AppConstant.PlayerMsg.PAUSE_MSG);

        } else if (PlayerSource.isPause) {

            PlayerSource.isPlaying = true;
            PlayerSource.isPause = false;
            intent.putExtra("MSG", AppConstant.PlayerMsg.CONTINUE_MSG);

        }
        intent.setPackage("com.mindjet.com.musicplayer");
        startService(intent);

        change_icon_state();
        play_scale_anim(btn_play);

    }

    //处理 上一首 按钮的点击事件
    private void previous() {

        if (PlayerSource.music_position - 1 >= 0) {

            //初始化各参数与界面
            PlayerSource.music_position -= 1;
            PlayerSource.isFirstTime = false;
            PlayerSource.isPlaying = true;
            PlayerSource.isPause = false;

            //开启服务
            Mp3Info mp3Info = PlayerSource.mp3InfoList.get(PlayerSource.music_position);
            Intent intent = new Intent();
            intent.putExtra("url", mp3Info.url);
            intent.putExtra("MSG", AppConstant.PlayerMsg.PREVIOUS_MSG);
            intent.setAction("com.mindjet.media.MUSIC_SERVICE");
            intent.setPackage("com.mindjet.com.musicplayer");
            startService(intent);

            change_icon_state();
            play_scale_anim(btn_pre);

        } else {

            Toast.makeText(this, "没有上一首了", Toast.LENGTH_SHORT).show();

        }

    }

    //处理 下一首 按钮的点击事件
    private void next() {

        if (PlayerSource.music_position + 1 <= PlayerSource.mp3InfoList.size() - 1) {

            //初始化各参数与界面
            PlayerSource.music_position += 1;
            PlayerSource.isFirstTime = false;
            PlayerSource.isPlaying = true;
            PlayerSource.isPause = false;

            //开启服务
            Mp3Info mp3Info = PlayerSource.mp3InfoList.get(PlayerSource.music_position);
            Intent intent = new Intent();
            intent.putExtra("url", mp3Info.url);
            intent.putExtra("MSG", AppConstant.PlayerMsg.NEXT_MSG);
            intent.setAction("com.mindjet.media.MUSIC_SERVICE");
            intent.setPackage("com.mindjet.com.musicplayer");
            startService(intent);

            change_icon_state();
            play_scale_anim(btn_next);

        } else {

            Toast.makeText(this, "没有下一首了", Toast.LENGTH_SHORT).show();

        }

    }

    //跳转到 LyricActivity
    private void redirect() {

        Mp3Info mp3Info = PlayerSource.mp3InfoList.get(PlayerSource.music_position);
        Intent intent = new Intent(PlayerActivity.this, LyricActivity.class);

        intent.putExtra("title", mp3Info.title);
        intent.putExtra("artist", mp3Info.artist);
        intent.putExtra("duration", MediaUtil.formatTime(mp3Info.duration));
        intent.setPackage("com.mindjet.com.musicplayer");

        //第一次启动，需要将歌词索引传进去给 lrcView
        if (LyricActivity.lrcView == null) {
            intent.putExtra("index", PlayerService.index);
        }

        startActivity(intent);

    }

    //更改按键的状态
    private void change_icon_state(){

        //改变播放暂停按钮的状态
        if (PlayerSource.isPause){
            btn_play.setBackgroundResource(R.mipmap.play_continue);
        }else if (PlayerSource.isPlaying){
            btn_play.setBackgroundResource(R.mipmap.pause);
        }

        //改变播放模式控制按钮的状态
        switch (PlayerSource.mode){

            case 1:
                btn_repeat.setBackgroundResource(R.mipmap.normal);
                btn_shuffle.setBackgroundResource(R.mipmap.shuffle);
                break;
            case 2:
                btn_repeat.setBackgroundResource(R.mipmap.repeat);
                btn_shuffle.setBackgroundResource(R.mipmap.shuffle);
                break;
            case 3:
                btn_repeat.setBackgroundResource(R.mipmap.repeat);
                btn_shuffle.setBackgroundResource(R.mipmap.normal);
                break;

        }

    }

    //缩放动画
    private void play_scale_anim(Button btn) {

        ObjectAnimator.ofFloat(btn, "scaleX",1f,0.9f).start();
        ObjectAnimator.ofFloat(btn,"scaleY",1f,0.9f).start();
        ObjectAnimator.ofFloat(btn, "scaleX",0.9f,1f).start();
        ObjectAnimator.ofFloat(btn,"scaleY",0.9f,1f).start();

    }

    //更新专辑封面
    private void update_album(){

        Mp3Info mp3Info = PlayerSource.mp3InfoList.get(PlayerSource.music_position);
        Bitmap bitmap = MediaUtil.getAlbum(getApplicationContext(),mp3Info.id,mp3Info.album_id,true);
        album.setImageBitmap(bitmap);

    }

    /*-----------------------------------------监听器 与 广播接收器-------------------------------------------*/

    //listView 的项目监听器，点击后开启播放服务
    private class MusicListItemClickListener implements android.widget.AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (PlayerSource.mp3InfoList != null) {

                Mp3Info mp3Info = PlayerSource.mp3InfoList.get(position);
                Intent intent = new Intent();
                intent.putExtra("url", mp3Info.url);
                intent.putExtra("MSG", AppConstant.PlayerMsg.PLAY_MSG);
                intent.setAction("com.mindjet.media.MUSIC_SERVICE");
                intent.setPackage("com.mindjet.com.musicplayer");
                startService(intent);

                PlayerSource.music_position = position;
                PlayerSource.isFirstTime = false;
                PlayerSource.isPlaying = true;
                PlayerSource.isPause = false;
                change_icon_state();
                update_album();
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
                    redirect();
                    break;

                case R.id.repeat:
                    //TODO 后期与 lyricactivity相同方法包装
                    if (PlayerSource.mode != 1) {
                        PlayerSource.mode = 1;
                        Toast.makeText(PlayerActivity.this, "当前播放模式：单曲循环", Toast.LENGTH_SHORT).show();
                    } else if (PlayerSource.mode == 1) {
                        PlayerSource.mode = 2;
                        Toast.makeText(PlayerActivity.this, "当前播放模式：顺序播放", Toast.LENGTH_SHORT).show();
                    }
                    change_icon_state();
                    play_scale_anim(btn_repeat);

                    break;

                case R.id.shuffle:

                    //TODO 后期与 lyricactivity相同方法包装
                    if (PlayerSource.mode != 3) {
                        PlayerSource.mode = 3;
                        Toast.makeText(PlayerActivity.this, "当前播放模式：随机播放", Toast.LENGTH_SHORT).show();
                    } else if (PlayerSource.mode == 3) {
                        PlayerSource.mode = 2;
                        Toast.makeText(PlayerActivity.this, "当前播放模式：顺序播放", Toast.LENGTH_SHORT).show();
                    }
                    change_icon_state();
                    play_scale_anim(btn_shuffle);

                    break;

            }

        }

    }

    //长按音乐列表项的监听事件，
    class MyItemLongClickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(100);

            CustomDialog dialog = new CustomDialog();
            Mp3Info info = PlayerSource.mp3InfoList.get(position);
            dialog.setStr_album(info.album);
            dialog.setStr_year(info.year);
            dialog.setStr_name(info.title);
            dialog.setStr_artist(info.artist);
            dialog.setStr_duration(MediaUtil.formatTime(info.duration));
            dialog.setStr_location(info.url);
            dialog.show(getFragmentManager(),"SHOW_INFO_PANEL");

            return true;
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
            intent.setPackage("com.mindjet.com.musicplayer");
            startService(intent);

        }
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

                String name = intent.getStringExtra("title");
                song_name.setText(name);
            }

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
