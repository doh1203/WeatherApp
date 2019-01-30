package com.example.doh12.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Dennis Li on 1/28/2019.
 */

public class GetWeatherActivity extends AppCompatActivity {

    // UI references.
    private TextView mWelcomeView;
    private TextView mLocationView;
    private TextView mWeatherView;
    private TextView mMeasurementView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_weather);
        // Set up the get weather display.
        mWelcomeView = findViewById(R.id.welcome);
        mLocationView = findViewById(R.id.location);
        mWeatherView = findViewById(R.id.weather);
        mMeasurementView = findViewById(R.id.measurements);

        Bundle weatherParams = getIntent().getExtras();
        if (weatherParams != null) {
            // Set the welcome text
            String name = weatherParams.getString("name");
            mWelcomeView.setText(String.format("Welcome, %s!", name));
            double lat = weatherParams.getDouble("lat");
            double lon = weatherParams.getDouble("lon");
            String apiKey = weatherParams.getString("apiKey");
            String dataStr = "";
            try {
                Callable<String> c = new callableWeatherData(lat, lon, apiKey);
                ExecutorService pool = Executors.newFixedThreadPool(1);
                Future<String> data = pool.submit(c);
                dataStr = data.get();
            } catch (Exception e) {
                Log.d("NetworkingThread", "Callable Error: " + e + ": " + e.getMessage());
            }
            if (dataStr != null && dataStr != "") {
                try {
                    JSONObject dataObj = new JSONObject(dataStr);
                    // Set the location textview
                    String location = dataObj.getString("name");
                    String country = dataObj.getJSONObject("sys").getString("country");
                    String locationText = String.format("%s, %s", location, country);
                    mLocationView.setText(locationText);
                    // Set the weather textview
                    JSONArray mainWeatherArr = dataObj.getJSONArray("weather");
                    String weatherString = "";
                    for (int i = 0; i < mainWeatherArr.length(); i++) {
                        String weatherTemplate = "%s: %s";
                        JSONObject obj = mainWeatherArr.getJSONObject(i);
                        weatherString += "\n" + String.format(weatherTemplate,
                                obj.getString("main"), obj.getString("description"));
                    }
                    mWeatherView.setText(weatherString);
                    // Set the measurement textview
                    String measurementsString = "Temperature: %s\n    High: %s\n    Low: %s\nPressure: %s\nHumidity: %s\nWind Speed: %s";
                    JSONObject measurement = dataObj.getJSONObject("main");
                    measurementsString = String.format(
                            measurementsString,
                            Double.toString(measurement.getDouble("temp")),
                            Double.toString(measurement.getDouble("temp_max")),
                            Double.toString(measurement.getDouble("temp_min")),
                            Double.toString(measurement.getDouble("pressure")),
                            Double.toString(measurement.getDouble("humidity")),
                            Double.toString(dataObj.getJSONObject("wind").getDouble("speed")));
                    mMeasurementView.setText(measurementsString);
                } catch (JSONException e) {
                    String error = "Malformed JSON data";
                    Log.d("JSON ERROR", error);
                    mWelcomeView.setAllCaps(true);
                    mWelcomeView.setText("ERROR");
                    mLocationView.setAllCaps(true);
                    mLocationView.setText("ERROR: Unable to retrieve weather data: " + error);
                }
            } else {
                mWelcomeView.setAllCaps(true);
                mWelcomeView.setText("ERROR");
                mLocationView.setAllCaps(true);
                mLocationView.setText("ERROR: Unable to retrieve weather data: data retrieval error.");
            }
        } else {
            mWelcomeView.setAllCaps(true);
            mWelcomeView.setText("ERROR");
            mLocationView.setAllCaps(true);
            mLocationView.setText("ERROR: Unable to retrieve weather data.");
        }

        Button mSignOutButton = findViewById(R.id.get_weather_activity_sign_out_button);
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    private class callableWeatherData implements Callable {
        double lat;
        double lon;
        String apiKey;

        public callableWeatherData(double lat, double lon, String apiKey) {
            this.lat = lat;
            this.lon = lon;
            this.apiKey = apiKey;
        }

        @Override
        public String call() {
            return getWeatherData(lat, lon, apiKey);
        }

        private String getWeatherData(double lat, double lon, String apiKey) {
            try {
                String weatherUrl = String.format(
                        "http://api.openweathermap.org/data/2.5/weather?units=metric&lat=%1$s&lon=%2$s&appid=%3$s",
                        URLEncoder.encode(Double.toString(lat), "utf-8"),
                        URLEncoder.encode(Double.toString(lon), "utf-8"),
                        URLEncoder.encode(apiKey, "utf-8"));
                URL weatherApi = new URL(weatherUrl);
                HttpURLConnection weatherConnection = (HttpURLConnection) weatherApi.openConnection();
                weatherConnection.setRequestMethod("GET");
                StringBuilder returnString = new StringBuilder();
                try {
                    BufferedReader wd = new BufferedReader(new InputStreamReader(weatherConnection.getInputStream()));
                    String l;
                    while ((l = wd.readLine()) != null) {
                        returnString.append(l);
                    }
                } finally {
                    weatherConnection.disconnect();
                }
                return returnString.toString();
            } catch (Exception e) {
                Log.d("CONNECTION ERROR", "Error getting weather data: " + e + ": " + e.getMessage());
            }
            return null;
        }
    }



    private void signOut() {
        Intent loginActivity = new Intent(this, LoginActivity.class);
        this.startActivity(loginActivity);
    }
}
