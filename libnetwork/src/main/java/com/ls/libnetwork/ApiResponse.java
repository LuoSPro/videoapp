package com.ls.libnetwork;

/**
 * 对响应体进行包装
 * @param <T>
 */
public class ApiResponse<T> {

    public boolean success;
    public int status;
    public String message;
    public T body;

}
