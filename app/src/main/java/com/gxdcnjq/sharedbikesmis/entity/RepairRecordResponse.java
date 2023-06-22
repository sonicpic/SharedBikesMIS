package com.gxdcnjq.sharedbikesmis.entity;

import java.util.List;

public class RepairRecordResponse {
    private String code;
    private String msg;
    private List<RepairRecord> data;

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

    public List<RepairRecord> getData() {
        return data;
    }

    public void setData(List<RepairRecord> data) {
        this.data = data;
    }
}
