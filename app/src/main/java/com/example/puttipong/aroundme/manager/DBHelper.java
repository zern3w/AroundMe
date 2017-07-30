package com.example.puttipong.aroundme.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.puttipong.aroundme.dao.LocationSQLite;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private Context mContext;
    private SQLiteDatabase sqLiteDatabase;

    private static final String databaseName = "aroundme.sqlite";
    private static final int databaseVersion = 1;
    // LOCATION TABLE
    public static final String TABLE_NAME = "location";
    public static final String ROW_ID = "_id";
    public static final String PLACE_ID = "place_id";
    public static final String PLACE_NAME = "place_name";
    public static final String LATITUDE = "lat";
    public static final String LONGTITUDE = "lng";

    private static final String TAG = "DBHelper";

    public DBHelper(Context context) {
        super(context, databaseName, null, databaseVersion);
        this.mContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String.format("CREATE TABLE %s (" +
                        "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%s TEXT UNIQUE, " +
                        "%s TEXT, " +
                        "%s DOUBLE, " +
                        "%s DOUBLE);",
                TABLE_NAME,
                ROW_ID,
                PLACE_ID,
                PLACE_NAME,
                LATITUDE,
                LONGTITUDE
        );

        Log.e(TAG, "onCreate: " + sql);

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        Log.i(TAG, "Upgrade Database from " + oldVersion + " to " + newVersion);

        db.execSQL(sql);
        onCreate(db);
    }

    public void createPlace(String placeId, String placeName ,double lat, double lng) {
        sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PLACE_ID, placeId);
        values.put(PLACE_NAME, placeName);
        values.put(LATITUDE, lat);
        values.put(LONGTITUDE, lng);

        sqLiteDatabase.insertWithOnConflict(TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);
        sqLiteDatabase.close();
    }

    public List<LocationSQLite> getNearestPlaces(int distance, double lat, double lng) {
        sqLiteDatabase = this.getWritableDatabase();
        double latMin, latMax, lngMin, lngMax;
        double distanceInGeo;

//        d = radius * arccos(sin(x1) * sin(x2) + cos(x1) * cos(x2) * cos(y1 - y2))
//        1Degree = 111.32KM >> 1M = 0.000008983
        final double DEGREE = 0.000008983;

        distanceInGeo = distance * DEGREE;
        latMin = lat - distanceInGeo;
        lngMin = lng - distanceInGeo;
        latMax = lat + distanceInGeo;
        lngMax = lng + distanceInGeo;
        List<LocationSQLite> locationSQLites = new ArrayList<LocationSQLite>();
        String sql = String.format("SELECT * FROM %s " +
                        "WHERE (%s >= %s AND %s <= %s) " +
                        "AND (%s >= %s AND %s <= %s);",
                TABLE_NAME,
                LATITUDE, latMin, LATITUDE, latMax,
                LONGTITUDE, lngMin, LONGTITUDE, lngMax);

        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                LocationSQLite location = new LocationSQLite(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4)
                );
                // Adding contact to list
                locationSQLites.add(location);
            } while (cursor.moveToNext());
        }

        return locationSQLites;
    }


}
