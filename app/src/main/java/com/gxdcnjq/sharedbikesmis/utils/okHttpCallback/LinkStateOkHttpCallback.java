package com.gxdcnjq.sharedbikesmis.utils.okHttpCallback;

import android.app.Activity;
import android.graphics.Color;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Response;

public class LinkStateOkHttpCallback implements OkHttpCallback {

    private Activity mActivity;
    private TextView mTextView;

    public LinkStateOkHttpCallback(Activity activity, TextView textView) {
        mActivity = activity;
        mTextView = textView;
    }

    @Override
    public void onFailure(IOException e) {
        // 处理请求失败的情况
        // 发送请求失败，更新卡片文本信息为连接失败的信息
        // 可以使用 runOnUiThread 方法更新 UI 界面
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextView.setText("连接失败");
                mTextView.setTextColor(Color.RED);
                // 更新卡片文本信息为连接失败的信息
            }
        });
        e.printStackTrace();
    }

    @Override
    public void onSuccess(Response response) {
        // 处理请求成功的情况
        // 发送请求成功，更新卡片文本信息为连接成功的信息
        // 可以使用 runOnUiThread 方法更新 UI 界面
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextView.setText("已连接");
                mTextView.setTextColor(Color.GREEN);
                // 更新卡片文本信息为连接成功的信息
            }
        });
    }
}
