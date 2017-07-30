package com.example.puttipong.aroundme.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.puttipong.aroundme.R;
import com.example.puttipong.aroundme.dao.Results;
import com.example.puttipong.aroundme.fragment.ListPlaceFragment;

import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private Bundle bundle;
    private static final String TAG = "ResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        bundle = getIntent().getExtras();
        List<Results> resultsList = bundle.getParcelableArrayList("RESULT_LIST");

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.contentContainer,
                            ListPlaceFragment.newInstance(bundle),
                            "ListPlaceFragment")
                    .commit();
        }
    }

}
