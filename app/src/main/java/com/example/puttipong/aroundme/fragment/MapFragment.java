package com.example.puttipong.aroundme.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.puttipong.aroundme.adapter.PlaceAdapter;
import com.example.puttipong.aroundme.dao.AddressDao;
import com.example.puttipong.aroundme.R;
import com.example.puttipong.aroundme.activity.ResultActivity;
import com.example.puttipong.aroundme.dao.Example;
import com.example.puttipong.aroundme.dao.Results;
import com.example.puttipong.aroundme.manager.APIService;
import com.example.puttipong.aroundme.manager.ApiUtils;
import com.example.puttipong.aroundme.manager.PicassoMarker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        View.OnClickListener {

    private EditText etDistance;
    private Button btnSearch;
    private View rootView;
    private Location mLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private GoogleMap mMap;
    private LatLng latLng;
    private Marker marker;
    private Double lat, lng;
    private SupportMapFragment mapFragment;
    private AddressDao addressDao;
    private FusedLocationProviderClient mFusedLocationClient;
    private FloatingActionButton fab;
    private int distance;
    private static final int REQUEST_LOCATION_CODE = 99;
    private Location lastlocation;
    private Marker currentLocationmMarker;

    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 1000; /* 2 sec */

    private static final String TAG = "MapFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.google_map_fragment);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_LONG).show();
                }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity()).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_maps,
                    container, false);
        }
        initInstance(rootView);
        return rootView;
    }

    private void initInstance(View rootView) {
        btnSearch = (Button) rootView.findViewById(R.id.btnSearch);
        etDistance = (EditText) rootView.findViewById(R.id.etDistance);
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        btnSearch.setOnClickListener(this);
        fab.setOnClickListener(this);
    }

    private void getResponse() {
        String latLng = "18.7717874,98.9742796";
        APIService apiService = ApiUtils.getAPIService();
        Call<Example> call = apiService.getNearbyPlaces(latLng, distance);

        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Call<Example> call, Response<Example> response) {

                try {
                    mMap.clear();
                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        Double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                        Double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                        final String placeName = response.body().getResults().get(i).getName();
                        final String vicinity = response.body().getResults().get(i).getVicinity();
                        final String icon = response.body().getResults().get(i).getIcon();

                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng latLng = new LatLng(lat, lng);
                        // Position of Marker on Map
                        markerOptions.position(latLng);
                        // Adding Title to the Marker
                        markerOptions.title(placeName);
                        markerOptions.snippet(vicinity);

                        // Adding Marker to the Camera.
                        Marker m = mMap.addMarker(markerOptions);
                        // Adding colour to the marker

                        PicassoMarker markerPicasso = new PicassoMarker(m);
                        Picasso.with(getActivity())
                                .load(icon)
                                .into(markerPicasso);

//                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        // move map camera

                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                    }
                } catch (Exception e) {
                    Log.d("onResponse", "There is an error: " + e.toString());
                    e.printStackTrace();
                }
            }


            @Override
            public void onFailure(Call<Example> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.toString());
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map_fragment);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    private void setCurrentLocation(Location location) {
        Log.i(TAG, "setCurrentLocation: ");
        if (location != null) {
            Log.i(TAG, "setCurrentLocation: notnull");
            String msg = "Updated Location: " +
                    Double.toString(location.getLatitude()) + "," +
                    Double.toString(location.getLongitude());
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            // You can now create a LatLng Object for use with maps
            lat = location.getLatitude();
            lng = location.getLongitude();
            latLng = new LatLng(lat, lng);
        }
        mapFragment.getMapAsync(this);
    }

    private void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        lastlocation = location;
        if (currentLocationmMarker != null) {
            currentLocationmMarker.remove();

        }
        Log.d("lat = ", "" + lat);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentLocationmMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'." +
                        "\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_CODE);
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_CODE);
            }
            return false;
        } else
            return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");

    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service destroyed!");
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        lat = latLng.latitude;
//        lng = latLng.longitude;
        latLng = new LatLng(18, 20);

//        addressDao = getAddress(getActivity(), lat, lng);

        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        CameraUpdate center = CameraUpdateFactory.newLatLngZoom(latLng, 8);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(14);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

        if (marker == null) {
            marker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("I'm here...")
//                    .snippet(addressDao.getCity() + ", " + addressDao.getState())
            );
            marker.showInfoWindow();
        }
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (marker == null) {

                    // Marker was not set yet. Add marker:
                    marker = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(15, 15))
                                    .title("I'm here...")
//                            .snippet(addressDao.getCity() + ", " + addressDao.getState())
                    );

                } else {
                    lat = latLng.latitude;
                    lng = latLng.longitude;
                    addressDao = getAddress(getActivity(), lat, lng);

                    // Marker already exists, just update it's position
                    marker.setPosition(latLng);
//                    marker.setSnippet(addressDao.getCity() + ", " + addressDao.getState());
                    marker.showInfoWindow();
                    Toast.makeText(getActivity(), "Lat: " + lat + "\nLng: " + lng, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "setOnMapLongClickListener: " + lat);
                    Log.e(TAG, "setOnMapLongClickListener: " + lng);
                }
            }
        });

    }

    public static AddressDao getAddress(Context context, double LATITUDE, double LONGITUDE) {
        AddressDao addressDao = new AddressDao();

        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null && addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();

                addressDao.setAddress(address);
                addressDao.setCity(city);
                addressDao.setState(state);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addressDao;
    }

    @Override
    public void onClick(View v) {
        if (v == btnSearch) {
            if (!isEmpty(etDistance.getText().toString())) {
                distance = Integer.parseInt(etDistance.getText().toString());

                if (isCorrectDistance(distance)) {
                    getResponse();
                } else {
                    Toast.makeText(getActivity(), "The maximum distance is 50000", Toast.LENGTH_SHORT).show();
                }
            } else Toast.makeText(getActivity(), "Please enter the distance", Toast.LENGTH_SHORT).show();
        }

        if (v == fab) {
            Toast.makeText(getActivity(), "FAB", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), ResultActivity.class);
            startActivity(intent);
        }
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