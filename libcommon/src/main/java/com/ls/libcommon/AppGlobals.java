package com.ls.libcommon;

import android.annotation.SuppressLint;
import android.app.Application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 随着后期的开发，我们将一些类下沉，放到一个公共的库，供大家一起使用
 */
public class AppGlobals {

    private static Application sApplication;

    @SuppressLint("PrivateApi")
    public static Application getApplication(){
        if (sApplication == null){
            //通过反射ActivityThread的currentApplication方法来得到Application
            try {
                Method method = Class.forName("android.app.ActivityThread").getDeclaredMethod("currentApplication");
                //因为currentApplication方法是不需要参数的，所以我们这里就直接传null进去即可
                sApplication = (Application) method.invoke(null);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return sApplication;
    }

}
