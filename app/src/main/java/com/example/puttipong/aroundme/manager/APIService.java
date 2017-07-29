package com.example.puttipong.aroundme.manager;

import com.example.puttipong.aroundme.dao.Example;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIService {

//    location=18.7717874,98.9742796&radius=500
    @GET("api/place/nearbysearch/json?&key=AIzaSyCZ1BCe4Q7YL1nCa_ovtet4Bjn52tT20T8")
    Call<Example> getNearbyPlaces(
            @Query("location") String location,
            @Query("radius") int radius);

}