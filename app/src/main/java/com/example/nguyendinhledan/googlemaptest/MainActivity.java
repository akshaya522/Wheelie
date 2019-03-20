package com.example.nguyendinhledan.googlemaptest;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    private TextView txtJson;
    private ProgressDialog pd;

    private static final String TAG = "MainActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isServicesOK()){
            init();
        }
    }

    private void init(){
        Button btnMap = (Button) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
        Button btnTest = (Button) findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new JsonTask().execute("https://api.data.gov.sg/v1/transport/carpark-availability");
            }
        });
        txtJson = (TextView) findViewById(R.id.tvJsonItem);
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
    private class  JsonTask extends AsyncTask<String, String, String> {

        private static final String TAG = "JsonTask";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please Wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params){

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try{
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                Log.d(TAG, "doInBackground: establish connection");

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null){
                    buffer.append(line + "\n");
                    Log.d(TAG, "doInBackground: " + line);
                }

                Log.d(TAG, "doInBackground: got here");

                return buffer.toString();
            }catch (MalformedURLException e){
                e.printStackTrace();
                Log.d(TAG, "doInBackground: Malformed URL");
            } catch (IOException e){
                e.printStackTrace();
                Log.d(TAG, "doInBackground: IO Exception");
            } finally {
                if (connection != null){
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
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "onPostExecute: " + result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            try{
                txtJson.setText(result);
            } catch (NullPointerException e){
                Log.d(TAG, "onPostExecute: null pointer");
            }

        }
    }
}


