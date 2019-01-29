package com.example.doh12.weatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Dennis Li on 1/27/2019.
 */

public class RegisterActivity extends AppCompatActivity {

    // Permissions
    int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mNameView;
    private EditText mLatitudeView;
    private EditText mLongitudeView;
    private EditText mApiKeyView;
    // For GPS
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("GPS", "Starting register form create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the register form.
        mEmailView = findViewById(R.id.email);
        mPasswordView = findViewById(R.id.password);
        mNameView =  findViewById(R.id.name);
        mLatitudeView = findViewById(R.id.coordinate_x);
        mLongitudeView = findViewById(R.id.coordinate_y);
        mApiKeyView = findViewById(R.id.api_key);

        Button mFindCoordinatesButton = findViewById(R.id.find_coordinates_button);
        mFindCoordinatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findCoordinates(mFusedLocationClient);
            }
        });

        Button mRegisterButton = findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void findCoordinates(FusedLocationProviderClient locationClient) {
        Log.d("GPS", "Button clicked");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("GPS", "Task started because permissions check passed");
            Task<Location> lastLocationTask = locationClient.getLastLocation();
            lastLocationTask.addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Log.d("GPS", "Successfully entered callback");
                    if (location != null) {
                        Log.d("GPS", "Returned location");
                        mLatitudeView.setText(Double.toString(location.getLatitude()));
                        mLongitudeView.setText(Double.toString(location.getLongitude()));
                    } else {
                        Log.d("GPS", "Returned null location");
                    }
                }
            });
            lastLocationTask.addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("GPS", e.getMessage());
                }
            });
            lastLocationTask.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Log.d("GPS", "Task Complete!");
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }
    }

    private void attemptRegister() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String email = mEmailView.getText().toString();
        if (email == null || email == "") {
            Log.d("Register", "Email not provided.");
            return;
        }
        String password = mPasswordView.getText().toString();
        if (password == null || password.length() < 6) {
            Log.d("Register", "Password not provided or does not meet specification.");
            return;
        }
        // Register the user in firebaseauth
        mAuth.createUserWithEmailAndPassword(email, password);
        if (password == null || password == "") {
            Log.d("Register", "Email not provided.");
            return;
        }
        String lat = mLatitudeView.getText().toString();
        double latDouble = 0;
        if (lat != null && lat != "") {
            latDouble = Double.parseDouble(lat);
        }
        String lon = mLongitudeView.getText().toString();
        double lonDouble = 0;
        if (lat != null && lat != "") {
            lonDouble = Double.parseDouble(lon);
        }
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference emailRef = dataRef.child(email.split("@")[0]);
        emailRef.child("apiKey").setValue(mApiKeyView.getText().toString());
        emailRef.child("lat").setValue(latDouble);
        emailRef.child("lon").setValue(lonDouble);
        emailRef.child("name").setValue(mNameView.getText().toString());
        emailRef.child("email").setValue(email);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "Failed to get permissions for permissions request " + permissions[i]);
            } else {
                Log.d("Permissions", "Permission granted for permissions request " + permissions[i]);
            }
        }
    }

}
