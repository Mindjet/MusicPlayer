package com.mindjet.com.musicplayer.Constant;


/**
 * @author mindjet
 * @date 2016.5.19
 * 存放常量
 * MSG = activity对service发送intent携带的消息
 * intentFilter的filter
 */

public class AppConstant {

    public class PlayerMsg{

        public static final int PLAY_MSG = 1;		//播放
        public static final int PAUSE_MSG = 2;		//暂停
        public static final int STOP_MSG = 3;		//停止
        public static final int CONTINUE_MSG = 4;	//继续
        public static final int PREVIOUS_MSG = 5;	//上一首
        public static final int NEXT_MSG = 6;		//下一首
        public static final int PROGRESS_CHANGE = 7;//进度改变

    }

    public class ActionMsg{

        public static final String MUSIC_CURRENT = "com.mindjet.action.MUSIC_CURRENT";  //发送当前播放时间，更新进度条
        public static final String UPDATE_TITLE = "com.mindjet.action.UPDATE_TITLE";    //更新播放标题

    }
}
