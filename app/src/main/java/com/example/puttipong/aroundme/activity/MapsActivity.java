package com.example.puttipong.aroundme.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.puttipong.aroundme.R;
import com.example.puttipong.aroundme.dao.Example;
import com.example.puttipong.aroundme.manager.APIService;
import com.example.puttipong.aroundme.manager.ApiUtils;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, View.OnClickListener {

    private GoogleMap mGoogleMap;
    private SupportMapFragment mapFrag;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private EditText etDistance;
    private Button btnSearch;
    private FloatingActionButton fab;
    private int distance;
    private LatLng myLatLng;
    private LocationManager locationManager;
    private APIService mApiService;

    private static final String TAG = "MapsActivity";
    private boolean doubleBackToExitPressedOnce = false;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_maps);

        initInstance();
    }

    private void initInstance() {
        etDistance = (EditText) findViewById(R.id.etDistance);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        btnSearch.setOnClickListener(this);
        fab.setOnClickListener(this);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map_fragment);
        mapFrag.getMapAsync(this);
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
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap=googleMap;
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
        }
        else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }

        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                myLatLng = latLng;
                mCurrLocationMarker.setPosition(myLatLng);
                mCurrLocationMarker.setSnippet("dfgdfg");
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
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

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
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
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
                markerOptions.snippet(String.format("%.5f", location.getLatitude())
                        +" ,"+ String.format("%.5f", location.getLongitude()));
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
            if (!isEmpty(etDistance.getText().toString())) {
                distance = Integer.parseInt(etDistance.getText().toString());

                if (isCorrectDistance(distance)) {
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
            distance = Integer.parseInt(etDistance.getText().toString());
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("DISTANCE", distance);
            intent.putExtra("LATITUDE", myLatLng.latitude);
            intent.putExtra("LONGTITUDE", myLatLng.longitude);
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

    private void getPlaces() {
        fab.setVisibility(View.VISIBLE);

        mApiService = ApiUtils.getAPIService();

        String location = myLatLng.latitude + "," + myLatLng.longitude;
//        Log.i(TAG, "getPlaces: "+ location);
//        Log.i(TAG, "getPlaces: " + distance);

        Call<Example> call = mApiService.getNearbyPlaces(location ,distance);

        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {
                Log.i(TAG, "onResponse: " + response.code());

//                try {
                    mGoogleMap.clear();
                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        Double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                        Double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                        String placeName = response.body().getResults().get(i).getName();
                        String vicinity = response.body().getResults().get(i).getVicinity();
                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng latLng = new LatLng(lat, lng);
                        // Position of Marker on Map
                        markerOptions.position(latLng);
                        // Adding Title to the Marker
                        markerOptions.title(placeName);
                        markerOptions.snippet(vicinity);
                        // Adding Marker to the Camera.
                        Marker m = mGoogleMap.addMarker(markerOptions);
                        setInfoWindowAdapter(placeName, vicinity, latLng);
                        // Adding colour to the marker
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        // move map camera
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(11));
//                    }
//                } catch (Exception e) {
//                    Log.d("onResponse", "There is an error");
//                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                Log.i(TAG, "onFailure: " + t.toString());
            }
        });
    }

    private void setInfoWindowAdapter(final String placeName, final String vicinity, LatLng latLng){
       mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
           @Override
           public View getInfoWindow(Marker marker) {
               return null;
           }

           @Override
           public View getInfoContents(Marker marker) {
               View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);
               LatLng latLng = marker.getPosition();

               TextView tvPlaceName = (TextView) v.findViewById(R.id.tvPlaceName);
               TextView tvAddress = (TextView) v.findViewById(R.id.tvAddress);
               TextView tvLocation = (TextView) v.findViewById(R.id.tvLocation);
               ImageView imgPlace = (ImageView) v.findViewById(R.id.imgPlace);

               tvPlaceName.setText(placeName);
               tvAddress.setText(vicinity);
               tvLocation.setText("lat: " + String.format("%.5f", latLng.latitude
                       + "   ,lng: " + String.format("%.5f", latLng.longitude)));

//               Picasso.with(getActivity())
//                       .load(imgUrl)
//                       .into(imgVillage);

               return v;
           }
       });
    }

    private boolean isCorrectDistance(int distance) {
        if (distance > 50000) {
            return false;
        }
        return true;
    }

    private boolean isEmpty(String txt) {
        if (txt.matches("")) {
            return true;
        }
        return false;
    }
}