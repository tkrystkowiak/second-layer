package com.tomaszkrystkowiak.secondlayer;

import android.location.Location;

import java.util.ArrayList;
import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

@Entity
public class Board {

    @PrimaryKey(autoGenerate = true)
    public int bid;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "creator")
    public String creator;

    @ColumnInfo(name = "location")
    public LatLng location;

    @ColumnInfo(name = "date")
    public Date date;

    @ColumnInfo(name = "messages")
    public ArrayList<Message> messages;



}
