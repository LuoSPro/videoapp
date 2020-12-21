package com.ls.libnetwork;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ApiService {

    protected static String sBaseUrl;

    protected static okhttp3.OkHttpClient okHttpClient;
    protected static Convert sConvert;

    /**
     * 对okHttp的一些配置进行初始化
     */
    static {
        //网络请求的日志
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)//超时的时间配置
                .writeTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(interceptor)//添加日志打印
                .build();

        //Https接口请求证书信任的问题
        TrustManager[] trustManagers = {new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};
        try {
            //Http的握手的过程
            SSLContext ssl = SSLContext.getInstance("SSL");
            ssl.init(null,trustManagers,new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(ssl.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
        }
    }

    //初始化URL的域名，并传入Convert，以便Request里面用来转换返回的结果
    public static void init(String baseUrl,Convert convert){
        sBaseUrl = baseUrl;
        if (convert == null){
            convert = new JsonConvert();
        }
        sConvert = convert;
    }

    /**
     * 暴露接口，方便外部直接使用GET请求
     */
    public static <T> GetRequest<T> get(String url){
        return new GetRequest<>(sBaseUrl+url);
    }

    /**
     * 暴露接口，方便外部直接使用POST请求
     */
    public static <T> PostRequest<T> post(String url){
        return new PostRequest<>(sBaseUrl+url);
    }

}
