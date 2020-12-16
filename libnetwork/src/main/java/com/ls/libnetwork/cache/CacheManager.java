package com.ls.libnetwork.cache;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CacheManager {

    /**
     * 写入缓存
     * @param key
     * @param body
     * @param <T>
     */
    public static <T> void save(String key,T body){
        Cache cache = new Cache();
        //这个key，上一层已经处理了，这里直接赋值
        cache.key = key;
        //把body变成字节数组再保存
        cache.data = toByteArray(body);

        //写入数据库
        CacheDatabase.get().getCacheDao().save(cache);
    }

    /**
     * 读出缓存
     * @param key
     * @return
     */
    public static Object getCache(String key){
        Cache cache = CacheDatabase.get().getCacheDao().getCache(key);
        if (cache != null&&cache.data != null){
            return toObject(cache.data);
        }
        return null;
    }

    /**
     * 将二进制数组转化成对象
     * @param data
     * @return
     */
    private static Object toObject(byte[] data) {
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(data);
            ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            if (bais != null){
                try {
                    bais.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (ois != null){
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 将对象转化成一个二进制数组
     * @param body
     * @param <T>
     * @return
     */
    public static <T> byte[] toByteArray(T body){
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(body);
            oos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (baos != null){
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (oos != null){
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new byte[0];
    }

}
