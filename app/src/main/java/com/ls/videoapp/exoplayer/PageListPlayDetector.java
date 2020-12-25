package com.ls.videoapp.exoplayer;

import android.util.Pair;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 控制边滑动边播放
 */
public class PageListPlayDetector {

    private List<IPlayTarget> mTargets = new ArrayList<>();

    //正在播放的那个
    private IPlayTarget playingTarget;
    private RecyclerView mRecyclerView;

    public void addTarget(IPlayTarget target){
        mTargets.add(target);
    }

    public void removeTarget(IPlayTarget target){
        mTargets.remove(target);
    }

    /**
     *
     * @param owner 监听宿主的生命周期吧，在宿主的onDestory()中做一下清理和反注册工作
    * @param recyclerView 监听列表的滚动
     */
    public PageListPlayDetector(LifecycleOwner owner, RecyclerView recyclerView){
        mRecyclerView = recyclerView;

        //监听宿主的生命周期
        owner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                //宿主每次声明周期的变化，都会回调到这里来
                if (event == Lifecycle.Event.ON_DESTROY){
                    playingTarget = null;
                    mTargets.clear();
                    //如果是Destroy，就需要执行反注册和清理的工作
                    recyclerView.getAdapter().unregisterAdapterDataObserver(mDataObserver);
                    owner.getLifecycle().removeObserver(this);
                }
            }
        });
        //注册监听，recyclerView的数据变化时，进行监听
        recyclerView.getAdapter().registerAdapterDataObserver(mDataObserver);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE){//当滚动停止之后，任然要触发自动播放
                    autoPlay();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dx == 0 && dy == 0) {
                    //时序问题。当执行了AdapterDataObserver#onItemRangeInserted  可能还没有被布局到RecyclerView上。
                    //所以此时 recyclerView.getChildCount()还是等于0的。
                    //等childView 被布局到RecyclerView上之后，会执行onScrolled（）方法
                    //并且此时 dx,dy都等于0
                    postAutoPlay();
                } else {
                    //如果有正在播放的,且滑动时被划出了屏幕 则 停止他
                    if (playingTarget != null && playingTarget.isPlaying() && !isTargetInBounds(playingTarget)) {
                        playingTarget.inActive();
                    }
                }
            }
        });
    }

    /**
     * 插入数据的监听
     */
    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
        }
    };

    private void postAutoPlay() {
        mRecyclerView.post(delayAutoPlay);
    }

    Runnable delayAutoPlay = new Runnable() {
        @Override
        public void run() {
            autoPlay();
        }
    };

    /**
     * 自动播放
     * 条件：视频View有一半在屏幕内，我们就自动播放
     */
    private void autoPlay() {
        if (mTargets.size() <= 0 || mRecyclerView.getChildCount() <= 0) {
            return;
        }

        if (playingTarget != null && playingTarget.isPlaying() && isTargetInBounds(playingTarget)) {
            return;
        }

        IPlayTarget activeTarget = null;
        for (IPlayTarget target : mTargets) {

            boolean inBounds = isTargetInBounds(target);
            if (inBounds) {
                activeTarget = target;
                break;
            }
        }

        if (activeTarget != null) {
            if (playingTarget != null) {
                playingTarget.inActive();
            }
            playingTarget = activeTarget;
            activeTarget.onActive();
        }
    }

    /**
     * 检测 IPlayTarget 所在的 viewGroup 是否至少还有一半的大小在屏幕内
     *
     * @param target
     * @return
     */
    private boolean isTargetInBounds(IPlayTarget target) {
        ViewGroup owner = target.getOwner();
        ensureRecyclerViewLocation();
        if (!owner.isShown() || !owner.isAttachedToWindow()) {
            return false;
        }

        int[] location = new int[2];
        owner.getLocationOnScreen(location);

        int center = location[1] + owner.getHeight() / 2;

        //承载视频播放画面的ViewGroup它需要至少一半的大小 在RecyclerView上下范围内
        return center >= rvLocation.first && center <= rvLocation.second;
    }

    private Pair<Integer, Integer> rvLocation = null;

    private Pair<Integer, Integer> ensureRecyclerViewLocation() {
        if (rvLocation == null) {
            int[] location = new int[2];
            mRecyclerView.getLocationOnScreen(location);

            int top = location[1];
            int bottom = top + mRecyclerView.getHeight();

            rvLocation = new Pair(top, bottom);
        }
        return rvLocation;
    }

    public void onPause() {
        if (playingTarget != null) {
            playingTarget.inActive();
        }
    }

    public void onResume() {
        if (playingTarget != null) {
            playingTarget.onActive();
        }
    }
}
