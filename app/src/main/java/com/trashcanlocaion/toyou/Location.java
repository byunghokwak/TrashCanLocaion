package com.trashcanlocaion.toyou;

public class Location {
    private double latitude;
    private double longitude;
    private String locationName;

    public Location(double latitude, double longitude, String locationName) {
        this.locationName = locationName;
        this.latitude = latitude;   // 위도
        this.longitude = longitude; // 경도
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String getLocationName() {
        return this.locationName;
    }
}
