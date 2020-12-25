package com.ls.videoapp.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.ls.videoapp.R;
import com.ls.libcommon.PixUtils;
import com.ls.videoapp.exoplayer.IPlayTarget;
import com.ls.videoapp.exoplayer.PageListPlay;
import com.ls.videoapp.exoplayer.PageListPlayManager;

/**
 * 列表视频播放专用
 */
public class ListPlayerView extends FrameLayout implements IPlayTarget, Player.EventListener, PlayerControlView.VisibilityListener {

    private View mBufferView;
    private CustomImageView mCover, mBlur;
    private ImageView mPlayBtn;
    private String mCategory;
    protected boolean isPlaying;
    private int mWidthPx;
    private int mHeightPx;
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

        //缓冲转圈圈的view
        mBufferView = findViewById(R.id.buffer_view);
        //封面view
        mCover = findViewById(R.id.cover);
        //高斯模糊背景图,防止出现两边留黑
        mBlur = findViewById(R.id.blur_background);
        //播放盒暂停的按钮
        mPlayBtn = findViewById(R.id.play_btn);

        mPlayBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (isPlaying()){
                    inActive();
                }else{
                    onActive();
                }
            }
        });

        this.setTransitionName("listPlayerView");
    }

    /**
     * 监听触摸事件
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //点击该区域时 我们主动让视频控制器显示出来
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        pageListPlay.mControlView.show();
        return true;
    }

    /**
     * 因为dataBanding使用时，会有一帧的延迟，所以我们直接采用原生的数据绑定方式
     * <p>
     * 因为videoUrl不是马上就用的，要滑动到他的时候才用，所以我们先保存
     */
    public void bindData(String category, int widthPx, int heightPx, String coverUrl, String videoUrl) {
        mCategory = category;
        mVideoUrl = videoUrl;
        mWidthPx = widthPx;
        mHeightPx = heightPx;

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

    @Override
    public ViewGroup getOwner() {
        return this;
    }

    @Override
    public void onActive() {
        //视频播放,或恢复播放

        //通过该View所在页面的mCategory(比如首页列表tab_all,沙发tab的tab_video,标签帖子聚合的tag_feed) 字段，
        //取出管理该页面的Exoplayer播放器，ExoplayerView播放View,控制器对象PageListPlay
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        PlayerView playerView = pageListPlay.mPlayerView;
        PlayerControlView controlView = pageListPlay.mControlView;
        SimpleExoPlayer exoPlayer = pageListPlay.mExoPlayer;
        if (playerView == null) {
            return;
        }

        //此处我们需要主动调用一次 switchPlayerView，把播放器Exoplayer和展示视频画面的View ExoplayerView相关联
        //为什么呢？因为在列表页点击视频Item跳转到视频详情页的时候，详情页会复用列表页的播放器Exoplayer，
        // 然后和新创建的展示视频画面的View ExoplayerView相关联，达到视频无缝续播的效果
        //如果 我们再次返回列表页，则需要再次把播放器和ExoplayerView相关联
        pageListPlay.switchPlayerView(playerView, true);
        ViewParent parent = playerView.getParent();
        if (parent != this) {

            //把展示视频画面的View添加到ItemView的容器上
            if (parent != null) {
                ((ViewGroup) parent).removeView(playerView);
                //还应该暂停掉列表上正在播放的那个
                ((ListPlayerView) parent).inActive();
            }

            ViewGroup.LayoutParams coverParams = mCover.getLayoutParams();
            this.addView(playerView, 1, coverParams);
        }

        ViewParent ctrlParent = controlView.getParent();
        if (ctrlParent != this) {
            //把视频控制器 添加到ItemView的容器上
            if (ctrlParent != null) {
                ((ViewGroup) ctrlParent).removeView(controlView);
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM;
            this.addView(controlView, params);
        }

        //如果是同一个视频资源,则不需要从重新创建mediaSource。
        //但需要onPlayerStateChanged 否则不会触发onPlayerStateChanged()
        if (TextUtils.equals(pageListPlay.playUrl, mVideoUrl)) {
            onPlayerStateChanged(true, Player.STATE_READY);
        } else {
            MediaSource mediaSource = PageListPlayManager.createMediaSource(mVideoUrl);
            exoPlayer.prepare(mediaSource);
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            pageListPlay.playUrl = mVideoUrl;
        }
        controlView.show();
        controlView.setVisibilityListener(this);
        exoPlayer.addListener(this);
        exoPlayer.setPlayWhenReady(true);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isPlaying = false;
        mBufferView.setVisibility(GONE);
        mCover.setVisibility(VISIBLE);
        mPlayBtn.setVisibility(VISIBLE);
        mPlayBtn.setImageResource(R.drawable.icon_video_play);
    }

    @Override
    public void inActive() {

        //暂停视频的播放并让封面图和 开始播放按钮 显示出来
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        if (pageListPlay.mPlayerView == null || pageListPlay.mControlView == null || pageListPlay.mExoPlayer == null)
            return;
        pageListPlay.mExoPlayer.setPlayWhenReady(false);
        pageListPlay.mControlView.setVisibilityListener(null);
        pageListPlay.mExoPlayer.removeListener(this);
        mCover.setVisibility(VISIBLE);
        mPlayBtn.setVisibility(VISIBLE);
        mPlayBtn.setImageResource(R.drawable.icon_video_play);
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void onVisibilityChange(int visibility) {
        mPlayBtn.setVisibility(visibility);
        mPlayBtn.setImageResource(isPlaying() ? R.drawable.icon_video_pause : R.drawable.icon_video_play);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        //监听视频播放的状态
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        SimpleExoPlayer exoPlayer = pageListPlay.mExoPlayer;
        if (playbackState == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0 && playWhenReady) {
            mCover.setVisibility(GONE);
            mBufferView.setVisibility(GONE);
        } else if (playbackState == Player.STATE_BUFFERING) {
            mBufferView.setVisibility(VISIBLE);
        }
        isPlaying = playbackState == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0 && playWhenReady;
        mPlayBtn.setImageResource(isPlaying ? R.drawable.icon_video_pause : R.drawable.icon_video_play);
    }

    public View getPlayController() {
        PageListPlay listPlay = PageListPlayManager.get(mCategory);
        return listPlay.mControlView;
    }
}
