package com.example.puttipong.aroundme.tool;

public class Validator {

    public static boolean isCorrectDistance(int distance) {
        if (distance > 50000) {
            return false;
        }
        return true;
    }

    public static boolean isEmpty(String txt) {
        if (txt.matches("")) {
            return true;
        }
        return false;
    }

    public static double distFrom(Double lat1, Double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = (earthRadius * c) * 0.001;

        return dist;
    }
}
