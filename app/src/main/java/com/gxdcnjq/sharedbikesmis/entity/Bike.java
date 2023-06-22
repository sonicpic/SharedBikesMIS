package com.gxdcnjq.sharedbikesmis.entity;

public class Bike {
    private int bikeId;
    private String bikeMac;
    private String location;
    private int status;

    public int getBikeId() {
        return bikeId;
    }

    public void setBikeId(int bikeId) {
        this.bikeId = bikeId;
    }

    public String getBikeMac() {
        return bikeMac;
    }

    public void setBikeMac(String bikeMac) {
        this.bikeMac = bikeMac;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
