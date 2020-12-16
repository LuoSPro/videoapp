package com.ls.libnetwork;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.arch.core.executor.ArchTaskExecutor;

import com.ls.libnetwork.cache.CacheManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 *
 * @param <T> Response里面的实体类型
 * @param <R> Request的子类
 */
public abstract class Request<T,R extends Request> implements Cloneable {

    public String mUrl;
    //对headers参数的存储
    private HashMap<String, String> headers = new HashMap<>();
    //对参数的存储，后面的object接收八种基本数据类型
    protected HashMap<String, Object> params = new HashMap<>();

    //只访问缓存，即便本地缓存不存在，也不会发起网络请求
    public static final int CACHE_ONLY = 1;
    //先访问缓存，再访问网络（并发），成功后缓存到本地
    public static final int CACHE_FIRST = 2;
    //只访问网络，不做任何存储
    public static final int NET_ONLY = 3;
    //先访问网络，网络接口回来成功之后，缓存到本地
    public static final int NET_CACHE = 4;
    private String cacheKey;
    private Type mType;
    private Class mClaz;
    private int mCacheStrategy;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CACHE_ONLY,CACHE_FIRST,NET_ONLY,NET_CACHE})
    public @interface CacheStrategy{

    }
    public Request(String url){//这里的url不需要服务器域名
        mUrl = url;
    }

    public R addHeader(String key,String value){
        headers.put(key, value);
        return (R) this;
    }

    public R addParam(String key,Object value){
        //类里面的TYPE字段，类型：Class<Integer>,我们可以去找到类中的TYPE字段，然后通过TYPE字段来比较他是不是8
        //种基本数据类型的一个，就能实现我们对value类型的判断
        try {
            Field filed = value.getClass().getField("TYPE");
            Class claz = (Class) filed.get(null);
            if (claz.isPrimitive()){//如果这个class是基本数据类型
                params.put(key, value);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return (R) this;
    }

    /**
     * 用于获取缓存的key
     * @param key
     * @return
     */
    public R cacheKey(String key){
        this.cacheKey = key;
        return (R) this;
    }

    /**
     * 对于同步请求的情况，我们只能暴露出Api
     * @param type
     * @return
     */
    public R responseType(Type type){
        this.mType = type;
        return (R) this;
    }

    public R responseClaz(Class claz){
        mClaz = claz;
        return (R) this;
    }

    public R cacheStrategy(@CacheStrategy int cacheStrategy){
        mCacheStrategy = cacheStrategy;
        return (R) this;
    }

    /**
     * 真正执行网络请求的方法
     * 这里对execute方法重载，如果有参数，代表异步请求，如果没参数，代表同步
     */
    @SuppressLint("RestrictedApi")
    public void execute(final JsonCallback<T> callback){
        if (mCacheStrategy != NET_ONLY){//不为只查询网络时，才做缓存
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    //读取缓存
                    ApiResponse<T> response = readCache();
                    if (callback != null){
                        callback.onCreateSuccess(response);
                    }
                }
            });
        }
        if (mCacheStrategy != CACHE_ONLY){//不为CACHE_ONLY时，才访问网络
            getCall().enqueue(new Callback() {
                /**
                 * 请求失败
                 * @param call
                 * @param e
                 */
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    ApiResponse<T> response = new ApiResponse<>();
                    response.message = e.getMessage();
                    //回调失败的方法
                    callback.onError(response);
                }

                /**
                 * 请求成功
                 * @param call
                 * @param response
                 * @throws IOException
                 */
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    ApiResponse<T> apiResponse = parseResponse(response,callback);
                    //请求成功，但是还得在业务逻辑上面判断是否成功了
                    if (apiResponse.success){
                        callback.onSuccess(apiResponse);
                    }else{
                        callback.onError(apiResponse);
                    }
                }
            });
        }
    }

    private ApiResponse<T> readCache() {
        //处理key
        String key = TextUtils.isEmpty(cacheKey) ? generateCacheKey() : cacheKey;
        Object cache = CacheManager.getCache(key);
        ApiResponse<T> result = new ApiResponse<>();
        result.status = 304;
        result.message = "缓存获取成功";
        result.body = (T) cache;
        result.success = true;
        return result;
    }

    private ApiResponse<T> parseResponse(Response response, JsonCallback<T> callback) {
        //返回response里面的message：成功的，失败的，报错的
        String message = null;
        //返回的状态码
        int status = response.code();
        //是否成功(业务逻辑上的判断)
        boolean success = response.isSuccessful();
        ApiResponse<T> result = new ApiResponse<>();
        //把ApiService里面初始化的Convert拿来转化返回结果
        Convert convert = ApiService.sConvert;
        try {
            String content = response.body().string();
            if (success){//请求成功
                if (callback != null){
                    ParameterizedType type = (ParameterizedType) callback.getClass().getGenericSuperclass();
                    Type argument = type.getActualTypeArguments()[0];
                    result.body = (T)convert.convert(content, argument);//通过Convert进行转换
                }else if (mType != null){//通过暴露给外面的Api，对Type进行赋值保存，再使用Type进行解析
                    result.body = (T) convert.convert(content,mType);
                }else if (mClaz != null){//通过暴露给外面的Api，对Class进行赋值保存，使用返回体的Class进行解析
                    result.body = (T) convert.convert(content,mClaz);
                }else{//无法解析的情况
                    Log.e("request", "parseResponse: 无法解析" );
                }
            }else {//请求失败
                message = content;
            }
        } catch (IOException e) {
            message = e.getMessage();
            result.success = false;
        }
        result.success = success;
        result.status = status;
        result.message = message;

        //当mCacheStrategy的模式不为只访问网络时才做缓存
        if (mCacheStrategy != NET_ONLY&&result.success&&result.body!=null&&result.body instanceof Serializable){
            saveCache(result.body);
        }

        return result;
    }

    private void saveCache(T body) {
        //检查用户传入的key是否为null，为null，我们就为其创建一个，不为null，我们就直接使用
        String key = TextUtils.isEmpty(cacheKey)?generateCacheKey():cacheKey;
        CacheManager.save(key,body);
    }

    private String generateCacheKey() {
        cacheKey = UrlCreator.createUrlFromParams(mUrl,params);
        return cacheKey;
    }

    private Call getCall() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        //request header的构造
        addHeaders(builder);
        //request body的构造
        okhttp3.Request request = generateRequest(builder);
        Call call = ApiService.okHttpClient.newCall(request);
        return call;
    }

    protected abstract okhttp3.Request generateRequest(okhttp3.Request.Builder builder);

    private void addHeaders(okhttp3.Request.Builder builder) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(),entry.getValue());
        }
    }

    /**
     * 对于同步请求，由于没有JsonCallback，我们获取不了泛型 T 的实际类型
     *
     * 因为jdk1.5之后，规定泛型写到java文件中时，没有显示明确声明泛型类型的，会被编译器在编译时擦出掉，比如：new ArrayList<User>()  就会被擦除
     * 即 通过new关键字给一个普通的Class传进去的泛型都会被擦除
     *
     * 但是  new 接口/抽象类  时，传递的泛型类型运行时是能获取到的，因为编译时会生成  接口/抽象类  的匿名内部类，内部类已经显示明确声明了泛型的类型，不会被擦除
     *
     *     class innerClass implements Interface<List<User>>{
     *         ParameterizedType type = (ParameterizedType) callback.getClass().getGenericSuperclass();
     *         Type actual = type.getActualTypeArguments()[0];
     *         //此时的actual就是List<User> 类型的
     *     }
     * JsonCallback是一个抽象类，外面在传值给他的时候，会生成一个匿名内部类
     * @return
     */
    public ApiResponse<T> execute(){
        //同步请求里面：要么只能读缓存，要么只能网络数据
        if (mCacheStrategy == CACHE_ONLY){
            return readCache();
        }
        //Request类实现了Cloneable之后，我们修改mCacheStrategy，就可以实现先读取缓存，再请求网络数据了
        try {
            Response response = getCall().execute();
            ApiResponse<T> result = parseResponse(response, null);//同步请求的时候，没有JsonCallback，所以我们传null过去
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
