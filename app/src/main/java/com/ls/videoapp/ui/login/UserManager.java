package com.ls.videoapp.ui.login;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ls.libnetwork.cache.CacheManager;
import com.ls.videoapp.model.User;

public class UserManager {

    private static final String KEY_CACHE_USER = "cache_user";

    private MutableLiveData<User> mUserMutableLiveData = new MutableLiveData<>();

    private static UserManager mUserManager = new UserManager();
    private User mUser;

    /**
     * 暴露接口给外界调用mUserManager
     * @return
     */
    public static UserManager get(){
        return mUserManager;
    }

    private UserManager() {
        //获取我们缓存的user信息
        User cache = (User) CacheManager.getCache(KEY_CACHE_USER);
        if (cache != null&&cache.getExpires_time() < System.currentTimeMillis()){
            //说明缓存的用户信息还有效
            mUser = cache;
        }
    }

    public void save(User user) {
        //保存一份
        mUser = user;
        //这里传递的对象必须实现了Serializable,因为我们存储数据的时候，是用二进制来保存的
        CacheManager.save(KEY_CACHE_USER, user);
        //以前传递信息的时候，都是在Activity之间使用Intent传递数据，然后在onActivityResult中去获取返回的信息，
        //现在，jetPack中的liveData就能发送数据，通过Observer去观察数据的变化，并且LiveData和生命周期绑定，更安全

        //判断是否有观察者已经注册到LiveData里面了，如果有，我们就不需要再发送这个事件了，因为Observer能够监听到
        if (mUserMutableLiveData.hasObservers()) {//判断是否有observer注册
            mUserMutableLiveData.postValue(user);//调用这个方法的地方可能在主线程，也可能在子线程
        }
    }

    /**
     * 统一跳转LoginActivity的接口
     *
     * @param context
     * @return
     */
    public LiveData<User> login(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        //不设置，会出现：Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        context.startActivity(intent);
        return mUserMutableLiveData;
    }

    /**
     * 判断用户当前是否登录
     *
     * @return
     */
    public boolean isLogin() {
        return mUser == null ? false : mUser.getExpires_time() < System.currentTimeMillis();
    }

    /**
     * 获取当前登录的用户的信息
     *
     * @return
     */
    public User getUser() {
        return isLogin() ? mUser : null;
    }

    /**
     * 获取用户id
     * @return
     */
    public long getUserId() {
        return isLogin() ? mUser.getUserId() : 0;
    }

}
