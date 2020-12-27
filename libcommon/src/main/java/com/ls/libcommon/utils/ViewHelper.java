package com.ls.libcommon.utils;

import android.content.res.TypedArray;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.ls.libcommon.R;

/**
 * 解析自定义属性的工具类
 * 因为我们除了把FrameLayout设置成圆角，还可能把RelativeLayout、LinearLayout等设置成圆角
 */
public class ViewHelper {

    public static final int RADIUS_ALL = 0;
    public static final int RADIUS_LEFT = 0;
    public static final int RADIUS_TOP = 0;
    public static final int RADIUS_RIGHT = 0;
    public static final int RADIUS_BOTTOM = 0;

    public static void setViewOutline(View view, AttributeSet attributeSet,int defStyleAttr,int defStyleResource){
        TypedArray typedArray = view.getContext().obtainStyledAttributes(attributeSet, R.styleable.viewOutLineStrategy, defStyleAttr, defStyleAttr);
        int radius = typedArray.getDimensionPixelOffset(R.styleable.viewOutLineStrategy_clip_radius, 0);
        int radiusSide = typedArray.getIndex(R.styleable.viewOutLineStrategy_clip_side);
        typedArray.recycle();

        setViewOutline(view,radius,radiusSide);
    }

    public static void setViewOutline(View view,final int radius, final int radiusSide) {
        if (radius <= 0){//边角小于0，就return
            return;
        }
        //使用setOutlineProvider去改变圆角
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int width = view.getWidth();
                int height = view.getHeight();

                if (width <= 0 || height <= 0){
                    return;
                }

                //如果边角不全为圆角
                if (radiusSide != RADIUS_ALL) {
                    int left = 0, top = 0, right = width, bottom = height;
                    //剪裁的为左边
                    if (radiusSide == RADIUS_LEFT) {
                        right += radius;
                    } else if (radiusSide == RADIUS_TOP) {////剪裁的为顶部
                        bottom += radius;
                    } else if (radiusSide == RADIUS_RIGHT) {//剪裁的为右边
                        left -= radius;
                    } else if (radiusSide == RADIUS_BOTTOM) {//剪裁的为底部
                        top -= radius;
                    }
                    outline.setRoundRect(left, top, right, bottom, radius);
                    return;
                }
                int top = 0, bottom = height, left = 0, right = width;
                if (radius <= 0) {
                    outline.setRect(left, top, right, bottom);
                } else {
                    outline.setRoundRect(left, top, right, bottom, radius);
                }
            }
        });

    }

}
