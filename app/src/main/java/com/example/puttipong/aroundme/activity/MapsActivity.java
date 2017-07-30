package com.example.puttipong.aroundme.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.puttipong.aroundme.R;
import com.example.puttipong.aroundme.dao.Example;
import com.example.puttipong.aroundme.dao.LocationSQLite;
import com.example.puttipong.aroundme.dao.Results;
import com.example.puttipong.aroundme.manager.APIService;
import com.example.puttipong.aroundme.manager.ApiUtils;
import com.example.puttipong.aroundme.manager.DBHelper;
import com.example.puttipong.aroundme.manager.PicassoMarker;
import com.example.puttipong.aroundme.tool.Validator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, View.OnClickListener {

    private Button btnSearch;
    private EditText etDistance;
    private FloatingActionButton fab;

    private GoogleMap mGoogleMap;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private SupportMapFragment mapFrag;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private int distance;
    private DBHelper dbHelper;
    private LatLng myLatLng;
    private APIService mApiService;
    private List<Results> resultsList;
    private List<LocationSQLite> locationSQLites;

    private static final String TAG = "MapsActivity";
    private boolean doubleBackToExitPressedOnce = false;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_maps);

        initInstance();
    }

    private void initInstance() {
        resultsList = new ArrayList<Results>();
        dbHelper = new DBHelper(this);
        etDistance = (EditText) findViewById(R.id.etDistance);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        btnSearch.setOnClickListener(this);
        fab.setOnClickListener(this);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map_fragment);
        mapFrag.getMapAsync(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }

        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                myLatLng = latLng;
                mCurrLocationMarker.setPosition(myLatLng);
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(myLatLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
        mCurrLocationMarker.showInfoWindow();

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 11));

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnSearch) {
            if (!Validator.isEmpty(etDistance.getText().toString())) {
                distance = Integer.parseInt(etDistance.getText().toString());

                if (Validator.isCorrectDistance(distance)) {
                    Toast.makeText(this,
                            distance + "Lat: " + myLatLng.latitude + ", Lng: " + myLatLng.longitude,
                            Toast.LENGTH_SHORT)
                            .show();
                    getPlaces();

                } else {
                    Toast.makeText(this, "The maximum distance is 50000", Toast.LENGTH_SHORT).show();
                }
            } else Toast.makeText(this, "Please enter the distance", Toast.LENGTH_SHORT).show();
        }

        if (v == fab) {
            Log.e(TAG, "PlaceAdapter: " + resultsList.get(0).getPhotos().get(0).getPhotoReference());
            Intent intent = new Intent(this, ResultActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("RESULT_LIST", (ArrayList<? extends Parcelable>) resultsList);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        //Checking for fragment count on backstack
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else if (!doubleBackToExitPressedOnce) {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.double_back_exit, Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            super.onBackPressed();
            return;
        }
    }

    private void getLocalData() {
        locationSQLites =
                dbHelper.getNearestPlaces(distance,
                        myLatLng.latitude,
                        myLatLng.longitude);

        for (int i = 0; i < locationSQLites.size(); i++) {
            Double lat = Double.valueOf(locationSQLites.get(i).getLatitude());
            Double lng = Double.valueOf(locationSQLites.get(i).getLongtitude());
            String placeId = locationSQLites.get(i).getPlaceId();
            final String placeName = locationSQLites.get(i).getPlaceName();
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng latLng = new LatLng(lat, lng);
            double mDistance = Validator.distFrom(lat, lng, myLatLng.latitude, myLatLng.longitude);
            // Position of Marker on Map
            markerOptions.position(latLng);
            // Adding Title to the Marker
            markerOptions.title(placeName);
            markerOptions.snippet("Distance: " + String.format("%,.2f", mDistance) + " KM.");
            // Adding Marker to the Camera.
            Marker m = mGoogleMap.addMarker(markerOptions);

            // move map camera
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }
    }

    private void getPlaces() {
        Log.i(TAG, "getPlaces: ");

        String location = myLatLng.latitude + "," + myLatLng.longitude;

        mApiService = ApiUtils.getAPIService();
        Call<Example> call = mApiService.getNearbyPlaces(location, distance);

        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {
                Log.i(TAG, "onResponse: " + response.code());

                try {
                    mGoogleMap.clear();
                    addCurrentMarker();
                    drawCircleArea();
                    getLocalData();
                    fab.setVisibility(View.VISIBLE);

                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        Double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                        Double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                        String placeId = response.body().getResults().get(i).getPlaceId();
                        final String placeName = response.body().getResults().get(i).getName();
                        final String vicinity = response.body().getResults().get(i).getVicinity();
                        String iconUrl = response.body().getResults().get(i).getIcon();
                        MarkerOptions markerOptions = new MarkerOptions();
                        double mDistance = Validator.distFrom(lat, lng, myLatLng.latitude, myLatLng.longitude);
                        LatLng latLng = new LatLng(lat, lng);
                        // Position of Marker on Map
                        markerOptions.position(latLng);
                        // Adding Title to the Marker
                        markerOptions.title(placeName);
                        markerOptions.snippet("Distance: " + String.format("%,.2f", mDistance) + " KM.");
                        // Adding Marker to the Camera.

//                        for (int j = 0; j < locationSQLites.size(); j++) {
//                            if (placeId != locationSQLites.get(j).getPlaceId()) {
                                Marker m = mGoogleMap.addMarker(markerOptions);

                                PicassoMarker markerPicasso = new PicassoMarker(m);
                                Picasso.with(getApplicationContext())
                                        .load(iconUrl)
                                        .into(markerPicasso);
                                // move map camera
                                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                                dbHelper.createPlace(placeId, placeName, lat, lng);
//                            } else {
//                                Log.i(TAG, "onResponse: placeID is duplicated");
//                            }
//                        }

                        resultsList = response.body().getResults();
                    }
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error: " + e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                Log.i(TAG, "onFailure: " + t.toString());
            }
        });
    }

    private void drawCircleArea() {
        Circle circle = mGoogleMap.addCircle(new CircleOptions()
                .center(myLatLng)
                .radius(distance)
                .strokeColor(Color.BLUE)
                .fillColor(0x200000cc));
    }

    private void addCurrentMarker() {
        MarkerOptions CurrMarkerOption = new MarkerOptions();
        CurrMarkerOption.position(myLatLng)
                .title("You're here!")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mCurrLocationMarker = mGoogleMap.addMarker(CurrMarkerOption);
        mCurrLocationMarker.showInfoWindow();
    }

}