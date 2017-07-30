package com.example.puttipong.aroundme.dao;

/**
 * Created by puttipong on 7/30/17.
 */

public class LocationSQLite {
    private String placeId;

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    private String placeName;

    public LocationSQLite(String placeId, String placeName, String latitude, String longtitude) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    private String latitude;
    private String longtitude;
}
