package com.ls.videoapp.exoplayer;

import android.view.ViewGroup;

public interface IPlayTarget {

    /**
     * 得到playerView所在的容器在哪里
     * 当我们得到了容器View，才能在列表滚动的时候，检测他的位置是否满足自动播放，然后去判断
     */
     ViewGroup getOwner();

    /**
     * 当我们检测到playerTarget满足自动播放的时候，我们就调用这个方法进行自动播放
     */
    void onActive();

    /**
     * 当playerView滚出了屏幕之后，我们就停止播放
     */
    void inActive();

    /**
     * 检测当前target是否正在播放
     * @return
     */
    boolean isPlaying();

}
