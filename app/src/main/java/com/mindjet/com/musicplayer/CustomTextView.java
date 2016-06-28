package com.mindjet.com.musicplayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @author Mindjet
 * @date 2016/6/2
 */
public class CustomTextView extends TextView {


    public CustomTextView(Context context) {
        super(context,null);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode()) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomTextView);

            setText(array.getString(R.styleable.CustomTextView_textContent));
            setTextColor(array.getColor(R.styleable.CustomTextView_textColor, Color.parseColor("#390")));
            setTextSize(array.getDimension(R.styleable.CustomTextView_textSize, 20));

            array.recycle();
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {



    }
}
