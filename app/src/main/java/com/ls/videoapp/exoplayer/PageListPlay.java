package com.ls.videoapp.exoplayer;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.ls.libcommon.AppGlobals;
import com.ls.videoapp.R;

public class PageListPlay {

    public SimpleExoPlayer mExoPlayer;
    public PlayerView mPlayerView;
    public PlayerControlView mControlView;
    public String playUrl;

    public PageListPlay(){
        Application application = AppGlobals.getApplication();
        //创建exoplayer播放器实例
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(application,
                //视频每一这的画面如何渲染,实现默认的实现类
                new DefaultRenderersFactory(application),
                //视频的音视频轨道如何加载,使用默认的轨道选择器
                new DefaultTrackSelector(),
                //视频缓存控制逻辑,使用默认的即可
                new DefaultLoadControl());

        //加载咱们布局层级优化之后的能够展示视频画面的View
        mPlayerView = (PlayerView) LayoutInflater.from(application).inflate(R.layout.layout_exo_player_view, null, false);

        //加载咱们布局层级优化之后的视频播放控制器
        mControlView = (PlayerControlView) LayoutInflater.from(application).inflate(R.layout.layout_exo_player_contorller_view, null, false);

        //别忘记 把播放器实例 和 playerView，controlView相关联
        //如此视频画面才能正常显示,播放进度条才能自动更新
        mPlayerView.setPlayer(mExoPlayer);
        mControlView.setPlayer(mExoPlayer);
    }

    public void release() {
        if (mExoPlayer != null){
            mExoPlayer.setPlayWhenReady(false);
            mExoPlayer.stop(true);
            mExoPlayer.release();
            mExoPlayer=null;
        }

        if (mPlayerView != null){
            mPlayerView.setPlayer(null);
            mPlayerView = null;
        }

        if (mControlView != null){
            mControlView.setPlayer(null);
            mControlView.setVisibilityListener(null);
            mControlView=null;
        }
    }

    /**
     * 切换与播放器exoplayer 绑定的exoplayerView。用于页面切换视频无缝续播的场景
     *
     * @param newPlayerView
     * @param attach
     */
    public void switchPlayerView(PlayerView newPlayerView, boolean attach) {
        mPlayerView.setPlayer(attach ? null : mExoPlayer);
        newPlayerView.setPlayer(attach ? mExoPlayer : null);
    }
}
