package com.example.nguyendinhledan.googlemaptest;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog pd;
    private ArrayList<Event> eventList = new ArrayList<>();
    private HashMap<String, Integer> carparkSlots;

    private JSONArray carparkLocations;

    private HashMap<String, LatLng> carparkLocationsByName = new HashMap<>();

    private RTreeNode root;

    private Location eventLocation = new Location("");

    private EventListAdapter adapter;

    private static final String TAG = "MainActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;

    private boolean resume = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isServicesOK();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        Log.d(TAG, "onCreate: initiating r tree");
        root = new RTreeNode();
        root.initTree(4);
        Log.d(TAG, "onCreate: getting event list");
        new CarParkLocationTask().execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (resume){
            updateCarparkEvent();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_settings: {
                startActivity(new Intent(this, SettingActivity.class));

                break;
            }
        }
        return true;
    }

    public void initListView(){
        //search bar
        EditText filter = (EditText) findViewById(R.id.search_filter);

        Log.d(TAG, "initListView: adding carpark to event");
        addCarparkToEvent();
        Log.d(TAG, "initListView: initiating list view");
        ListView mListView = (ListView) findViewById(R.id.eventListView);
        adapter = new EventListAdapter(MainActivity.this, R.layout.custom_event_view, eventList);

        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                Log.d(TAG, "onItemClick: pressed at " + position);

                Event temp = eventList.get(position);
                intent.putExtra("event_name", temp.getName());
                intent.putExtra("event_lat", temp.getLat());
                intent.putExtra("event_lng", temp.getLng());
                intent.putExtra("event_address", temp.getAddress());
                intent.putExtra("event_location", temp.getLocation());
                intent.putExtra("event_description", temp.getDescription());

                ArrayList<String> nearbyCarpark = temp.getNearbyCarparks();
                intent.putExtra("event_number_of_carpark", nearbyCarpark.size());

                try {
                    for (int i = 0; i < nearbyCarpark.size(); i++) {
                        Log.d(TAG, "onItemClick: putting carpark " + nearbyCarpark.get(i));
                        for (int j = 0; j < carparkLocations.length(); j++) {
                            if (carparkLocations.getJSONObject(j).getString("car_park_no").equals(nearbyCarpark.get(i))) {

                                intent.putExtra("event_carpark_name" + i, carparkLocations.getJSONObject(j).getString("car_park_no"));
                                intent.putExtra("event_carpark_lat" + i, carparkLocations.getJSONObject(j).getDouble("lat"));
                                intent.putExtra("event_carpark_lng" + i, carparkLocations.getJSONObject(j).getDouble("lon"));
                                intent.putExtra("event_carpark_address"+ i, carparkLocations.getJSONObject(j).getString("address"));
                                intent.putExtra("event_carpark_rates"+i, carparkLocations.getJSONObject(j).getString("rates"));
                                intent.putExtra("event_carpark_type"+i, carparkLocations.getJSONObject(j).getString("type"));
                            }
                        }
                    }
                } catch (JSONException e){
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });

        filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: "+ s);
                (MainActivity.this).adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        resume = true;
    }

    public void addCarparkToEvent(){
        RTreeNode temp;
        Event event;
        int slots;
        LatLng carparkLatLng;
        Location carparkLocation = new Location("");
        root.populateTree(carparkLocations);
        for (int i=0; i<eventList.size(); i++){
            slots=0;
            event = eventList.get(i);
            eventLocation.setLatitude(event.getLat());
            eventLocation.setLongitude(event.getLng());
            temp = root.findNode(event.getLat(), event.getLng());

            for (String carpark : temp.getData()){
                carparkLatLng = carparkLocationsByName.get(carpark);
                carparkLocation.setLongitude(carparkLatLng.longitude);
                carparkLocation.setLatitude(carparkLatLng.latitude);
                if (eventLocation.distanceTo(carparkLocation) < PreferenceManager.getDefaultSharedPreferences(this).
                                                                getInt(getResources().getString(R.string.radius_preference), 500)){
                    Log.d(TAG, "addCarparkToEvent: " + carpark);
                    event.addNearbyCarparks(carpark);
                    try{
                        slots += carparkSlots.get(carpark);
                    } catch (NullPointerException e){ //carparks that don't have slots
                        e.printStackTrace();
                    }
                }
            }
            event.setSlots(slots);
        }
    }

    public void updateCarparkEvent(){
        Log.d(TAG, "updateCarparkEvent: updating carpark event");
        RTreeNode temp;
        Iterator iterator;
        Location carparkLocation = new Location("");
        LatLng carparkLatLng;
        for (Event event : eventList){
            eventLocation.setLatitude(event.getLat());
            eventLocation.setLongitude(event.getLng());

            temp = root.findNode(event.getLat(), event.getLng());

            ArrayList<String> carparks = event.getNearbyCarparks();
            iterator = carparks.iterator();
            while (iterator.hasNext()){
                String carpark = (String) iterator.next();

                carparkLatLng = carparkLocationsByName.get(carpark);
                carparkLocation.setLatitude(carparkLatLng.latitude);
                carparkLocation.setLongitude(carparkLatLng.longitude);

                if (eventLocation.distanceTo(carparkLocation) > PreferenceManager.getDefaultSharedPreferences(this).getInt(getResources().getString(R.string.radius_preference), 500)){
                    Log.d(TAG, "updateCarparkEvent: remove carpark " + carpark);
                    iterator.remove();
                    if (carparkSlots.get(carpark) != null) {
                        event.setSlots(event.getSlots() - carparkSlots.get(carpark));
                    }
                }
            }

            for (String carpark : temp.getData()){
                carparkLatLng = carparkLocationsByName.get(carpark);
                carparkLocation.setLongitude(carparkLatLng.longitude);
                carparkLocation.setLatitude(carparkLatLng.latitude);
                if (eventLocation.distanceTo(carparkLocation) < PreferenceManager.getDefaultSharedPreferences(this).getInt(getResources().getString(R.string.radius_preference), 500)){
                    if (!event.hasCarpark(carpark)) {
                        Log.d(TAG, "updateCarparkEvent: add carpark " + carpark);
                        event.addNearbyCarparks(carpark);
                        if (carparkSlots.get(carpark) != null) {
                            event.setSlots(event.getSlots() + carparkSlots.get(carpark));
                        }
                    }
                }
            }
        }



        adapter.notifyDataSetChanged();
    }



    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: Checking Google Play Service");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS){
            Log.d(TAG, "isServicesOK: Google Play service is working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");

            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }

        else {
            Toast.makeText(this, "We can't resolve your problem", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private class  EventJsonTask extends AsyncTask<String, String, ArrayList<Event>> {
        private final String basicAuth = "Basic " + Base64.encodeToString("eventsaroundsingapore:p96k9gkrb6s8".getBytes(), Base64.NO_WRAP);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        ArrayList<Event> eventsTask;

        protected Event createEvent(JSONArray events, int i, int imagePosition) throws JSONException, ParseException {
            JSONObject event;
            event = events.getJSONObject(i);
            String name = event.getString("name");
            String description = event.getString("description");
            String img = event.getJSONObject("images")
                    .getJSONArray("images")
                    .getJSONObject(0)
                    .getJSONObject("transforms")
                    .getJSONArray("transforms")
                    .getJSONObject(imagePosition)
                    .getString("url");
            Event event1 = new Event(name, description + "\n", img);
            event1.setAddress(event.getString("address") + "\n");
            event1.setLocation(event.getString("location_summary") + "\n");
            event1.setUrl(event.getString("url"));
            event1.setDatetimeStart(dateFormat.parse(event.getString("datetime_start")));
            event1.setDatetimeEnd(dateFormat.parse(event.getString("datetime_end")));
            event1.setLat(event.getJSONObject("point").getDouble("lat"));
            event1.setLng(event.getJSONObject("point").getDouble("lng"));
            return event1;
        }

        protected void addEventByDate(Event event){
            Log.d(TAG, "addEventByDate: adding event " + event.getName());
            if (eventsTask.size() == 0){
                eventsTask.add(event);
                Log.d(TAG, "addEventByDate: list empty add at 0");
            }
            else {
                if(event.getDatetimeStart().after(eventsTask.get(eventsTask.size()-1).getDatetimeStart())){
                    eventsTask.add(event);
                }
                else{
                    for (int i = eventsTask.size()-1; i>=0;i--){
                        if(event.getDatetimeStart().after(eventsTask.get(i).getDatetimeStart())){
                            eventsTask.add(i+1, event);
                            Log.d(TAG, "addEventByDate: eventList " + eventsTask.size());
                            return;
                        }
                    }
                    eventsTask.add(0, event);
                }
            }
            Log.d(TAG, "addEventByDate: eventList "+ eventsTask.size());
        }

        private static final String TAG = "EventJsonTask";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute: getting event");
        }

        protected ArrayList<Event> doInBackground(String... params){
            BufferedReader reader = null;
            HttpURLConnection connection = null;

            try{
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", basicAuth);

                InputStream stream = new BufferedInputStream(connection.getInputStream());
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null){
                    buffer.append(line + "\n");
                    Log.d(TAG, "doInBackground: " + line);
                }

                Log.d(TAG, "doInBackground: read input success");

                eventsTask = new ArrayList<>();
                JSONArray events = new JSONArray();
                try {
                    JSONObject jsonObject = new JSONObject(buffer.toString());
                    events = jsonObject.getJSONArray("events");
                } catch (JSONException e){
                    e.printStackTrace();
                }
                Log.d(TAG, "doInBackground: event jsonarray length: " + events.length());
                Event event;
                for (int i = 0; i < events.length(); i++) {
                    try {
                        event = createEvent(events, i , 3);
                        addEventByDate(event);
                    } catch (JSONException e3){
                        try{
                            event = createEvent(events, i,2);
                            addEventByDate(event);
                        } catch (JSONException e2){
                            try {
                                event = createEvent(events, i, 1);
                                addEventByDate(event);
                            }catch (JSONException e1){
                                e1.printStackTrace();
                            }catch (ParseException p1){
                                p1.printStackTrace();
                            }
                        } catch (ParseException p2){
                            p2.printStackTrace();
                        }
                    } catch (ParseException p3){
                        p3.printStackTrace();
                    }
                }
                Log.d(TAG, "doInBackground: events task size: " + eventsTask.size());
                return eventsTask;
            }catch (MalformedURLException e){
                e.printStackTrace();
                Log.d(TAG, "doInBackground: Malformed URL");
            } catch (IOException e){
                e.printStackTrace();
                Log.d(TAG, "doInBackground: IO Exception");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null){
                        reader.close();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                    Log.d(TAG, "doInBackground: IO Exception");
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Event> result) {
            super.onPostExecute(result);
            eventList = result;
            Log.d(TAG, "onPostExecute: Pass result");
            initListView();
            if (pd.isShowing()){
                pd.dismiss();
            }

        }
    }

    private class CarParkLocationTask extends AsyncTask<Void, Void, JSONArray> {
        private static final String TAG = "CarParkLocationTask";

        @Override
        protected JSONArray doInBackground(Void... voids) {
            InputStream inputStream = getResources().openRawResource(R.raw.carparks_location);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int ctr;
            Log.d(TAG, "doInBackground: Getting carpark location");
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
            Log.d(TAG, "onPreExecute: calling carpark slot");
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please Wait");
            pd.show();
        }

        @Override
        protected void onPostExecute(JSONArray carparks) {
            super.onPostExecute(carparks);
            Log.d(TAG, "onPostExecute: passing carpark locations");
            try {
                carparkLocations = carparks;
                for (int i=0; i<carparks.length();i++){
                    try {
                        JSONObject carpark = carparks.getJSONObject(i);
                        carparkLocationsByName.put(carpark.getString("car_park_no"),
                                new LatLng(carpark.getDouble("lat"), carpark.getDouble("lon")));
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "onPostExecute: pass successful");
                Log.d(TAG, "onPostExecute: carparklocationby name" + carparkLocationsByName.size());
            } catch (NullPointerException e){
                Log.d(TAG, "onPostExecute: null pointer");
                e.printStackTrace();
            }
            new CarParkSlotsTask().execute("https://api.data.gov.sg/v1/transport/carpark-availability");
        }
    }

    private class CarParkSlotsTask extends AsyncTask<String, String, HashMap<String, Integer>> {

        private static final String TAG = "CarParkSlotsTask";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute: pre-execute");
        }

        @Override
        protected void onPostExecute(HashMap<String, Integer> result) {
            super.onPostExecute(result);
            Log.d(TAG, "onPostExecute: passing carpark slots");
            try {
                carparkSlots = result;
                Log.d(TAG, "onPostExecute: carpark slots passed");
            } catch (NullPointerException e){
                Log.d(TAG, "onPostExecute: null pointer");
            }
            new EventJsonTask().execute("https://api.eventfinda.sg/v2/events.json");
        }

        protected HashMap<String, Integer> doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                Log.d(TAG, "doInBackground: establish connection");

                InputStream stream = new BufferedInputStream(connection.getInputStream());

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


