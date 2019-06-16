package com.tomaszkrystkowiak.secondlayer;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface BoardDao {

    List<Board> getByTitle(String title);

    @Query("SELECT * FROM board WHERE creator LIKE :user ")
    List<Board> getAllUserBoards(String user);

    @Query("SELECT * FROM board")
    List<Board> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Board board);

    @Delete
    void delete(Board board);

}
