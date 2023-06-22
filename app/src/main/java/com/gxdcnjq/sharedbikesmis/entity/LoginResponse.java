package com.gxdcnjq.sharedbikesmis.entity;

public class LoginResponse {
    private String code;
    private String msg;
    private UserResponse data;

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


    public UserResponse getData() {
        return data;
    }

    public void setData(UserResponse data) {
        this.data = data;
    }
}



