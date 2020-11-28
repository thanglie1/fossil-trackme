package com.trackmeapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "RouteRecordDB.db";
    private static final String TABLE_NAME= "RouteRecord";

    public DatabaseHandler(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable= "CREATE TABLE " + TABLE_NAME + " ( ID TEXT PRIMARY KEY, Distance TEXT, Duration TEXT, AvgSpeed TEXT, startLocation TEXT, Route TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void add(RouteRecord record) {
        ContentValues values = new ContentValues();
        String startLocation = new Gson().toJson(record.getStartLocation());
        String route = new Gson().toJson(record.getRoute());
        values.put("ID", String.valueOf(record.getID()));
        values.put("Distance", String.valueOf(record.getDistance()));
        values.put("Duration", String.valueOf(record.getDuration()));
        values.put("AvgSpeed", String.valueOf(record.getAvgSpeed()));
        values.put("startLocation", startLocation);
        values.put("Route", route);

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public RouteRecord find(long ID) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE ID = '" + String.valueOf(ID) + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        RouteRecord record= new RouteRecord();
        if (cursor.moveToFirst()){
            cursor.moveToFirst();
            record.setID(ID);
            record.setDistance(Float.parseFloat(cursor.getString(1)));
            record.setDuration(Integer.parseInt(cursor.getString(2)));
            record.setAvgSpeed(Float.parseFloat(cursor.getString(3)));

            LatLng startLocation = new Gson().fromJson(cursor.getString(4), LatLng.class);
            record.setStartLocation(startLocation);
            ArrayList<List<LatLng>> route = new Gson().fromJson(cursor.getString(5), new TypeToken<ArrayList<List<LatLng>>>(){}.getType());
            record.setRoute(route);
        }
        db.close();
        return record;
    }

    public ArrayList<RouteRecord> loadData() {
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<RouteRecord> result = new ArrayList<>();
        while (cursor.moveToNext()){
            RouteRecord record= new RouteRecord();
            record.setID(Long.parseLong(cursor.getString(0)));
            record.setDistance(Float.parseFloat(cursor.getString(1)));
            record.setDuration(Integer.parseInt(cursor.getString(2)));
            record.setAvgSpeed(Float.parseFloat(cursor.getString(3)));

            LatLng startLocation = new Gson().fromJson(cursor.getString(4), LatLng.class);
            record.setStartLocation(startLocation);
            ArrayList<List<LatLng>> route = new Gson().fromJson(cursor.getString(5), new TypeToken<ArrayList<List<LatLng>>>(){}.getType());
            record.setRoute(route);
            result.add(record);
        }
        db.close();
        return result;
    }
}
