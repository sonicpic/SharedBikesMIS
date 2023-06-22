package com.gxdcnjq.sharedbikesmis.utils.okHttpCallback;

import java.io.IOException;

import okhttp3.Response;

public class ImageOkHttpCallback implements OkHttpCallback {

    @Override
    public void onFailure(IOException e) {
        // 处理请求失败的情况
        e.printStackTrace();
    }

    @Override
    public void onSuccess(Response response) {
        // 处理请求成功的情况
        try {
;
//            String responseData = response.body().string();
//            // 处理服务器返回的数据
////            Log.d("Pan","pose响应体: "+responseData);
//            Gson gson = new Gson();
//            ResultCoordinates resultCoordinates = gson.fromJson(responseData, ResultCoordinates.class);
////            Log.d("Pan",resultCoordinates.toString());
////            Log.d("Pan",String.valueOf(resultCoordinates.getX()));
////            Log.d("Pan",String.valueOf(resultCoordinates.getY()));
//            ControlCenter.getInstance().moveArm(1-resultCoordinates.getX(),resultCoordinates.getY());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
