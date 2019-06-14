package com.tomaszkrystkowiak.secondlayer;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

import androidx.room.TypeConverter;

public class Converters {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String fromLatLng(LatLng location) {
        Gson gson = new Gson();
        String json = gson.toJson(location);
        return json;
    }

    @TypeConverter
    public static LatLng fromStringToLatLng(String location) {
        return new Gson().fromJson(location, LatLng.class);
    }

    @TypeConverter
    public static String fromMessagesArrayList(ArrayList<Message> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }

    @TypeConverter
    public static ArrayList<Message> fromStringToLatLngArrayList(String value) {
        Type listType = new TypeToken<ArrayList<Message>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }




}
