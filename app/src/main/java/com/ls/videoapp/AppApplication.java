package com.ls.videoapp;

import android.app.Application;

import com.ls.libnetwork.ApiService;

public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化网络请求module，否则后面会报空指针
        ApiService.init("http://10.0.2.2:8888/serverdemo",null);
    }
}
