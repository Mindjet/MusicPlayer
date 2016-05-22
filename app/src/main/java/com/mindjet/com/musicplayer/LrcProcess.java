package com.mindjet.com.musicplayer;

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

    private List<LrcContent> lrcContentList = new ArrayList<LrcContent>();

    /**
     * 从 .lrc 文件中读取并解析歌词
     * <p/>
     * 歌词样式：
     * [00:02.32]雅俗共赏
     * [00:03.43]许嵩
     * [00:05.22]歌词制作  mindjet
     *
     * @param lrcPath
     */
    public String readLRC(String lrcPath) {

        StringBuilder stringBuilder = new StringBuilder();
        lrcPath = lrcPath.replace("Music","Musiclrc");
        lrcPath = lrcPath.replace(".mp3",".lrc");

        File file = new File(lrcPath);

        try {

            FileInputStream in = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in,"utf-8"));
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

        return stringBuilder.toString();

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
