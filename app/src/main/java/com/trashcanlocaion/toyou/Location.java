package com.trashcanlocaion.toyou;

public class Location {
    private double latitude;
    private double longitude;
    private String locationName;
    private String locationDetails;
    private String ward;

    public Location(double latitude, double longitude, String locationName, String locationDetails, String ward) {
        this.locationDetails = locationDetails;
        this.locationName = locationName;
        this.latitude = latitude;   // 위도
        this.longitude = longitude; // 경도
        this.ward = ward;
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

    public String getWard() { return this.ward; }
}
