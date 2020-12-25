package com.ls.videoapp.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.ls.videoapp.databinding.LayoutFeedTypeImageBinding;
import com.ls.videoapp.databinding.LayoutFeedTypeVideoBinding;
import com.ls.videoapp.model.Feed;
import com.ls.videoapp.view.ListPlayerView;

public class FeedAdapter extends PagedListAdapter<Feed,FeedAdapter.ViewHolder> {

    private final LayoutInflater mInflater;
    private Context mContext;
    private String mCategory;

    protected FeedAdapter(Context context,String category) {
        super(new DiffUtil.ItemCallback<Feed>() {
            @Override
            public boolean areItemsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                //判断两个Item是否相同
                return false;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) {
                //判断两个Item的内容是否相同
                return false;
            }
        });
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mCategory = category;
    }

    @Override
    public int getItemViewType(int position) {
        Feed feed = getItem(position);
        return feed.getItemType();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding;
        if (viewType == Feed.TYPE_IMAGE){//图片类型的Item
            binding = LayoutFeedTypeImageBinding.inflate(mInflater);
        }else{
            binding = LayoutFeedTypeVideoBinding.inflate(mInflater);
        }
        return new ViewHolder(binding.getRoot(),binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindData(getItem(position));
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding mBinding;
        private ListPlayerView mListPlayerView;

        public ViewHolder(@NonNull View itemView, ViewDataBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Feed item) {
            if (mBinding instanceof LayoutFeedTypeImageBinding){
                //图片
                LayoutFeedTypeImageBinding imageBinding = (LayoutFeedTypeImageBinding) mBinding;
                //往布局里面设置数据
                imageBinding.setFeed(item);
                imageBinding.feedImage.bindData(item.getWidth(),item.getHeight(),16,item.getCover());
                //给布局里面设置数据进去，因为布局里面需要这些参数
                imageBinding.setLifecycleOwner((LifecycleOwner)mContext);
            }else{
                //视频
                LayoutFeedTypeVideoBinding videoBinding = (LayoutFeedTypeVideoBinding) mBinding;
                //往布局里面设置数据
                videoBinding.setFeed(item);
                videoBinding.listPlayerView.bindData(mCategory,item.getWidth(),item.getHeight(),item.getUrl(),item.getUrl());
                videoBinding.setLifecycleOwner((LifecycleOwner)mContext);

                mListPlayerView = videoBinding.listPlayerView;
            }
        }

        public boolean isVideoItem(){
            return mBinding instanceof LayoutFeedTypeVideoBinding;
        }

        public ListPlayerView getListPlayerView() {
            return mListPlayerView;
        }
    }
}
