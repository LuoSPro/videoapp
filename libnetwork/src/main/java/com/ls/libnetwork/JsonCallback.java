package com.ls.libnetwork;

public abstract class JsonCallback<T> {

    public void onSuccess(ApiResponse<T> response){

    }

    public void onError(ApiResponse<T> response){

    }

    public void onCreateSuccess(ApiResponse<T> response){

    }

}
