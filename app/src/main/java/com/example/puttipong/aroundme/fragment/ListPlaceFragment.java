package com.example.puttipong.aroundme.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.puttipong.aroundme.R;
import com.example.puttipong.aroundme.adapter.PlaceAdapter;
import com.example.puttipong.aroundme.dao.Example;
import com.example.puttipong.aroundme.dao.Results;
import com.example.puttipong.aroundme.manager.APIService;
import com.example.puttipong.aroundme.manager.ApiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListPlaceFragment extends Fragment {

    private int distance;
    private Double lat, lng;
    private RecyclerView recyclerView;
    private APIService mAPIService;
    private PlaceAdapter adapter;
    private List<Results> resultsList;

    private SwipeRefreshLayout swipeRefreshLayout;

    private static final String TAG = "ListPlaceFragment";

    public ListPlaceFragment() {
        super();
    }

    public static ListPlaceFragment newInstance(int distance, double lat, double lng) {
        ListPlaceFragment fragment = new ListPlaceFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("DISTANCE", distance);
        bundle.putDouble("LATITUDE", lat);
        bundle.putDouble("LONGTITUDE", lng);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            distance = getArguments().getInt("DISTANCE", 0);
            lat = getArguments().getDouble("LATITUDE", 0.0);
            lng = getArguments().getDouble("LONGTITUDE", 0.0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        initInstances(rootView);
        getData();
        return rootView;
    }

    private void initInstances(View rootView) {
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
    }

        private void getData() {
            String latLng = lat + "," + lng;

            Log.i(TAG, "getData: " + getArguments().getInt("DISTANCE", 0));
            Log.i(TAG, "getData: " + latLng);

            APIService apiService = ApiUtils.getAPIService();
            Call<Example> call = apiService.getNearbyPlaces(latLng, distance);

            call.enqueue(new Callback<Example>() {
                @Override
                public void onResponse(Call<Example> call, Response<Example> response) {
                    Log.i(TAG, "onResponse: " + response.code());

                    if (response.body().getResults().size() != 0) {
                        Log.i(TAG, "onResponse: " + response.body().getResults().size());
                        Log.i(TAG, "onResponse: " + response.body().getResults().get(0).getName());

                        resultsList = new ArrayList<Results>();
                        resultsList = response.body().getResults();

                        Collections.sort(resultsList, new Comparator<Results>() {
                            @Override
                            public int compare(Results o1, Results o2) {
                                String s1 = o1.getName();
                                String s2 = o2.getName();
                                return s1.compareToIgnoreCase(s2);
                            }
                        });

                        adapter = new PlaceAdapter(resultsList, getActivity());
                        recyclerView.setAdapter(adapter);

                    } else Toast.makeText(getActivity(), "No place around you!" ,Toast.LENGTH_SHORT).show();
                }


                @Override
                public void onFailure(Call<Example> call, Throwable t) {
                    Log.e(TAG, "onFailure: " + t.toString() );
                }
            });
        }

    private void refreshContent() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 5000);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /*
     * Save Instance State Here
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save Instance State here
    }

    /*
     * Restore Instance State Here
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            // Restore Instance State here
        }
    }


}
