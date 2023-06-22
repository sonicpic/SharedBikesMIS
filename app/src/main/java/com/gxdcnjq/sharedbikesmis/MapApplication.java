package com.gxdcnjq.sharedbikesmis;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.services.core.ServiceSettings;
import com.gxdcnjq.sharedbikesmis.entity.BikeDevice;

public class MapApplication extends Application {

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BikeDevice currentBikeDevice = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = this;
        //定位隐私政策同意
        AMapLocationClient.updatePrivacyShow(context,true,true);
        AMapLocationClient.updatePrivacyAgree(context,true);
        //地图隐私政策同意
        MapsInitializer.updatePrivacyShow(context,true,true);
        MapsInitializer.updatePrivacyAgree(context,true);
        //搜索隐私政策同意
        ServiceSettings.updatePrivacyShow(context,true,true);
        ServiceSettings.updatePrivacyAgree(context,true);
    }

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public void setmBluetoothAdapter(BluetoothAdapter mBluetoothAdapter) {
        this.mBluetoothAdapter = mBluetoothAdapter;
    }

    public BikeDevice getCurrentBikeDevice() {
        return currentBikeDevice;
    }

    public void setCurrentBikeDevice(BikeDevice currentBikeDevice) {
        this.currentBikeDevice = currentBikeDevice;
    }

}

