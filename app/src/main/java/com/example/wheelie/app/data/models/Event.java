package com.example.wheelie.app.data.models;

import java.util.ArrayList;

/*
    Event model
 */

public class Event {

    private String name;
    private String description;
    private String img;
    private String url;
    private String address;
    private double lat;
    private double lng;
    private String datetimeStart;
    private String datetimeEnd;
    private int slot;
    private ArrayList<String> nearbyCarparks;

    // constructors
    public Event() {}

    public Event(String name, String description, String img, String url, String address,
                 double lat, double lng, String datetimeStart, String datetimeEnd, int slot, ArrayList<String> nearbyCarparks) {
        this.name = name;
        this.description = description;
        this.img = img;
        this.url = url;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.datetimeStart = datetimeStart;
        this.datetimeEnd = datetimeEnd;
        this.slot = slot;
        this.nearbyCarparks = nearbyCarparks;
    }

    // setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setDatetimeStart(String datetimeStart) {
        this.datetimeStart = datetimeStart;
    }

    public void setDatetimeEnd(String datetimeEnd) {
        this.datetimeEnd = datetimeEnd;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public void setNearbyCarparks(ArrayList<String> nearbyCarparks) {
        this.nearbyCarparks = nearbyCarparks;
    }

    // getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImg() {
        return img;
    }

    public String getUrl() {
        return url;
    }

    public String getAddress() {
        return address;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getDatetimeStart() {
        return datetimeStart;
    }

    public String getDatetimeEnd() {
        return datetimeEnd;
    }

    public int getSlot() {
        return slot;
    }

    public ArrayList<String> getNearbyCarparks() {
        return nearbyCarparks;
    }
}
