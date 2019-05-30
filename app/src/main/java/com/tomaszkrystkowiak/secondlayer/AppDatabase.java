package com.tomaszkrystkowiak.secondlayer;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Board.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract BoardDao boardDao();

}
