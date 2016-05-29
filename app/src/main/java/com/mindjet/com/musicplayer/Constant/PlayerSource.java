package com.mindjet.com.musicplayer.Constant;

import com.mindjet.com.musicplayer.ItemBean.Mp3Info;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *@author mindjet
 * @date 2016.5.23
 * 存放音乐播放器的状态与公用变量
 * 状态 = 播放|暂停|第一次播放|播放模式
 * 变量 = 歌曲信息列表|当前歌曲索引|所有歌词文件
 *
 */


public class PlayerSource {

    public static boolean isPlaying = false;
    public static boolean isPause = true;
    public static boolean isFirstTime = true;
    public static boolean isServiceOnline = false;

    //所有歌曲集合
    public static List<Mp3Info> mp3InfoList;
    public static int music_position = 0;

    //所有歌词文件
    public static List<File> LrcFile = new ArrayList<>();

    public static int mode = 2;

    /**
     * mode
     * 1----单曲循环
     * 2----列表循环
     * 3----随机播放
     */


    public static void getLrcFile(File root){

        File files[] = root.listFiles();
        for (File file : files){

            if (file.isDirectory()){
                getLrcFile(file);
            }else if (file.getPath().contains(".lrc")){
                LrcFile.add(file);
            }

        }

    }


}
