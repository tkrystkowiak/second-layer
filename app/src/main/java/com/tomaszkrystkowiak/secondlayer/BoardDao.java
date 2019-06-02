package com.tomaszkrystkowiak.secondlayer;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

@Dao
public interface BoardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Board board);
}
