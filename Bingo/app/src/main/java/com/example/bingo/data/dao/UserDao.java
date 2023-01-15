package com.example.bingo.data.dao;

import androidx.room.*;

import com.example.bingo.data.model.User;

import java.util.*;

@Dao
public interface UserDao {
    @Query("SELECT username FROM user")
    List<String> getAll();

    @Query("SELECT * FROM user WHERE username LIKE :username")
    User findByUsername(String username);

    @Insert()
    void insert(User user);

    @Delete
    void delete(User user);

}
