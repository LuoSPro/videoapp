package com.ls.videoapp.ui.sofa;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.ls.libnavannotation.FragmentDestination;
import com.ls.videoapp.R;
import com.ls.videoapp.databinding.FragmentSofaBinding;
import com.ls.videoapp.model.SofaTab;
import com.ls.videoapp.ui.home.HomeFragment;
import com.ls.videoapp.utils.AppConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
@FragmentDestination(pagerUrl = "main/tabs/sofa",asStarter = true)
public class SofaFragment extends Fragment {

    private static final String TAG = "SofaFragment";

    private FragmentSofaBinding mBinding;
    private ViewPager2 mViewPager;
    private TabLayout mTabLayout;
    private SofaTab mTabConfig;
    private List<SofaTab.Tabs> mTabs;

    //创建一个Map去保存每个tab对应的Fragment，这样就避免每次选择tab都重建Fragment
    private Map<Integer,Fragment> mFragmentMap = new HashMap<>();
    private TabLayoutMediator mMediator;

    public SofaFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentSofaBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mViewPager = mBinding.viewPager;
        mTabLayout = mBinding.tabLayout;

        //对tab进行过滤，使enable=false的tabItem被过滤掉
        mTabConfig = getTabConfig();
        mTabs = new ArrayList<>();
        for (SofaTab.Tabs tab : mTabConfig.getTabs()) {
            if (tab.isEnable()){
                //enable=true的才显示出来
                mTabs.add(tab);
            }
        }

        //禁止ViewPage2的预加载效果
        mViewPager.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        mViewPager.setAdapter(new FragmentStateAdapter(getChildFragmentManager(),this.getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                //创建每个tab对应的页面
                Fragment fragment = mFragmentMap.get(position);
                if (fragment == null){
                    fragment = getTabFragment(position);
                }
                return fragment;
            }

            @Override
            public int getItemCount() {
                return mTabs.size();
            }
        });

        //将TabLayout和ViewPage关联起来
        mMediator = new TabLayoutMediator(mTabLayout, mViewPager, false, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                //创建一个自定义的TabView作为顶部的tab栏
                tab.setCustomView(makeTabView(position));
            }
        });
        //让viewPage和TabLayout联动
        mMediator.attach();

        //监听页面选中时的回调
        mViewPager.registerOnPageChangeCallback(mPageChangeCallback);

        //设置默认选中的tab，因为必须要TabLayout和ViewPager加载完之后才有效，所以我们在post里面实现
        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(mTabConfig.getSelect());
            }
        });
    }

    ViewPager2.OnPageChangeCallback mPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            //对选中的文本的大小、颜色进行改变
            int tabCount = mTabLayout.getTabCount();
            for (int i = 0; i < tabCount; i++) {
                TabLayout.Tab tab = mTabLayout.getTabAt(i);
                TextView customView = (TextView) tab.getCustomView();
                if (tab.getPosition() == position){
                    //被选中的文本
                    customView.setTextSize(mTabConfig.getActiveSize());
                    customView.setTypeface(Typeface.DEFAULT_BOLD);
                }else{
                    //未被选中的tab
                    customView.setTextSize(mTabConfig.getNormalSize());
                    customView.setTypeface(Typeface.DEFAULT);
                }
            }
            super.onPageSelected(position);
        }
    };

    private View makeTabView(int position) {
        TextView tabView = new TextView(getContext());
        //选中和未选中的文本颜色，以及文字的大小
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};

        int[] colors = new int[]{Color.parseColor(mTabConfig.getActiveColor()),Color.parseColor(mTabConfig.getNormalColor())};
        ColorStateList stateList = new ColorStateList(states, colors);
        tabView.setTextColor(stateList);
        tabView.setText(mTabs.get(position).getTitle());
        tabView.setTextSize(mTabConfig.getNormalSize());
        return tabView;
    }

    private Fragment getTabFragment(int position) {
        return HomeFragment.newInstance(mTabs.get(position).getTag());
    }

    private SofaTab getTabConfig() {
        return AppConfig.getSofaTab();
    }

    @Override
    public void onDestroyView() {
        //清理、销毁
        mMediator.detach();
        mViewPager.unregisterOnPageChangeCallback(mPageChangeCallback);
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume: ");
        super.onResume();
    }
}
