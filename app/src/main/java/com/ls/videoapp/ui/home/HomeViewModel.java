package com.ls.videoapp.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PageKeyedDataSource;
import androidx.paging.PositionalDataSource;

import com.alibaba.fastjson.TypeReference;
import com.ls.libnetwork.ApiResponse;
import com.ls.libnetwork.ApiService;
import com.ls.libnetwork.JsonCallback;
import com.ls.libnetwork.Request;
import com.ls.videoapp.AbsViewModel;
import com.ls.videoapp.model.Feed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeViewModel extends AbsViewModel<Feed> {

    private volatile boolean witchCache = true;

    /**
     * DataSource<Key,Value>数据源：  key对应加载数据的条件信息，value对应数据实体类
     *
     * 1.PageKeyedDataSource;适用于目标数据 根据页信息请求数据的场景
     * 2.ItemKeyedDataSource:适用于目标数据的加载 依赖特定item的信息
     * 3.PositionalDataSource:适用于目标数据总数固定，通过特定的位置加载数据
     * @return
     */
    @Override
    public DataSource createDataSource() {

        return null;
    }

    ItemKeyedDataSource<Integer,Feed> mDataSource = new ItemKeyedDataSource<Integer, Feed>() {
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            //加载初始化数据的
            //首页的加载：先在家缓存，然后再加载网络数据，网络数据成功之后，再更新本地缓存
            loadData(0,callback);
            witchCache = false;
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            //加载分页数据的
            loadData(params.key,callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            callback.onResult(Collections.emptyList());
            //是能够向前加载数据的——一般不怎么用到
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            //通过最后一条Item的信息，去返回一个Item的对象
            return item.getId();
        }
    };

    private void loadData(int key, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        //由于PageList在调用loadInitial、loadAfter、loadBefore方法的时候，已经开了子线程了，所以我们在这里就没必要再开子线程了，所以这里直接使用同步请求
        Request request = ApiService.get("/feeds/queryHotFeedsList")
                .addParam("feedType", null)
                .addParam("userId", 0)//用户id
                .addParam("feedId", key)
                .addParam("pageCount", 10)//分页加载多少条
                .responseType(new TypeReference<ArrayList<Feed>>() {
                }.getType());//同步方法，需要添加一个responseType。（因为同步无法获取到泛型类型）

        if (witchCache){//需要加载缓存
            request.cacheStrategy(Request.CACHE_ONLY);
            request.execute(new JsonCallback<List<Feed>>() {
                @Override
                public void onCacheSuccess(ApiResponse<List<Feed>> response) {

                }
            });
        }

        //网络数据的请求
        try {
            Request netRequest = witchCache?request.clone():request;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }
}