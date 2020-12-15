package com.ls.videoapp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.ls.videoapp.R;
import com.ls.videoapp.model.BottomBar;
import com.ls.videoapp.model.Destination;
import com.ls.videoapp.utils.AppConfig;

import java.util.List;

/**
 * 我们使用这种去解析json的方式，可以动态的改变底部按钮，而json文件可以从后台获取，这就有利于
 * 不同权限的用户看见的底部导航栏不一样：如：是否登录，是否有会员等
 */
public class AppBottomBar extends BottomNavigationView {

    private static int[] sIcons = new int[]{
            R.drawable.icon_tab_home,
            R.drawable.icon_tab_sofa,
            R.drawable.icon_tab_publish,
            R.drawable.icon_tab_find,
            R.drawable.icon_tab_mine};

    public AppBottomBar(@NonNull Context context) {
        this(context,null);
    }

    public AppBottomBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    @SuppressLint("RestrictedApi")
    public AppBottomBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //从资源文件中解析出bar信息，和布局中的：  app:menu="@menu/bottom_nav_menu" 作用类似
        BottomBar bottomBar = AppConfig.getBottomBar();
        List<BottomBar.Tabs> tabs = bottomBar.getTabs();

        //使用二维数组保存底部按钮选中和未选中的状态
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};

        //一维数组保存colors
        int[] colors = new int[]{Color.parseColor(bottomBar.getActiveColor()),Color.parseColor(bottomBar.getInActiveColor())};
        ColorStateList colorStateList = new ColorStateList(states, colors);

        setItemIconTintList(colorStateList);
        setItemTextColor(colorStateList);
        //什么时候都显示按钮文本
        setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        setSelectedItemId(bottomBar.getSelectTab());

        for (int i = 0; i < tabs.size(); i++) {
            BottomBar.Tabs tab = tabs.get(i);
            //如果按钮为enable，则不显示
            if (!tab.isEnable()){
                continue;
            }
            int id = getId(tab.getPageUrl());
            if (id < 0 ){
                continue;
            }
            //给BottomNavigationView添加item
            MenuItem item = getMenu().add(0, id, tab.getIndex(), tab.getTitle());
            //设置item的icon，但这里还设置不了icon的大小
            item.setIcon(sIcons[tab.getIndex()]);
        }

        //因为BottomNavigationView需要等itemView添加完之后，才能给icon设置大小
        for (int i = 0; i < tabs.size(); i++) {
            BottomBar.Tabs tab = tabs.get(i);
            int iconSize = dp2px(tab.getSize());
            //BottomNavigationView没有提供直接的方法供我们去修改icon的大小，但是他里面的
            //BottomNavigationMenuView就提过改变每个icon大小的接口：setItemIconSize
            //所以我们从BottomNavigationMenuView入手，他就是BottomNavigationView的第一个子View
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) getChildAt(0);
            BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(tab.getIndex());
            if (itemView == null){
                continue;
            }
            itemView.setIconSize(iconSize);

            if (TextUtils.isEmpty(tab.getTitle())){
                itemView.setIconTintList(ColorStateList.valueOf(Color.parseColor(tab.getTintColor())));
                //不让按钮有点击时的浮动效果
                itemView.setShifting(false);
            }
        }
    }

    private int dp2px(int size) {
        float value = getContext().getResources().getDisplayMetrics().density * size + 0.5f;

    return (int) value;
    }

    private int getId(String pageUrl) {
        Destination destination = AppConfig.getDestConfig().get(pageUrl);
        if (destination == null){
            return -1;
        }
        return destination.getId();
    }
}
