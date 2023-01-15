package com.example.bingo.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.bingo.data.model.Statistics;

@Dao
public interface StatisticsDao {
    @Query("SELECT * FROM statistics WHERE username LIKE :user")
    Statistics findByUsername(String user);

    @Update
    void update(Statistics stat);

    @Insert
    void insert(Statistics stat);
}
