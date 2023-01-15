package com.example.bingo.data;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.bingo.AppDatabase;
import com.example.bingo.data.dao.UserDao;
import com.example.bingo.data.model.User;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class UserRepository {
    private UserDao userDao;
    private List<String> allUsers;
    UserRepository(Application application){
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
    }

    public void getAll(){
        AppDatabase.databaseWriteExecutor.execute(() -> {
            allUsers = userDao.getAll();
        });
    }


    public void insert(User user){
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.insert(user);
        });
    }

    public User findByUsername(String username){
        Future<User> user = AppDatabase.databaseWriteExecutor.submit(new Callable<User>(){
            public User call(){
                return userDao.findByUsername(username);
            }
        });
        try{
            return user.get();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public UserDao getUserDao(){
        return userDao;
    }
}
