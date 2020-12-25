package com.ls.videoapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ls.libnavannotation.FragmentDestination;
import com.ls.videoapp.BR;
import com.ls.videoapp.R;
import com.ls.videoapp.exoplayer.PageListPlayDetector;
import com.ls.videoapp.model.Feed;
import com.ls.videoapp.ui.AbsListFragment;
import com.ls.videoapp.ui.MutableDataSource;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.List;

import static com.ls.videoapp.BR.feed;

@FragmentDestination(pagerUrl = "main/tabs/home", asStarter = true)
public class HomeFragment extends AbsListFragment<Feed, HomeViewModel> {

    private PageListPlayDetector mPlayDetector;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mViewModel.getCacheLiveData().observe(getViewLifecycleOwner(), new Observer<PagedList<Feed>>() {
            @Override
            public void onChanged(PagedList<Feed> feeds) {//这里的feeds就是我们的缓存数据
                //这里我们就成功的将缓存数据添加到了列表上
                submitList(feeds);
            }
        });
        mPlayDetector = new PageListPlayDetector(this, mRecyclerView);
    }

    @Override
    public PagedListAdapter getAdapter() {
        String feedType = getArguments() == null ? "all" : getArguments().getString("feedType");
        return new FeedAdapter(getContext(), feedType){
            @Override
            public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
                //当有新的item滚动到界面上时，如果他是视频，就把他添加到mPlayDetector中
                super.onViewAttachedToWindow(holder);
                if (holder.isVideoItem()){
                    //只要是视频类型的，都会添加到mPlayDetector里面，参与视频自动播放
                    mPlayDetector.addTarget( holder.getListPlayerView());
                }
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
                //Item被划出屏幕的时候，如果他是视频，需要把它从 mPlayDetector移除
                super.onViewDetachedFromWindow(holder);
                if (holder.isVideoItem()){
                    mPlayDetector.removeTarget( holder.getListPlayerView());
                }
            }
        };
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        final PagedList<Feed> currentList = mAdapter.getCurrentList();
        if (currentList == null || currentList.size() <= 0) {
            finishRefresh(false);
            return;
        }
        //手动处理下拉分页的逻辑
        Feed feed = mAdapter.getCurrentList().get(mAdapter.getItemCount() - 1);//得到最后一个Item
        mViewModel.loadAfter(feed.getId(), new ItemKeyedDataSource.LoadCallback<Feed>() {
            @Override
            public void onResult(@NonNull List<Feed> data) {
                PagedList.Config config = currentList.getConfig();
                //加载的数据返回回来
                if (data != null && data.size() > 0) {
                    //这里 咱们手动接管 分页数据加载的时候 使用MutableItemKeyedDataSource也是可以的。
                    //由于当且仅当 paging不再帮我们分页的时候，我们才会接管。所以 就不需要ViewModel中创建的DataSource继续工作了，所以使用
                    //MutablePageKeyedDataSource也是可以的
                    MutableDataSource dataSource = new MutableDataSource();
                    dataSource.data.addAll(currentList);
                    dataSource.data.addAll(data);
                    PagedList pagedList = dataSource.buildNewPagedList(config);
                    //给列表添加数据
                    submitList(pagedList);
                }
            }
        });
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        //invalidate 之后Paging会重新创建一个DataSource 重新调用它的loadInitial方法加载初始化数据
        //再次使用Paging触发页面的初始化数据的加载
        mViewModel.getDataSource().invalidate();
    }

    @Override
    public void onPause() {
        mPlayDetector.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        //和Fragment的生命周期绑定
        mPlayDetector.onResume();
        super.onResume();
    }
}
