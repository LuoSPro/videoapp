package com.ls.libnetwork;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.deserializer.JSONPDeserializer;

import java.lang.reflect.Type;

public class JsonConvert implements Convert {
    @Override
    public Object convert(String response, Type type) {
        JSONObject jsonObject = JSON.parseObject(response);
        //获取responseBody里面的data字段。（我们请求的数据就是放到data里面返回的）
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null){
            //获取data里面的data字段，
            Object innerData = data.get("data");
            return JSON.parseObject(innerData.toString(),type);
        }
        return null;
    }

    @Override
    public Object convert(String response, Class claz) {
        JSONObject jsonObject = JSON.parseObject(response);
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null){
            Object innerData = data.get("data");
            return JSON.parseObject(innerData.toString(),claz);
        }
        return null;
    }
}
