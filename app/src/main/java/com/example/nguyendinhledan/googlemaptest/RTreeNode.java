package com.example.nguyendinhledan.googlemaptest;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RTreeNode {
    private static final String TAG = "RTreeNode";

    private static final double TOP_LEFT_LAT_SING = 1.4700293498673866;
    private static final double TOP_LEFT_LNG_SING = 103.64762766456545;
    private static final double BOT_RIGHT_LAT_SING = 1.2369616516770854;
    private static final double BOT_RIGHT_LNG_SING = 103.97880108967752;

    private double topLeftLat;
    private double topLeftLng;

    private double botRightLat;
    private double botRightLng;

    public RTreeNode topLeft;
    public RTreeNode topRight;
    public RTreeNode botLeft;
    public RTreeNode botRight;

    private ArrayList<String> data;

    public RTreeNode() {
        topLeftLat = TOP_LEFT_LAT_SING;
        topLeftLng = TOP_LEFT_LNG_SING;
        botRightLat = BOT_RIGHT_LAT_SING;
        botRightLng = BOT_RIGHT_LNG_SING;

        data = new ArrayList<String>();
    }

    public RTreeNode(double topLeftLat, double topLeftLng, double botRightLat, double botRightLng) {
        this.topLeftLat = topLeftLat;
        this.topLeftLng = topLeftLng;
        this.botRightLat = botRightLat;
        this.botRightLng = botRightLng;

        data = new ArrayList<String>();
    }

    public double getTopLeftLat() {
        return topLeftLat;
    }

    public double getTopLeftLng() {
        return topLeftLng;
    }

    public double getBotRightLat() {
        return botRightLat;
    }

    public double getBotRightLng() {
        return botRightLng;
    }

    public void initTree(int level) {
        if (level == 0) {
            return;
        } else {
            topLeft = new RTreeNode(topLeftLat,
                    topLeftLng,
                    Math.abs(topLeftLat - botRightLat) / 2 + botRightLat,
                    Math.abs(topLeftLng - botRightLng) / 2 + topLeftLng);
            topLeft.initTree(level - 1);

            topRight = new RTreeNode(topLeftLat,
                    Math.abs(topLeftLng - botRightLng) / 2 + topLeftLng,
                    Math.abs(topLeftLat - botRightLat) / 2 + botRightLat,
                    botRightLng);
            topRight.initTree(level - 1);

            botLeft = new RTreeNode(Math.abs(topLeftLat - botRightLat) / 2 + botRightLat,
                    topLeftLng,
                    botRightLat,
                    Math.abs(topLeftLng - botRightLng) / 2 + topLeftLng);

            botLeft.initTree(level - 1);

            botRight = new RTreeNode(Math.abs(topLeftLat - botRightLat) / 2 + botRightLat,
                    Math.abs(topLeftLng - botRightLng) / 2 + topLeftLng,
                    botRightLat,
                    botRightLng);
            botRight.initTree(level - 1);
        }
    }

    public static void traverse(RTreeNode node) {
        if (node.topLeft != null) {
            traverse(node.topLeft);
        }
        if (node.topRight != null) {
            traverse(node.topRight);
        }
        System.out.println(node.getTopLeftLat() + "," + node.getTopLeftLng());
        System.out.println(node.getBotRightLat() + "," + node.getBotRightLng());

        if (node.botLeft != null) {
            traverse(node.botLeft);
        }
        if (node.botRight != null) {
            traverse(node.botRight);
        }

    }

    public boolean isLeaf() {
        if ((topLeft == null) && (topRight == null) && (botLeft == null) && (botRight == null)) {
            return true;
        }
        return false;
    }

    public RTreeNode findNode(double lat, double lng) {
        if (lat < Math.abs(topLeftLat - botRightLat) / 2 + botRightLat) {
            if (lng < Math.abs(topLeftLng - botRightLng) / 2 + topLeftLng) {
                if (botLeft.isLeaf()) {
                    return botLeft;
                } else {
                    return botLeft.findNode(lat, lng);
                }
            } else {
                if (botRight.isLeaf()) {
                    return botRight;
                } else {
                    return botRight.findNode(lat, lng);
                }
            }
        } else {
            if (lng < Math.abs(topLeftLng - botRightLng) / 2 + topLeftLng) {
                if (topLeft.isLeaf()) {
                    return topLeft;
                } else {
                    return topLeft.findNode(lat, lng);
                }
            } else {
                if (topRight.isLeaf()) {
                    return topRight;
                } else {
                    return topRight.findNode(lat, lng);
                }
            }
        }
    }

    public void putData(String data){
        this.data.add(data);
    }

    public ArrayList<String> getData(){
        return data;
    }


    public void populateTree(JSONArray jsonArray){
        Log.d(TAG, "populateTree: populating tree");
        RTreeNode temp;
        for (int i=0; i<jsonArray.length(); i++){
            try {
                JSONObject carpark = jsonArray.getJSONObject(i);
                temp = findNode(carpark.getDouble("lat"), carpark.getDouble("lon"));
                temp.putData(carpark.getString("car_park_no"));
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
}
