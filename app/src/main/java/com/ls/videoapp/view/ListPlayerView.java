package com.ls.videoapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ls.videoapp.R;
import com.ls.libcommon.PixUtils;

public class ListPlayerView extends FrameLayout {

    private View mBufferView;
    private CustomImageView mCover, mBlur;
    private ImageView mPlayBtn;
    private String mCategory;
    private String mVideoUrl;

    public ListPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //因为layout_player_view的根布局是<merge></merge>标签，所以这里使用LayoutInflater的时候，要将attachToRoot属性设为true
        LayoutInflater.from(context).inflate(R.layout.layout_player_view, this, true);

        mBufferView = findViewById(R.id.buffer_view);
        mCover = findViewById(R.id.cover);
        mBlur = findViewById(R.id.blur_background);
        mPlayBtn = findViewById(R.id.play_btn);
    }

    /**
     * 因为dataBanding使用时，会有一帧的延迟，所以我们直接采用原生的数据绑定方式
     * <p>
     * 因为videoUrl不是马上就用的，要滑动到他的时候才用，所以我们先保存
     */
    public void bindData(String category, int widthPx, int heightPx, String coverUrl, String videoUrl) {
        mCategory = category;
        mVideoUrl = videoUrl;

        mCover.setImageUrl(mCover, coverUrl, false);
        if (widthPx < heightPx) {
            mBlur.setBlurImageUrl(coverUrl, 10);
            mBlur.setVisibility(VISIBLE);
        } else {
            mBlur.setVisibility(INVISIBLE);
        }
        setSize(widthPx, heightPx);
    }

    private void setSize(int widthPx, int heightPx) {
        //视频的大小，长宽都要和屏幕宽度一样
        int maxWidth = PixUtils.getScreenWidth();
        int maxHeight = maxWidth;

        //根据传进来的widthPx、heightPx处理之后得到的值
        int layoutWidth = maxWidth;
        int layoutHeight = 0;

        //封面的宽和高
        int coverWidth, coverHeight;
        if (widthPx >= heightPx) {
            coverWidth = maxWidth;
            layoutHeight = coverHeight = (int) (heightPx / (widthPx * 1.0f / maxWidth));
        } else {
            layoutHeight = coverHeight = maxHeight;
            coverWidth = (int) (widthPx / (heightPx * 1.0f / maxHeight));
        }

        //给ListPlayerView设置Params
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = layoutWidth;
        params.height = layoutHeight;
        setLayoutParams(params);

        //给Blur设置Params
        ViewGroup.LayoutParams blurParams = mBlur.getLayoutParams();
        blurParams.width = layoutWidth;
        blurParams.height = layoutHeight;
        mBlur.setLayoutParams(blurParams);

        //给Cover设置Params
        FrameLayout.LayoutParams coverParams = (LayoutParams) mCover.getLayoutParams();
        coverParams.width = coverWidth;
        coverParams.height = coverHeight;
        coverParams.gravity = Gravity.CENTER;
        mCover.setLayoutParams(coverParams);

        //给PlayBtn设置Params
        FrameLayout.LayoutParams playBtnParams = (LayoutParams) mPlayBtn.getLayoutParams();
        playBtnParams.gravity = Gravity.CENTER;
        mPlayBtn.setLayoutParams(playBtnParams);
    }
}
