package com.ls.libcommon;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 自定义圆角布局
 */
public class CornerFrameLayout extends FrameLayout {
    public CornerFrameLayout(@NonNull Context context) {
        this(context,null);
    }

    public CornerFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CornerFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ViewHelper.setViewOutline(this, attrs, defStyleAttr, defStyleAttr);
    }

    public void setViewOutline(int radius, int radiusSide) {
        ViewHelper.setViewOutline(this, radius, radiusSide);
    }
}
