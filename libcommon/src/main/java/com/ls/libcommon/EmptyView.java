package com.ls.libcommon;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EmptyView extends LinearLayout {

    private ImageView mIcon;
    private TextView mTitle;
    private Button mAction;

    public EmptyView(@NonNull Context context) {
        this(context,null);
    }

    public EmptyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public EmptyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_empty_view,this,true);

        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        mIcon = findViewById(R.id.empty_icon);
        mTitle = findViewById(R.id.empty_text);
        mAction = findViewById(R.id.empty_action);
    }

    public void setEmptyIcon(int iconRes){
        mIcon.setImageResource(iconRes);
    }

    public void setTitle(String text){
        if (TextUtils.isEmpty(text)){
            mTitle.setVisibility(View.GONE);
        }else{
            mTitle.setText(text);
            mTitle.setVisibility(View.VISIBLE);
        }
    }

    public void setButton(String text,View.OnClickListener listener){
        if (TextUtils.isEmpty(text)){
            mAction.setVisibility(View.GONE);
        }else{
            mAction.setText(text);
            mAction.setVisibility(View.VISIBLE);
            mAction.setOnClickListener(listener);
        }
    }
}
