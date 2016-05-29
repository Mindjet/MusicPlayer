package com.mindjet.com.musicplayer.Utils;

/**
 * Created by Mindjet on 2016/5/20.
 */


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.mindjet.com.musicplayer.ItemBean.LrcContent;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义绘画歌词，产生滚动效果
 *
 */
public class LrcView extends android.widget.TextView {

    private float width;		//歌词视图宽度
    private float height;		//歌词视图高度
    private Paint currentPaint;	//当前画笔对象
    private Paint notCurrentPaint;	//非当前画笔对象
    private float textHeight = 25;	//文本高度
    private float textSize = 20;		//文本大小
    private int index = 0;		//list集合下标


    private static List<LrcContent> mLrcList = new ArrayList<LrcContent>();

    public void setmLrcList(List<LrcContent> mLrcList) {
        LrcView.mLrcList = mLrcList;
    }

    public LrcView(Context context) {
        super(context);
        init();
    }
    public LrcView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public LrcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setFocusable(true);		//设置可对焦


        //高亮部分
        currentPaint = new Paint();
        currentPaint.setAntiAlias(true);	//设置抗锯齿，让文字美观饱满
        currentPaint.setTextAlign(Paint.Align.CENTER);//设置文本对齐方式

        //非高亮部分
        notCurrentPaint = new Paint();
        notCurrentPaint.setAntiAlias(true);
        notCurrentPaint.setTextAlign(Paint.Align.CENTER);

        width=getWidth();
        height=getHeight();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 绘画歌词
     */

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(canvas == null) {
            return;
        }

        currentPaint.setColor(Color.parseColor("#FF6231"));
        notCurrentPaint.setColor(Color.GRAY);

        currentPaint.setTextSize(25);
        currentPaint.setTypeface(Typeface.SERIF);

        notCurrentPaint.setTextSize(textSize);
        notCurrentPaint.setTypeface(Typeface.DEFAULT);

        try {

            setText("");
            canvas.drawText(mLrcList.get(index).getLrcStr(),width/2,height/2,currentPaint);
            float tempY = height / 2;
            //画出本句之前的句子
            for(int i = index - 1; i >= 0; i--) {
                //向上推移
                tempY = tempY - textHeight-10;
                canvas.drawText(mLrcList.get(i).getLrcStr(), width / 2, tempY, notCurrentPaint);
            }
            tempY = height /2;
            //画出本句之后的句子
            for(int i = index + 1; i < mLrcList.size(); i++) {
                //往下推移
                tempY = tempY + textHeight+10;
                canvas.drawText(mLrcList.get(i).getLrcStr(), width / 2, tempY, notCurrentPaint);
            }

//            canvas.drawText(mLrcList.get(index).getLrcStr(), width / 2, height / 2, currentPaint);
//
//
//            float tempY = height / 2;
//            //画出本句之前的句子
//            for(int i = index - 1; i >= 0; i--) {
//                //向上推移
//                tempY = tempY - textHeight;
//                canvas.drawText(mLrcList.get(i).getLrcStr(), width / 2, tempY, notCurrentPaint);
//            }
//            tempY = height / 2;
//            //画出本句之后的句子
//            for(int i = index + 1; i < mLrcList.size(); i++) {
//                //往下推移
//                tempY = tempY + textHeight;
//                canvas.drawText(mLrcList.get(i).getLrcStr(), width / 2, tempY, notCurrentPaint);
//            }
//            invalidate();

        } catch (Exception e) {
            setText("...木有歌词文件，赶紧去下载...");
        }
    }

    /**
     * 当view大小改变的时候调用的方法
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}

