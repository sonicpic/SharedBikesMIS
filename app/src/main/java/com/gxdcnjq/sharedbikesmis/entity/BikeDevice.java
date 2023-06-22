package com.gxdcnjq.sharedbikesmis.entity;

import android.bluetooth.BluetoothDevice;

public class BikeDevice {
    private BluetoothDevice device;
    private String aliasName;
    public BikeDevice(BluetoothDevice device, String aliasName) {
        this.device = device;
        this.aliasName = aliasName;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

}
