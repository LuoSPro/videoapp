package com.ls.libnetwork;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class UrlCreator {

    public static String createUrlFromParams(String url, Map<String, Object> params){

        StringBuilder builder = new StringBuilder();
        builder.append(url);
        if (url.indexOf("?") > 0||url.indexOf("&")>0){//前面已经加了参数了，那么下面的参数就要通过 &接到后面
            builder.append("&");
        }else{//如果前面没加参数，那么就先加一个 ?，后面再接参数
            builder.append("?");
        }
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            try {
                //转码
                String value = URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8");
                builder.append(entry.getKey()).append("=").append(value).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        //删除上面多添加的那个 & 符号
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

}
