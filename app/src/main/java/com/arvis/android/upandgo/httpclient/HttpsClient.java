package com.arvis.android.upandgo.httpclient;

import android.text.TextUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


/**
 * Created by Jack on 9/11/16.
 */

public class HttpsClient {

    private OkHttpClient okHttpClient;

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    public OkHttpClient getHttpsClient(){
        return okHttpClient;
    }

    public enum RequestMethod{
        POST, GET, DELETE, PUT, PATCH
    }


    public HttpsClient() {

        okHttpClient = new OkHttpClient.Builder().retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).build();


    }

    public void sendGetRequestWithCustomHeader(String requestUrl, Callback callback, Map<String, String> headers) {


        Request.Builder getRequest = new Request.Builder().url(requestUrl).get();

        if(headers != null){

            for (String key : headers.keySet()){
                getRequest.addHeader(key, headers.get(key));
            }
        }


        okHttpClient.newCall(getRequest.build()).enqueue(callback);

    }


    public void sendJsonRequestWithCustomHeader(String url, String content, RequestMethod method, Callback callback, Map<String, String> additionalHeader){

        RequestBody body = null;

        if(!TextUtils.isEmpty(content)){
            body = RequestBody.create(JSON, content);
        }

        Request.Builder request = new Request.Builder().url(url);

        if(additionalHeader != null){
            for (String key : additionalHeader.keySet()){
                request.addHeader(key, additionalHeader.get(key));
            }
        }

        addMethod(request, method, body);

        okHttpClient.newCall(request.build()).enqueue(callback);
    }

    private void addMethod(Request.Builder reqBuilder, RequestMethod method, RequestBody body){
        switch (method){
            case POST:

                reqBuilder.post(body);

                break;

            case DELETE:

                if(body == null){

                    reqBuilder.delete();

                }else{
                    reqBuilder.delete(body);
                }

                break;
            case PUT:
                reqBuilder.put(body);
                break;
        }
    }

}
