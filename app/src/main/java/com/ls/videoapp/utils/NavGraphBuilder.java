package com.ls.videoapp.utils;

import android.content.ComponentName;

import androidx.fragment.app.FragmentActivity;
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.FragmentNavigator;

import com.ls.libcommon.AppGlobals;
import com.ls.videoapp.navigator.FixFragmentNavigator;
import com.ls.videoapp.model.Destination;

import java.util.HashMap;

public class NavGraphBuilder {

    public static void build(NavController controller, FragmentActivity activity, int containerId){
        NavigatorProvider provider = controller.getNavigatorProvider();
        //取出NavigatorProvider中HashMap的对象
//        FragmentNavigator fragmentNavigator = provider.getNavigator(FragmentNavigator.class);
        //上面采用的是系统内置的navigator，内部navigate方法切换fragment的时候采用的是replace方法，对系统的资源消耗很大
        //所以我们采用自定义navigator，在重写的navigate方法里面，采用hide和show方法去切换fragment
        FixFragmentNavigator fragmentNavigator = new FixFragmentNavigator(activity,activity.getSupportFragmentManager(),containerId);
        //除了系统的内置的4中navigator，现在我们添加了第五种自定义navigator
        provider.addNavigator(fragmentNavigator);

        ActivityNavigator activityNavigator = provider.getNavigator(ActivityNavigator.class);

        NavGraph navGraph = new NavGraph(new NavGraphNavigator(provider));

        HashMap<String, Destination> destConfig = AppConfig.getDestConfig();
        for (Destination value : destConfig.values()) {
            //遍历页面节点
            if (value.isFragment()){//是fragment
                FragmentNavigator.Destination destination = fragmentNavigator.createDestination();
                destination.setClassName(value.getClazName());
                destination.setId(value.getId());
                destination.addDeepLink(value.getPageUrl());
                navGraph.addDestination(destination);
            }else{//activity节点
                ActivityNavigator.Destination destination = activityNavigator.createDestination();
                destination.setId(value.getId());
                destination.addDeepLink(value.getPageUrl());
                destination.setComponentName(new ComponentName(AppGlobals.getApplication().getPackageName(),value.getClazName()));

                navGraph.addDestination(destination);
            }

            if (value.isAsStarter()){
                navGraph.setStartDestination(value.getId());
            }
        }

        controller.setGraph(navGraph);
    }

}
