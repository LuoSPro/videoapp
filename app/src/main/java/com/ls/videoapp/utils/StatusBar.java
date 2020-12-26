package com.ls.videoapp.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;

public class StatusBar {

    public static void fitSystemBar(Activity activity){
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            //Android6.0之后的版本才设置沉浸式布局
            return;
        }
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        //View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN:能够使我们的页面布局延伸到状态栏之下，但不会隐藏状态栏，即状态栏是遮盖在布局之上的
        //View.SYSTEM_UI_FLAG_FULLSCREEN:能使我们页面布局延伸到状态栏，但是会隐藏状态栏。
        //WindowManager.LayoutParams.FLAG_FULLSCREEN：和上面View.SYSTEM_UI_FLAG_FULLSCREEN效果是一样的
        //View.SYSTEM_UI_FLAG_LAYOUT_STABLE:用户手机上的虚拟按钮，当我们显示或隐藏的时候，它会触发我们页面的重新布局，但是使用这个之后，无论虚拟按钮的状态是怎样，都能保证我们
        //布局在状态栏之下
        //View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR:轻亮的状态栏，我们不设置的话，状态栏就是主题色为底色，白色字体。如果设置了之后，状态栏会变成白底黑字
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        //运行window对状态栏进行绘制
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //指定状态栏为透明色，不然window会绘制成灰色
        window.setStatusBarColor(Color.TRANSPARENT);

        //为了不使页面上的文字和状态栏的文字重叠，我们还要在每个页面的根布局设置 android:fitsSystemWindows="true" 属性
    }

}
