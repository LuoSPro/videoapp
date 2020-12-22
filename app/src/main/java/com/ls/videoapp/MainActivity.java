package com.ls.videoapp;

import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ls.libnetwork.ApiResponse;
import com.ls.libnetwork.GetRequest;
import com.ls.libnetwork.JsonCallback;
import com.ls.videoapp.utils.NavGraphBuilder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private NavController mNavController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);

        //这样，我们就不用布局文件中的：app:navGraph="@navigation/mobile_navigation" 了
        NavGraphBuilder.build(mNavController,this,fragment.getId());

        //设置底部导航栏的监听事件
        navView.setOnNavigationItemSelectedListener(this);

//        GetRequest<JSONObject> request = new GetRequest<>("https://www.baidu.com/");
        //同步

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
//        request.execute();
//
//        //异步
//        request.execute(new JsonCallback<JSONObject>() {
//            @Override
//            public void onSuccess(ApiResponse<JSONObject> response) {
//                super.onSuccess(response);
//            }
//        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //通过NavController跳转页面
        mNavController.navigate(item.getItemId());
        //返回true表示被选中，就会有一个上下浮动的效果
        //根据item的title是否为null，来决定返回值
        return TextUtils.isEmpty(item.getTitle());
    }
}
