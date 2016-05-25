package com.mindjet.com.musicplayer;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Mindjet on 2016/5/20.
 */
public class LrcProcess {


    private List<LrcContent> lrcContentList;

    /**
     * 从 .lrc 文件中读取并解析歌词
     * <p/>
     * 歌词样式：
     * [00:02.32]雅俗共赏
     * [00:03.43]许嵩
     * [00:05.22]歌词制作  Mindjet
     */
    public String readLRC(String lrcPath) {

        StringBuilder stringBuilder = new StringBuilder();

        //从歌词集合中找出匹配歌词
        lrcPath = lrcPath.replace(".mp3", ".lrc");
        String song_name = lrcPath.substring(lrcPath.lastIndexOf("/")+1);
        File rightFile = null;

        for (int i=0;i<PlayerSource.LrcFile.size();i++){

            if (PlayerSource.LrcFile.get(i).getPath().contains(song_name)){
                rightFile = PlayerSource.LrcFile.get(i);
                break;
            }

        }

        lrcContentList = new ArrayList<>();

        if (rightFile!=null) {

            try {

                FileInputStream in = new FileInputStream(rightFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
                String str = "";

                while ((str = reader.readLine()) != null) {

                    str = str.replace("[", "");
                    str = str.replace("]", "@");

                    if (str.contains("x-trans")) continue;

                    String[] splitedData = str.split("@");

                    if (splitedData.length > 1) {

                        LrcContent content = new LrcContent();

                        content.setLrcStr(splitedData[1]);
                        content.setLrcTime(str2time(splitedData[0]));
                        lrcContentList.add(content);

                    }

                }

                reader.close();
                in.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                stringBuilder.append("没有歌词文件");
            } catch (IOException e) {
                e.printStackTrace();
                stringBuilder.append("读取错误");
            }
        }else {

            return stringBuilder.toString();
        }

        return null;

    }

    private int str2time(String s) {

        s = s.replace(":", "@");
        s = s.replace(".", "@");

        String[] timeData = s.split("@");

        int min = Integer.parseInt(timeData[0]);
        int sec = Integer.parseInt(timeData[1]);
        int milli = Integer.parseInt(timeData[2]);

        return (min * 60 + sec) * 1000 + milli;

    }

    public List<LrcContent> getLrcContentList() {
        return lrcContentList;
    }
}
