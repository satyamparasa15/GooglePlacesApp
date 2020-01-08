package com.example.googleplacesapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Arrays;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements LocationListener {

    TextView mCurrentLocation;
    Button mGetLocationBtn;

    protected LocationManager mLocationManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLocation;
    private String lat, lan;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.plant(new Timber.DebugTree());
        mCurrentLocation = findViewById(R.id.location_txt);
        mGetLocationBtn = findViewById(R.id.getLocationBtn);
        mProgress = findViewById(R.id.progressBar_cyclic);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mGetLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLocationPermission();
                // getCurrentLocation();

            }
        });
        setUp();
    }

    private void setUp() {
        mProgress.setVisibility(View.INVISIBLE);
        Places.initialize(getApplicationContext(), Constants.API_KEY);
        PlacesClient placesClient = Places.createClient(this);// Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Timber.e("Selected place %s, %s", place.getName(), place.getId());
            }

            @Override
            public void onError(Status status) {
                Timber.e("error %s", status);

            }
        });

    }

    public void requestLocationPermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        getCurrentLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            Toast.makeText(MainActivity.this, "Permissions needed, Please enable", Toast.LENGTH_LONG).show();
                            return;
                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                   PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Need to  enable location permissions", Toast.LENGTH_LONG).show();
            return;
        }
        mProgress.setVisibility(View.VISIBLE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Timber.e("Location found %s ,%s", location.getLongitude(),location.getLatitude());
                            if (location != null) {
                                mProgress.setVisibility(View.INVISIBLE);
                                mCurrentLocation.setVisibility(View.VISIBLE);
                                lat = String.valueOf(location.getLatitude());
                                lan = String.valueOf(location.getLongitude());
                                mCurrentLocation.setText(getString(R.string.current_location_coordinates, lat, lan));
                            }
                            mLocation = location;
                        }
                    }
                });
        if (mLocation == null) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            mLocationManager.removeUpdates(this);
        }

    }

    @Override
    public void onLocationChanged(Location location) {

        Timber.e("Current Location %s , %s", location.getLatitude(), location.getLongitude());
        if (location != null) {
            mProgress.setVisibility(View.INVISIBLE);
            mCurrentLocation.setVisibility(View.VISIBLE);
            lat = String.valueOf(location.getLatitude());
            lan = String.valueOf(location.getLongitude());
            mCurrentLocation.setText(getString(R.string.current_location_coordinates, lat, lan));
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Timber.e("onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String s) {
        Timber.e("onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String s) {
        Timber.e("onProviderDisabled");
    }
}
