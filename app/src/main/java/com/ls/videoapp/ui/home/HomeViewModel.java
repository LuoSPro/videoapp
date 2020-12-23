package com.ls.videoapp.ui.home;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import com.alibaba.fastjson.TypeReference;
import com.ls.libnetwork.ApiResponse;
import com.ls.libnetwork.ApiService;
import com.ls.libnetwork.JsonCallback;
import com.ls.libnetwork.Request;
import com.ls.videoapp.ui.AbsViewModel;
import com.ls.videoapp.model.Feed;
import com.ls.videoapp.ui.MutableDataSource;
import com.ls.videoapp.ui.login.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeViewModel extends AbsViewModel<Feed> {

    private static final String TAG = "HomeViewModel";

    private volatile boolean witchCache = true;

    //这里使用MutableLiveData，就是因为MutableLiveData里面将PostValue和setValue方法设置成了public，便于发送
    private MutableLiveData<PagedList<Feed>> cacheLiveData = new MutableLiveData<>();

    //设置同步标志位
    private AtomicBoolean loadAfterFlag = new AtomicBoolean(false);

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
        //这里每次都要创建创建DataSource，不然DataSource里面的mInvalid这个标志位不会被更新，一直为true，这样，就不会去加载数据了，
        //而新建一个对象之后，mInvalid被初始化为false，这样就能重新加载数据了
        return new FeedDataSource();
    }

    public MutableLiveData<PagedList<Feed>> getCacheLiveData() {
        return cacheLiveData;
    }

    class FeedDataSource extends ItemKeyedDataSource<Integer, Feed> {
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            //加载初始化数据的
            //首页的加载：先加载缓存，然后再加载网络数据，网络数据成功之后，再更新本地缓存
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
        if (key > 0){//如果传入的key是大于0的，代表此次加载是分页的，我们就要设置同步位为true
            loadAfterFlag.set(true);
        }
        //由于PageList在调用loadInitial、loadAfter、loadBefore方法的时候，已经开了子线程了，所以我们在这里就没必要再开子线程了，所以这里直接使用同步请求
        Request request = ApiService.get("/feeds/queryHotFeedsList")
                .addParam("feedType", null)
                .addParam("userId", UserManager.get().getUserId())//用户id
                .addParam("feedId", key)
                .addParam("pageCount", 10)//分页加载多少条
                .responseType(new TypeReference<ArrayList<Feed>>() {
                }.getType());//同步方法，需要添加一个responseType。（因为同步无法获取到泛型类型）

        if (witchCache){//需要加载缓存
            request.cacheStrategy(Request.CACHE_ONLY);
            request.execute(new JsonCallback<List<Feed>>() {
                @Override
                public void onCacheSuccess(ApiResponse<List<Feed>> response) {
                    if (response.body != null){
                        Log.d(TAG, "onCacheSuccess: " + response.body.size());
                        //我们要将请求到的数据通过adapter.subList()设置到Adapter里面去，但是subList接收的是PagedList对象，所以我们需要将body进行转化
                        List<Feed> body = response.body;
                        //将body和PagedList进行封装
                        MutableDataSource dataSource = new MutableDataSource<Integer,Feed>();
                        dataSource.data.addAll(body);
                        PagedList pagedList = dataSource.buildNewPagedList(mConfig);//mConfig传进去构造PagedList对象
                        //因为是在子线程里面，所以这里只能使用PostValue()方法
                        cacheLiveData.postValue(pagedList);
                    }else{
                        Log.d(TAG, "onCacheSuccess: response body is null");
                    }
                }
            });
        }

        //网络数据的请求
        try {
            //判断是从缓存拿数据，还是直接去请求网络
            Request netRequest = witchCache? request.clone() :request;
            //更改cacheStrategy的值，就可以实现，先读取缓存，载请求数据了
            netRequest.cacheStrategy(key == 0?Request.NET_CACHE:Request.NET_ONLY);
            //请求后的返回结果
            ApiResponse<List<Feed>> response = netRequest.execute();
            //判断返回的数据是否为null
            List<Feed> data = response.body == null ? Collections.emptyList() : response.body;
            //通知请求的结果到了，回调onResult
            callback.onResult(data);
            if (key>0){//key>0:上拉加载
                //通过liveData发送数据，告诉UI层 是否应该主动关闭上拉加载分页的动画
                getBooleanMutableLiveData().postValue(data.size()>0);
                //如果加载完之后，发现key>0，就说明还能加载分页数据，我们设置同步位为false
                loadAfterFlag.set(false);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        Log.e(TAG, "loadData: key --> " + key);
    }

    @SuppressLint("RestrictedApi")
    public void loadAfter(int id, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        if (loadAfterFlag.get()){//判断同步位
            callback.onResult(Collections.emptyList());
            return;
        }
        //这样就能手动触发分页数据的加载了。但是需要设置一个同步位，因为Paging可能会帮我们处理，我们在上拉的时候又手动触发了记载分页
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                loadData(id,callback);
            }
        });

    }
}