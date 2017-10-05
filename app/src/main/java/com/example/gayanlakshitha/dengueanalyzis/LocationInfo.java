package com.example.gayanlakshitha.dengueanalyzis;

/**
 * Created by Gayan Lakshitha on 9/19/2017.
 */

public class LocationInfo {
    private String location;
    private boolean risk;
    private double longitude;
    private double latitude;
    private int patients;
    private int deaths;

    public LocationInfo()
    {

    }

    public LocationInfo(String location, boolean risk, double longitude, double latitude, int patients, int deaths) {
        this.location = location;
        this.risk = risk;
        this.longitude = longitude;
        this.latitude = latitude;
        this.patients = patients;
        this.deaths = deaths;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isRisk() {
        return risk;
    }

    public void setRisk(boolean risk) {
        this.risk = risk;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getPatients() {
        return patients;
    }

    public void setPatients(int patients) {
        this.patients = patients;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
}
