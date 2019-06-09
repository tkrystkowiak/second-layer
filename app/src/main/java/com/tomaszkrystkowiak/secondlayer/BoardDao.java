package com.tomaszkrystkowiak.secondlayer;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface BoardDao {

    @Query("SELECT * FROM board WHERE creator LIKE :user ")
    List<Board> getAllUserBoards(String user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Board board);

}
