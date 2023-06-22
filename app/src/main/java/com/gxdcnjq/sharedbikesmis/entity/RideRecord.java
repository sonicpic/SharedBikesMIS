package com.gxdcnjq.sharedbikesmis.entity;

import java.util.ArrayList;
import java.util.List;

public class RideRecord {
    String recordId;
    List<Location> locationList = new ArrayList<Location>();
    String startTime;
    String endTime;

    double distance = 0.0;

    public RideRecord(String recordId, String startTime, String endTime) {
        this.recordId = recordId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public List<Location> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<Location> locationList) {
        this.locationList = locationList;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
