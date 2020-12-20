package com.ls.libnetwork;

public class GetRequest<T> extends Request<T,GetRequest> {

    public GetRequest(String url) {
        super(url);
    }

    /**
     * Get请求，所有的参数都是拼接到URL上面，所以需要将params这个map上面的数据加到URL上面去
     * @param builder
     * @return
     */
    @Override
    protected okhttp3.Request generateRequest(okhttp3.Request.Builder builder) {
        return builder.get().url(UrlCreator.createUrlFromParams(mUrl, params)).build();
    }
}
