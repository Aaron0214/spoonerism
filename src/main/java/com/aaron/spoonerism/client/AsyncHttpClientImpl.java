package com.aaron.spoonerism.client;

import com.ning.http.client.AsyncHttpClient;

/**
 * Created by Aaron on 17/4/10.
 */
public class AsyncHttpClientImpl extends AsyncHttpClient {

    public AsyncHttpClient.BoundRequestBuilder requestBuilder(String method, String url) {
        return super.requestBuilder(method, url);
    }

}
