package com.example.puttipong.aroundme.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.puttipong.aroundme.R;
import com.example.puttipong.aroundme.fragment.ListPlaceFragment;

public class ResultActivity extends AppCompatActivity {

    private int distance;
    private double lat, lng;
    private static final int defaultValue = 0;

    private static final String TAG = "ResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        if (intent != null) {
            distance = intent.getIntExtra("DISTANCE", defaultValue);
            lat = intent.getDoubleExtra("LATITUDE", defaultValue);
            lng = intent.getDoubleExtra("LONGTITUDE", defaultValue);
        }

        Log.i("MAIN", "onCreate: " + distance + lat + lng);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.contentContainer,
                            ListPlaceFragment.newInstance(distance, lat, lng),
                            "ListPlaceFragment")
                    .commit();
        }
    }

}
