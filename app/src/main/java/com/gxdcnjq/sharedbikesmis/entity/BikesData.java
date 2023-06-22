package com.gxdcnjq.sharedbikesmis.entity;

import java.util.List;

public class BikesData {
    private String code;
    private String msg;
    private List<Bike> data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<Bike> getData() {
        return data;
    }

    public void setData(List<Bike> data) {
        this.data = data;
    }
}
