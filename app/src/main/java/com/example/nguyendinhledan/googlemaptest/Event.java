package com.example.nguyendinhledan.googlemaptest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class Event {
    private String name;
    private String description;
    private String img;
    private String url;
    private String address;
    private String location;
    private double lat;
    private double lng;
    private Date datetimeStart;
    private Date datetimeEnd;
    private int slots;
    private ArrayList<String> nearbyCarparks = new ArrayList<>();


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

    public String getLocation() {
        return location;
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

    public Date getDatetimeStart() {
        return datetimeStart;
    }

    public Date getDatetimeEnd() {
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

    public void setLocation(String location) {
        this.location = location;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setDatetimeStart(Date datetimeStart) {
        this.datetimeStart = datetimeStart;
    }

    public void setDatetimeEnd(Date datetimeEnd) {
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

    public int getNumberOfCarpark(){
        return nearbyCarparks.size();
    }
}
