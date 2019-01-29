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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
        setContentView(R.layout.activity_register);
        // Set up the get weather display.
        mWelcomeView = findViewById(R.id.welcome);
        mLocationView = findViewById(R.id.location);
        mWeatherView = findViewById(R.id.weather);
        mMeasurementView = findViewById(R.id.measurements);

        Bundle weatherParams = getIntent().getExtras();
        if (weatherParams != null) {
            // Set the welcome text
            String name = weatherParams.getString("name");
            mWelcomeView.setText("Welcome, %s!".format(name));
            double lat = weatherParams.getDouble("lat");
            double lon = weatherParams.getDouble("lon");
            String apiKey = weatherParams.getString("apiKey");
            String data = getWeatherData(lat, lon, apiKey);
            if (data != null) {
                try {
                    JSONObject dataObj = new JSONObject(data);
                    // Set the location textview
                    String location = dataObj.getString("name");
                    String country = dataObj.getJSONObject("sys").getString("country");
                    String locationText = "%s, %s".format(location).format(country);
                    mLocationView.setText(locationText);
                    // Set the weather textview
                    JSONArray mainWeatherArr = dataObj.getJSONArray("weather");
                    String weatherString = "";
                    for (int i = 0; i < mainWeatherArr.length(); i++) {
                        String weatherTemplate = "%s: %s";
                        JSONObject obj = mainWeatherArr.getJSONObject(i);
                        weatherString += "\n" + weatherTemplate.format(obj.getString("main")).format(obj.getString("description"));
                    }
                    mWeatherView.setText(weatherString);
                    // Set the measurement textview
                    String measurementsString = "Temperature: %s\n    High: %s\n    Low: %s\nPressure: %s\nHumidity: %s\nWind Speed: %s";
                    JSONObject measurement = dataObj.getJSONObject("main");
                    measurementsString = measurementsString.format(
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

    private String readStream(InputStream in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int ch = in.read();
            while (ch != -1) {
                out.write(ch);
                ch = in.read();
            }
            return out.toString();
        } catch (IOException e) {
            return "";
        }
    }

    private String getWeatherData(double lat, double lon, String apiKey) {
        String weatherUrl = "api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s"
                .format(Double.toString(lat)).format(Double.toString(lon)).format(apiKey);
        try {
            URL weatherApi = new URL(weatherUrl);
            HttpsURLConnection weatherConnection = (HttpsURLConnection) weatherApi.openConnection();
            String returnString;
            try {
                InputStream wd = new BufferedInputStream(weatherConnection.getInputStream());
                returnString = readStream(wd);
            } finally {
                weatherConnection.disconnect();
            }
            return returnString;
        } catch (Exception e) {
            Log.d("CONNECTION ERROR", "Error getting weather data: " + e.getMessage());
        }
        return null;
    }

    private void signOut() {
        Intent loginActivity = new Intent(this, LoginActivity.class);
        this.startActivity(loginActivity);
    }
}
