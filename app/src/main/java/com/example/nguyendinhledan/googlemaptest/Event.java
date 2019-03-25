package com.example.nguyendinhledan.googlemaptest;

import org.json.JSONObject;

import java.util.ArrayList;

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
    private int slots;
    private ArrayList<String> nearbyCarparks = new ArrayList<String>();


    public Event(String name, String description, String img) {
        this.name = name;
        this.description = description;
        this.img = "https:" + img;
    }

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

    public int getSlots(){
        return slots;
    }

    public void setSlots(int slots){
        this.slots = slots;
    }

    public String getDatetimeStart() {
        return datetimeStart;
    }

    public String getDatetimeEnd() {
        return datetimeEnd;
    }

    public ArrayList<String> getNearbyCarparks() {
        return nearbyCarparks;
    }

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

    public void setNearbyCarparks(ArrayList<String> nearbyCarparks) {
        this.nearbyCarparks = nearbyCarparks;
    }

    public void addNearbyCarparks(String carpark){
        nearbyCarparks.add(carpark);
    }

    public void takeOutNearbyCarpark(String carpark){
        nearbyCarparks.remove(carpark);
    }

    public boolean hasCarpark(String carpark){
        return nearbyCarparks.contains(carpark);
    }
}
