package com.example.puttipong.aroundme.manager;

public class ApiUtils {

    private ApiUtils() {
    }

    private static final String BASE_URL = "https://maps.googleapis.com/maps/";

    public static APIService getAPIService() {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}