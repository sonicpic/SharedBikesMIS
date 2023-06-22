package com.gxdcnjq.sharedbikesmis.entity;

import java.util.List;

public class TrackRecordResponse {
    private String code;
    private String msg;
    private List<TrackRecord> data;

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

    public List<TrackRecord> getData() {
        return data;
    }

    public void setData(List<TrackRecord> data) {
        this.data = data;
    }
}
