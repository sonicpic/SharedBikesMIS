package com.gxdcnjq.sharedbikesmis.entity;

public class UserForReg {
    Integer id;
    private String username;
    private String password;
    private String phone;

    public UserForReg(String username, String password, String phone) {
        this.username = username;
        this.password = password;
        this.phone = phone;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
