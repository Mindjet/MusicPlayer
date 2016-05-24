package com.mindjet.com.musicplayer;

import java.util.List;

/**
 *@author mindjet
 * @date 2016.5.23
 * 存放音乐播放器的状态与公用变量
 * 状态 = 播放|暂停|第一次播放|播放模式
 * 变量 = 歌曲信息列表|当前歌曲索引
 *
 */


public class PlayerState {

    public static boolean isPlaying = false;
    public static boolean isPause = true;
    public static boolean isFirstTime = true;
    public static List<Mp3Info> mp3InfoList;
    public static int music_position = 0;

    public static int mode = 2;

    /**
     * mode
     * 1----单曲循环
     * 2----列表循环
     * 3----随机播放
     */


    //TODO 将控制键的触发的方法放在这里

}
