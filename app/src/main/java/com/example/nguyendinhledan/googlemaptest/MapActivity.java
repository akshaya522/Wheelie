package com.example.nguyendinhledan.googlemaptest;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;
import com.google.maps.android.kml.KmlPolygon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private static final String TAG = "MapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 12f;

    private HashMap<String, Integer> carparkSlots;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        getLocationPermission();
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: Getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            initMap();
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: Gotten location permission result");
        mLocationPermissionGranted = false;

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            Log.d(TAG, "onRequestPermissionsResult: " + permissions.length);
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                Log.d(TAG, "onRequestPermissionsResult: Permission granted");
                initMap();
            }
        }
    }

    private void initMap() {
        Log.d(TAG, "initMap: Initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: Getting device location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: Security Exception " + e.getMessage());
        }
    }


    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: Moving the camera to: lat: " + latLng.latitude + " long: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        //MarkerOptions options = new MarkerOptions()
                //.position()
    }





    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is ready");
        Toast.makeText(this, "Map is ready!", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
        new MarkerTaskClass().execute();


    }

    private class MarkerTaskClass extends AsyncTask<Void, Void, JSONArray> {
        private static final String TAG = "MarkerTaskClass";
        ProgressDialog pd;

        @Override
        protected JSONArray doInBackground(Void... voids) {
            InputStream inputStream = getResources().openRawResource(R.raw.carparks_location);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int ctr;
            Log.d(TAG, "doInBackground: Got here");
            try {
                ctr = inputStream.read();
                while (ctr != -1) {
                    byteArrayOutputStream.write(ctr);
                    ctr = inputStream.read();
                }
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "doInBackground: " + byteArrayOutputStream.toString());
            try{
                JSONObject jsonObject = new JSONObject(
                        byteArrayOutputStream.toString());
                JSONArray carparks = jsonObject.getJSONArray("carparks");
                return carparks;
            } catch (JSONException e){
                e.printStackTrace();
        }
        return null;
    }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            new JsonTaskClass().execute("https://api.data.gov.sg/v1/transport/carpark-availability");
            pd = new ProgressDialog(MapActivity.this);
            pd.setCancelable(true);
            pd.setMessage("Wait");
            pd.show();
        }

        @Override
        protected void onPostExecute(JSONArray carparks) {
            super.onPostExecute(carparks);
            Log.d(TAG, "onPostExecute: creating markers");
            Log.d(TAG, "onPostExecute: " + carparkSlots.get("HE12"));
            for (int i=0; i<carparks.length(); i++ ) {
                try {
                    JSONObject carpark = carparks.getJSONObject(i);
                    Log.d(TAG, "onPostExecute: " + carpark.getString("car_park_no"));
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(carpark.getDouble("lat"), carpark.getDouble("lon")))
                            .title("Address: " + carpark.getString("address")).snippet("Slots "
                                    + carparkSlots.get(carpark.getString("car_park_no"))));
                    } catch (JSONException e){
                    e.printStackTrace();
                }
            }
            if (pd.isShowing()){
                pd.dismiss();
            }
        }
    }

    private class  JsonTaskClass extends AsyncTask<String, String, HashMap<String, Integer>> {

        private static final String TAG = "JsonTaskClass";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute: pre-execute");
        }

        @Override
        protected void onPostExecute(HashMap<String, Integer> result) {
            super.onPostExecute(result);
            try {
                carparkSlots = result;
                Log.d(TAG, "onPostExecute: " + carparkSlots.get("HE12"));
            } catch (NullPointerException e){
                Log.d(TAG, "onPostExecute: null pointer");
            }
        }

        protected HashMap<String, Integer> doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                Log.d(TAG, "doInBackground: establish connection");

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d(TAG, "doInBackground: " + line);
                }

                JSONObject jsonObject = new JSONObject(buffer.toString());
                Log.d(TAG, "doInBackground: created json object");

                JSONArray items = jsonObject.getJSONArray("items");
                JSONObject item = items.getJSONObject(0);
                JSONArray carpark_data = item.getJSONArray("carpark_data");
                HashMap<String, Integer> slots = new HashMap<String, Integer>();
                for (int i = 0; i < carpark_data.length(); i++) {
                    JSONObject carpark = carpark_data.getJSONObject(i);
                    JSONArray info_array = carpark.getJSONArray("carpark_info");
                    JSONObject info = info_array.getJSONObject(0);
                    String number = carpark.getString("carpark_number");
                    int carpark_slots = info.getInt("lots_available");
                    slots.put(number, carpark_slots);
                }
                Log.d(TAG, "doInBackground: return slots");
                return slots;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground: Malformed URL");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground: IO Exception");
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }

                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "doInBackground: IO Exception");
                }
            }
            Log.d(TAG, "doInBackground: return null");
        return null;
        }
    }
}
