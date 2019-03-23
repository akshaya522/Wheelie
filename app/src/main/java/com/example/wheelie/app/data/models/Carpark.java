package com.example.wheelie.app.data.models;

/*
    Carpark model
 */

public class Carpark {

    private int carparkNo;
    private double lat;
    private double lng;
    private int slot;

    // constructors
    public Carpark() {}

    public Carpark(int carparkNo, double lat, double lng, int slot) {
        this.carparkNo = carparkNo;
        this.lat = lat;
        this.lng = lng;
        this.slot = slot;
    }

    // setters
    public void setCarparkNo(int carparkNo) {
        this.carparkNo = carparkNo;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    // getters
    public int getCarparkNo() {
        return carparkNo;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public int getSlot() {
        return slot;
    }
}
