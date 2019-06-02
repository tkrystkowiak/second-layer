package com.tomaszkrystkowiak.secondlayer;

import android.location.Location;

import java.util.ArrayList;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Board {

    @PrimaryKey(autoGenerate = true)
    public int bid;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "creator")
    public  String creator;

    @ColumnInfo(name = "location")
    public Location location;

    @ColumnInfo(name = "messages")
    public ArrayList<Message> messages;

}
