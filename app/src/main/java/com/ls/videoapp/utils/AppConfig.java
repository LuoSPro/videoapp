package com.ls.videoapp.utils;

import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ls.libcommon.AppGlobals;
import com.ls.videoapp.model.BottomBar;
import com.ls.videoapp.model.Destination;
import com.ls.videoapp.model.SofaTab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class AppConfig {

    private static HashMap<String, Destination> sDestConfig;

    private static BottomBar sBottomBar;

    private static SofaTab sSofaTab;

    public static HashMap<String, Destination> getDestConfig() {
        if (sDestConfig == null){
            //从destination.json文件中解析出sDestConfig对象
            String content = parseFile("destination.json");
            sDestConfig = JSON.parseObject(content,new TypeReference<HashMap<String,Destination>>(){}.getType());
        }
        return sDestConfig;
    }

    public static BottomBar getBottomBar(){
        if (sBottomBar == null){
            String content = parseFile("main_tabs_config.json");
            sBottomBar = JSON.parseObject(content,BottomBar.class);
        }
        return sBottomBar;
    }

    public static SofaTab getSofaTab(){
        if (sSofaTab == null){
            String content = parseFile("sofa_tabs_config.json");
            sSofaTab = JSON.parseObject(content,SofaTab.class);
            Collections.sort(sSofaTab.getTabs(), new Comparator<SofaTab.Tabs>() {
                @Override
                public int compare(SofaTab.Tabs o1, SofaTab.Tabs o2) {
                    return o1.getIndex() < o2.getIndex() ? -1 : 1;
                }
            });
        }
        return sSofaTab;
    }

    public static String parseFile(String fileName) {
        AssetManager assets = AppGlobals.getApplication().getResources().getAssets();
        InputStream stream = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            stream = assets.open(fileName);
            reader = new BufferedReader(new InputStreamReader(stream));
            String line = null;
            //把文件里面的数据一行一行的读出来
            while ((line = reader.readLine()) != null){
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (stream!= null){
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //返回读出来的数据
        return builder.toString();
    }
}
