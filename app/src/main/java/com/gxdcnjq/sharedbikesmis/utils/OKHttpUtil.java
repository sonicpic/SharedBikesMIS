package com.gxdcnjq.sharedbikesmis.utils;

import android.util.Log;


import com.gxdcnjq.sharedbikesmis.utils.okHttpCallback.OkHttpCallback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocketListener;

public class OKHttpUtil {
    private static Request request = null;
    private static Call call = null;
    private static int TimeOut = 120;
    private static String token = "DEFAULT";

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        OKHttpUtil.token = token;
    }

    //单例获取http3对象
    private static OkHttpClient client = null;
    /**
     * OkHttpClient的构造方法，通过线程锁的方式构造
     * @return OkHttpClient对象
     */
    private static synchronized OkHttpClient getInstance() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .addInterceptor(new TokenInterceptor())
                    .readTimeout(TimeOut, TimeUnit.SECONDS)
                    .connectTimeout(TimeOut, TimeUnit.SECONDS)
                    .writeTimeout(TimeOut, TimeUnit.SECONDS)
                    .build();
        }
        return client;
    }

    /**
     * callback接口
     * 异步请求时使用
     */
    static class MyCallBack implements Callback {
        private OkHttpCallback okHttpCallBack;

        public MyCallBack(OkHttpCallback okHttpCallBack) {
            this.okHttpCallBack = okHttpCallBack;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            okHttpCallBack.onFailure(e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            okHttpCallBack.onSuccess(response);
        }
    }

    /**
     * 自定义Interceptor
     * 处理携带token
     */
    static class TokenInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            // 获取原始的请求
            Request originalRequest = chain.request();

            // 根据你的实际需求，添加 Token 头部到请求中
            Request modifiedRequest = originalRequest.newBuilder()
                    .header("token", token)
                    .build();

            // 继续执行请求链，并返回响应
            return chain.proceed(modifiedRequest);
        }
    }

    /**
     * 同步GET请求
     * 例如：请求的最终地址为：http://127.0.0.1:8081/user/getUser/123
     * @param url 基本请求地址   例子： http://127.0.0.1:8081
     * @param args 请求的参数    args[]=new String[]{"user","getUser","123"}
     * @return
     */
    public static String getSyncRequest(String url, String... args) {
        List<String> result = new ArrayList<>();
        StringBuilder address = new StringBuilder(url);
        for (int i = 0; i < args.length; i++) {
            address.append("/").append(args[i]);
        }
        final String finalAddress = address.toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                client = getInstance();
                Log.d("同步GET请求地址：", finalAddress);
                HttpUrl.Builder urlBuilder = HttpUrl.parse(finalAddress).newBuilder();
                // 添加GET请求的参数
                // 例如：urlBuilder.addQueryParameter("param1", "value1");
                // 可以根据实际需求添加参数

                request = new Request.Builder()
                        .url(urlBuilder.build())
                        .get()
                        .addHeader("device-platform", "android")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String res = response.body().string();
                    result.add(res);
                    Log.d("HttpUtil", "同步GET请求成功！");
                    Log.d("请求对象：", res);
                } catch (Exception e) {
                    Log.d("HttpUtil", "同步GET请求失败！");
                    e.printStackTrace();
                }
            }
        }).start();
        // 等待请求完成
        int count = 0;
        while (result.size() == 0 && count < 300) {
            try {
                TimeUnit.MILLISECONDS.sleep(10); // 等待10毫秒
                count++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (count < 300) {
            return result.get(0);
        } else {
            Log.d("HttpUtil", "等待返回值超时");
            return null;
        }
    }

    /**
     * 同步Post请求，queryParam传查询参数，formDataParams传表单参数
     *
     * @param url      基本请求地址，例如：http://127.0.0.1:8081
     * @param args   请求的查询路径，例如：new String[]{"user", "getUser", "123"}
     * @param queryParams 请求的查询参数，键值对形式
     * @param formDataParams    请求的FormData参数，键值对形式
     * @return 请求结果的字符串
     */
    public static String postSyncRequestFormData(String url, Map<String,String> queryParams, Map<String,String> formDataParams, String... args) {
        List<String> result = new ArrayList<>();
        StringBuilder address = new StringBuilder(url);
        for (String arg : args) {
            address.append("/").append(arg);
        }

        // 添加查询参数到 URL
        HttpUrl.Builder urlBuilder = HttpUrl.parse(address.toString()).newBuilder();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            urlBuilder.addQueryParameter(key, value);
        }
        final String finalAddress = urlBuilder.build().toString();

        Log.d("pan",finalAddress);

        new Thread(new Runnable() {
            @Override
            public void run() {
                client = getInstance();
                Log.d("同步 POST 请求地址：", finalAddress);
                FormBody.Builder formBody = new FormBody.Builder();
                // 添加formBody参数
                for (Map.Entry<String, String> entry : formDataParams.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    // 在这里进行对键值对的处理
                    formBody.add(key, value);
                }

                request = new Request.Builder()
                        .url(finalAddress)
                        .post(formBody.build())
                        .addHeader("device-platform", "android")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String res = response.body().string();
                    result.add(res);
                    Log.d("HttpUtil", "同步 POST 请求成功！");
                    Log.d("请求对象：", res);
                } catch (Exception e) {
                    Log.d("HttpUtil", "同步 POST 请求失败！");
                    e.printStackTrace();
                }
            }
        }).start();

        // 等待结果返回
        int count = 0;
        while (result.size() == 0 && count < 300) {
            try {
                TimeUnit.MILLISECONDS.sleep(10); // 等待 10 毫秒
                count++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (count < 300) {
            return result.get(0);
        } else {
            Log.d("HttpUtil", "等待返回值超时");
            return null;
        }
    }

    /**
     * 同步Post请求，传application/json
     *
     * @param url      基本请求地址，例如：http://127.0.0.1:8081
     * @param args   请求的查询路径，例如：new String[]{"user", "getUser", "123"}
     * @param queryParams 请求的查询参数，键值对形式
     * @param json
     * @return 请求结果的字符串
     */
    public static String postSyncRequestJson(String url, Map<String,String> queryParams,String json, String... args) {
        List<String> result = new ArrayList<>();
        StringBuilder address = new StringBuilder(url);
        for (String arg : args) {
            address.append("/").append(arg);
        }

        // 添加查询参数到 URL
        HttpUrl.Builder urlBuilder = HttpUrl.parse(address.toString()).newBuilder();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            urlBuilder.addQueryParameter(key, value);
        }
        final String finalAddress = urlBuilder.build().toString();

        Log.d("pan",finalAddress);

        new Thread(new Runnable() {
            @Override
            public void run() {
                client = getInstance();
                Log.d("同步 POST 请求地址：", finalAddress);
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody requestBody = RequestBody.create(mediaType, json);

                request = new Request.Builder()
                        .url(finalAddress)
                        .post(requestBody)
                        .addHeader("device-platform", "android")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String res = response.body().string();
                    result.add(res);
                    Log.d("HttpUtil", "同步 POST 请求成功！");
                    Log.d("请求对象：", res);
                } catch (Exception e) {
                    Log.d("HttpUtil", "同步 POST 请求失败！");
                    e.printStackTrace();
                }
            }
        }).start();

        // 等待结果返回
        int count = 0;
        while (result.size() == 0 && count < 300) {
            try {
                TimeUnit.MILLISECONDS.sleep(10); // 等待 10 毫秒
                count++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (count < 300) {
            return result.get(0);
        } else {
            Log.d("HttpUtil", "等待返回值超时");
            return null;
        }
    }

    /**
     * 同步Post请求，queryParam传查询参数，formDataParams传表单参数。支持文件上传
     *
     * @param url         基本请求地址，例如：http://127.0.0.1:8081
     * @param queryParams 请求的查询参数，键值对形式
     * @param formData    请求的FormData参数，键值对形式，其中包含文件字段
     * @param args        请求的查询路径，例如：new String[]{"user", "getUser", "123"}
     * @return 请求结果的字符串
     */
    public static String postSyncRequestFormDataFileAvailable(String url, Map<String, String> queryParams, Map<String, Object> formData, String... args) {
        List<String> result = new ArrayList<>();
        StringBuilder address = new StringBuilder(url);
        for (String arg : args) {
            address.append("/").append(arg);
        }

        // 添加查询参数到 URL
        HttpUrl.Builder urlBuilder = HttpUrl.parse(address.toString()).newBuilder();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            urlBuilder.addQueryParameter(key, value);
        }
        final String finalAddress = urlBuilder.build().toString();

        Log.d("pan", finalAddress);

        new Thread(new Runnable() {
            @Override
            public void run() {
                client = getInstance();
                Log.d("同步 POST 请求地址：", finalAddress);

                MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                // 添加FormData参数到请求体
                for (Map.Entry<String, Object> entry : formData.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof File) {
                        File file = (File) value;
                        // 添加文件字段
                        requestBodyBuilder.addFormDataPart(key, file.getName(), RequestBody.create(MediaType.parse("multipart/form-data"), file));
                    } else {
                        // 添加普通字段
                        requestBodyBuilder.addFormDataPart(key, String.valueOf(value));
                    }
                }

                RequestBody requestBody = requestBodyBuilder.build();

                request = new Request.Builder()
                        .url(finalAddress)
                        .post(requestBody)
                        .addHeader("device-platform", "android")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String res = response.body().string();
                    result.add(res);
                    Log.d("HttpUtil", "同步 POST 请求成功！");
                    Log.d("请求对象：", res);
                } catch (Exception e) {
                    Log.d("HttpUtil", "同步 POST 请求失败！");
                    e.printStackTrace();
                }
            }
        }).start();

        // 等待结果返回
        int count = 0;
        while (result.size() == 0 && count < 300) {
            try {
                TimeUnit.MILLISECONDS.sleep(10); // 等待 10 毫秒
                count++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (count < 300) {
            return result.get(0);
        } else {
            Log.d("HttpUtil", "等待返回值超时");
            return null;
        }
    }



    /**
     * 异步上传文件
     *
     * @param url        上传地址
     * @param compressedBytes 上传文件的字节数组
     * @param key 服务器接收的参数名称
     * @param callback   上传结果的回调
     */
    public static void uploadFile(String url, byte[] compressedBytes/*String filePath*/, String key, OkHttpCallback callback) {
//        File file = new File(filePath);
//
//        if (!file.exists()) {
//            Log.e("HttpUtil", "上传的文件不存在");
//            return;
//        }
//        MediaType mediaType = MediaType.parse("application/octet-stream");// 设置上传文件的媒体类型
//        RequestBody requestBody = RequestBody.create(mediaType, file);// 构造RequestBody请求体对象
//        Request request = new Request.Builder()
//                .url(url)
//                .post(new MultipartBody.Builder()
//                        .setType(MultipartBody.FORM)
//                        .addFormDataPart(key, file.getName(), requestBody)// 添加上传的文件参数
//                        .addFormDataPart("time",TimeUtils.getCurrentTimestampAsString())
//                        .build())
//                .build();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(key, System.currentTimeMillis() +".jpeg"/*TimeUtils.getCurrentTimestampAsString()*/, RequestBody.create(MediaType.parse("image/jpeg"), compressedBytes))
//                .addFormDataPart("time", String.valueOf(System.currentTimeMillis()))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        MyCallBack myCallback = new MyCallBack(callback);
        getInstance().newCall(request).enqueue(myCallback);
    }



    /**
     * 获得异步get请求对象
     * @param url      请求地址
     * @param callback 实现callback接口
     */
    private static void doAsyncGet(String url, OkHttpCallback callback) {
        MyCallBack myCallback = new MyCallBack(callback);
        client = getInstance();
        request = new Request.Builder()
                .url(url)
                .get()
                .build();
        client.newCall(request).enqueue(myCallback);
    }


    /**
     * 异步post请求
     *
     * @param url        上传地址
     * @param message 上传的消息
     * @param key 服务器接收的参数名称
     * @param callback   上传结果的回调
     */
    public static void asyncPost(String url, String message, String key, OkHttpCallback callback){
        Request request = new Request.Builder()
                .url(url)
                .build();

        MyCallBack myCallback = new MyCallBack(callback);
        getInstance().newCall(request).enqueue(myCallback);
    }

    /**
     * 异步post请求
     * @param url
     * @param json
     * @param args
     */
    public static void dataPostAsyncRequest(OkHttpCallback callback,String url, String json, String... args) {
        List<String> result = new ArrayList<>();
        String address = url;
        for (int i = 0; i < args.length; i++) {
            address = address + "/" + args[i];
        }
        final String finalAddress = address;

        OkHttpClient client = getInstance();
//        Log.d("异步post请求地址：", finalAddress);
        FormBody.Builder formBody = new FormBody.Builder();
//        Log.d("传给服务器的json", json);
        formBody.add("json", json);
        Request request = new Request.Builder()
                .url(finalAddress)
                .post(formBody.build())
                .addHeader("device-platform", "android")
                .build();
        MyCallBack myCallback = new MyCallBack(callback);
        client.newCall(request).enqueue(myCallback);
    }


    /**
     * 发起WebSocket连接
     *
     * @param url       WebSocket服务器地址
     * @param listener  WebSocket监听器
     */
    public static void connectWebSocket(String url, WebSocketListener listener) {
        //单独为WebSocket长连接创建一个实例和线程池，避免冲突
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(TimeOut, TimeUnit.SECONDS)
                .connectTimeout(TimeOut, TimeUnit.SECONDS)
                .writeTimeout(TimeOut, TimeUnit.SECONDS)
                .dispatcher(new Dispatcher(new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>())))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();
    }
}
