package com.ls.videoapp.navigator;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigator;
import androidx.navigation.fragment.FragmentNavigator;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Map;

@Navigator.Name("fixfragment")
public class FixFragmentNavigator extends FragmentNavigator {

    private static final String TAG = "FixFragmentNavigator";

    private Context mContext;
    private FragmentManager mManager;
    private int mContainerId;

    public FixFragmentNavigator(@NonNull Context context, @NonNull FragmentManager manager, int containerId) {
        super(context, manager, containerId);
        mContext = context;
        mManager = manager;
        mContainerId = containerId;
    }

    /**
     * 由于FragmentNavigator的navigate方法在切换Fragment的时候，采用的是，replace方法，这会导致Fragment的
     * 生命周期都跑一边，但是这样太耗费性能了，所以我们重写FragmentNavigator里面的navigate方法，使用hide和show
     * 去替换replace方法
     * @param destination
     * @param args
     * @param navOptions
     * @param navigatorExtras
     * @return
     */
    @Nullable
    @Override
    public NavDestination navigate(@NonNull Destination destination, @Nullable Bundle args, @Nullable NavOptions navOptions, @Nullable Navigator.Extras navigatorExtras) {
        if (mManager.isStateSaved()) {
            Log.i(TAG, "Ignoring navigate() call: FragmentManager has already"
                    + " saved its state");
            return null;
        }
        String className = destination.getClassName();
        if (className.charAt(0) == '.') {
            className = mContext.getPackageName() + className;
        }
        //由于我们不再采用replace方法，所以我们的fragment可以重用
//        final Fragment frag = instantiateFragment(mContext, mManager,
//                className, args);
//        frag.setArguments(args);
        final FragmentTransaction ft = mManager.beginTransaction();

        int enterAnim = navOptions != null ? navOptions.getEnterAnim() : -1;
        int exitAnim = navOptions != null ? navOptions.getExitAnim() : -1;
        int popEnterAnim = navOptions != null ? navOptions.getPopEnterAnim() : -1;
        int popExitAnim = navOptions != null ? navOptions.getPopExitAnim() : -1;
        if (enterAnim != -1 || exitAnim != -1 || popEnterAnim != -1 || popExitAnim != -1) {
            enterAnim = enterAnim != -1 ? enterAnim : 0;
            exitAnim = exitAnim != -1 ? exitAnim : 0;
            popEnterAnim = popEnterAnim != -1 ? popEnterAnim : 0;
            popExitAnim = popExitAnim != -1 ? popExitAnim : 0;
            ft.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim);
        }

//        ft.replace(mContainerId, frag);
        //替代replace的方法
        //重用fragment
        Fragment fragment = mManager.getPrimaryNavigationFragment();
        if (fragment != null){
            ft.hide(fragment);
        }
        Fragment frag = null;
        //获取外部传进来的destination的id，然后去FragmentManager里面去查
        String tag = String.valueOf(destination.getId());
        frag = mManager.findFragmentByTag(tag);
        if (frag != null){//frag不为null，则直接展示出来
            ft.show(frag);
        }else{//否则就实例化这个Fragment，并添加到FragmentManager
            frag = instantiateFragment(mContext,mManager,className,args);
            frag.setArguments(args);
            ft.add(mContainerId,frag);
        }
        ft.setPrimaryNavigationFragment(frag);

        final @IdRes int destId = destination.getId();

        ArrayDeque<Integer> mBackStack = null;
        //这个mBackStack是NavigatorProvider里面的，我们只能通过反射的方式去拿
        try {
            Field filed = FragmentNavigator.class.getDeclaredField("mBackStack");
            filed.setAccessible(true);
            mBackStack = (ArrayDeque<Integer>) filed.get(this);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        final boolean initialNavigation = mBackStack.isEmpty();
        // TODO Build first class singleTop behavior for fragments
        final boolean isSingleTopReplacement = navOptions != null && !initialNavigation
                && navOptions.shouldLaunchSingleTop()
                && mBackStack.peekLast() == destId;

        boolean isAdded;
        if (initialNavigation) {
            isAdded = true;
        } else if (isSingleTopReplacement) {
            // Single Top means we only want one instance on the back stack
            if (mBackStack.size() > 1) {
                // If the Fragment to be replaced is on the FragmentManager's
                // back stack, a simple replace() isn't enough so we
                // remove it from the back stack and put our replacement
                // on the back stack in its place
                mManager.popBackStack(
                        generateBackStackName(mBackStack.size(), mBackStack.peekLast()),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                ft.addToBackStack(generateBackStackName(mBackStack.size(), destId));
            }
            isAdded = false;
        } else {
            ft.addToBackStack(generateBackStackName(mBackStack.size() + 1, destId));
            isAdded = true;
        }
        if (navigatorExtras instanceof Extras) {
            Extras extras = (Extras) navigatorExtras;
            for (Map.Entry<View, String> sharedElement : extras.getSharedElements().entrySet()) {
                ft.addSharedElement(sharedElement.getKey(), sharedElement.getValue());
            }
        }
        ft.setReorderingAllowed(true);
        ft.commit();
        // The commit succeeded, update our view of the world
        if (isAdded) {
            mBackStack.add(destId);
            return destination;
        } else {
            return null;
        }
    }

    /**
     * 和FragmentNavigator里面的一模一样，直接去copy来用就行
     * @param backStackIndex
     * @param destId
     * @return
     */
    private String generateBackStackName(int backStackIndex, int destId) {
        return backStackIndex + "-" + destId;
    }
}
