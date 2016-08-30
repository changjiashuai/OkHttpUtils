package io.github.changjiashuai.okhttputils;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 16/8/30 11:21.
 */
public class OkHttpManager {

    private static final String TAG = "OkHttpManager";
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");//根据服务端返回定义
    private static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    private static final int TYPE_GET = 0;//get request
    private static final int TYPE_POST_JSON = 1;//post request with json params
    private static final int TYPE_POST_FORM = 2;//post request with form params
    private OkHttpClient mOkHttpClient;
    private Handler mOkHttpHandler;

    private OkHttpManager() {
        mOkHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        mOkHttpHandler = new Handler(Looper.getMainLooper());
    }

    public static OkHttpManager getInstance() {
        return SingletonHolder.mInstance;
    }

    private static class SingletonHolder {
        private static final OkHttpManager mInstance = new OkHttpManager();
    }

    //sync request
    public Response request(String url, int requestType, HashMap<String, String> paramsMap) {
        try {
            switch (requestType) {
                case TYPE_GET:
                    return get(url, paramsMap).execute();
                case TYPE_POST_JSON:
                    return postWithJson(url, paramsMap).execute();
                case TYPE_POST_FORM:
                    return postWithForm(url, paramsMap).execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Call get(String url, HashMap<String, String> paramsMap) throws UnsupportedEncodingException {
        StringBuilder params = parseParams(paramsMap);
        String requestUrl = String.format("%s?%s", url, params.toString());//url?key1=value1&key2=value2
        Request request = addHeaders().url(requestUrl).build();
        return mOkHttpClient.newCall(request);
    }

    private Call postWithJson(String url, HashMap<String, String> parmasMap) throws UnsupportedEncodingException {
        String params = parseParams(parmasMap).toString();
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, params);
        Request request = addHeaders().url(url).post(requestBody).build();
        return mOkHttpClient.newCall(request);
    }

    private Call postWithForm(String url, HashMap<String, String> paramsMap) {
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : paramsMap.keySet()) {
            builder.add(key, paramsMap.get(key));
        }
        RequestBody formBody = builder.build();
        Request request = addHeaders().url(url).post(formBody).build();
        return mOkHttpClient.newCall(request);
    }

    /**
     * 解析参数
     */
    private StringBuilder parseParams(HashMap<String, String> paramsMap) throws UnsupportedEncodingException {
        StringBuilder params = new StringBuilder();
        int pos = 0;
        for (String key : paramsMap.keySet()) {
            if (pos > 0) {
                params.append("&");
            }
            params.append(String.format("%s=%s", key, URLEncoder.encode(paramsMap.get(key), "utf-8")));
            pos++;
        }
        return params;
    }

    //async request
    public void asyncRequest(String url, int requestType, HashMap<String, String> paramsMap, Callback callBack) {
        try {
            switch (requestType) {
                case TYPE_GET:
                    get(url, paramsMap).enqueue(callBack);
                    break;
                case TYPE_POST_JSON:
                    postWithJson(url, paramsMap).enqueue(callBack);
                    break;
                case TYPE_POST_FORM:
                    postWithForm(url, paramsMap).enqueue(callBack);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Request.Builder addHeaders() {
        return new Request.Builder()
                .addHeader("Connection", "keep-alive")
                .addHeader("platform", "2")
                .addHeader("phoneModel", Build.MODEL)
                .addHeader("systemVersion", Build.VERSION.RELEASE)
                .addHeader("appVersion", "3.4.1");
    }
}