package com.trashcanlocaion.toyou;

public class Location {
    private double latitude;
    private double longitude;
    private String locationName;
    private String locationDetails;

    public Location(double latitude, double longitude, String locationName, String locationDetails) {
        this.locationDetails = locationDetails;
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

    public String getLocationDetails() { return this.locationDetails; }
}
