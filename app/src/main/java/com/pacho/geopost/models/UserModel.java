package com.pacho.geopost.models;

/**
 * Created by do_ma on 18/11/2017.
 */

public class UserModel {

    String username;

    String msg;

    Float distanceFromMe;

    Double lat;

    Double lon;

    public void constructor(String username, String msg, Double lat, Double lon) {
        this.username = username;
        this.msg = msg;
        this.lat = lat;
        this.lon = lon;
    }

    public void constructor(String username, String msg, Double lat, Double lon, Float distanceFromMe) {
        this.username = username;
        this.msg = msg;
        this.lat = lat;
        this.lon = lon;
        this.distanceFromMe = distanceFromMe;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Float getDistanceFromMe() {
        return distanceFromMe;
    }

    public void setDistanceFromMe(Float distanceFromMe) {
        this.distanceFromMe = distanceFromMe;
    }
}
