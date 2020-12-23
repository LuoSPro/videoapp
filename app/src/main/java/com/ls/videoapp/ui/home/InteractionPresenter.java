package com.ls.videoapp.ui.home;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.ls.libcommon.AppGlobals;
import com.ls.libnetwork.ApiResponse;
import com.ls.libnetwork.ApiService;
import com.ls.libnetwork.JsonCallback;
import com.ls.videoapp.model.Feed;
import com.ls.videoapp.model.User;
import com.ls.videoapp.ui.login.UserManager;

import org.json.JSONException;
import org.json.JSONObject;

public class InteractionPresenter {

    private static final String URL_TOGGLE_FEED_LIK = "/ugc/toggleFeedLike";

    private static final String URL_TOGGLE_FEED_DISS = "/ugc/dissFeed";

    public static void toggleFeedLicked(LifecycleOwner owner, Feed feed){
        //判断用户是否已经登录
        if (!UserManager.get().isLogin()){
            LiveData<User> loginLiveData = UserManager.get().login(AppGlobals.getApplication());
            loginLiveData.observe(owner, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if (user != null){
                        //登录成功之后，继续完成刚才的请求
                        toggleFeedLikeInternal(feed);
                    }
                    //登录完成之后，移除这个Observer
                    loginLiveData.removeObservers(owner);
                }
            });
            return;
        }
        toggleFeedLikeInternal(feed);
    }

    /**
     * 用户点赞
     * @param feed
     */
    private static void toggleFeedLikeInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_LIK)
                .addParam("userId", UserManager.get().getUserId())
                .addParam("item",feed.getItemId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null){
                            try {
                                //获取到的网络数据
                                boolean hasLiked = response.body.getBoolean("hasLiked");
                                //渲染到页面，设置状态
                                feed.getUgc().setHasLiked(hasLiked);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });
    }

    /**
     * 用户 “踩”  的行为
     */
    public static void toggleFeedDiss(LifecycleOwner owner,Feed feed){
        //判断用户是否已经登录
        if (!UserManager.get().isLogin()){
            LiveData<User> loginLiveData = UserManager.get().login(AppGlobals.getApplication());
            loginLiveData.observe(owner, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if (user != null){
                        //登录成功之后，继续完成刚才的请求
                        toggleFeedDissInternal(feed);
                    }
                    //登录完成之后，移除这个Observer
                    loginLiveData.removeObservers(owner);
                }
            });
            return;
        }
        toggleFeedDissInternal(feed);
    }

    public static void toggleFeedDissInternal(Feed feed){

        ApiService.get(URL_TOGGLE_FEED_DISS).addParam("userId",UserManager.get().getUserId())
                .addParam("itemId",feed.getItemId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null){
                            try {
                                //获取到的网络数据
                                boolean hasLiked = response.body.getBoolean("hasLiked");
                                //渲染页面，改变状态
                                feed.getUgc().setHasdiss(hasLiked);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

    }
}
