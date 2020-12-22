package com.ls.videoapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ls.libnavannotation.FragmentDestination;
import com.ls.videoapp.R;
import com.ls.videoapp.model.Feed;
import com.ls.videoapp.ui.AbsListFragment;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

@FragmentDestination(pagerUrl = "main/tabs/home",asStarter = true)
public class HomeFragment extends AbsListFragment<Feed,HomeViewModel> {

    @Override
    protected void afterCreateView() {

    }

    @Override
    public PagedListAdapter getAdapter() {
        String feedType = getArguments()==null?"all":getArguments().getString("feedType");
        return new FeedAdapter(getContext(),feedType);
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {

    }
}
