package com.example.bingo.data;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.bingo.AppDatabase;
import com.example.bingo.BingoApp;
import com.example.bingo.data.dao.UserDao;
import com.example.bingo.data.model.LoggedInUser;
import com.example.bingo.data.model.Statistics;
import com.example.bingo.data.model.User;

import java.io.IOException;
import java.util.UUID;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    private UserRepository userRepository = new UserRepository((Application) BingoApp.getContext());
    private static StatisticsRepository statRepository = new StatisticsRepository((Application) BingoApp.getContext());

    public static StatisticsRepository getStatRepository(){ return statRepository; }

    public Result<LoggedInUser> login(String username, String password) {
        try {
            User user = userRepository.findByUsername(username);
            if(user == null) return new Result.Error(new Exception("No user registered with given username was found!"));
            else if (user.username.equals(username) && user.password.equals(password)) {
                LoggedInUser loggedUser = new LoggedInUser(user.uuid.toString(), user.username);
                return new Result.Success<>(loggedUser);
            }
            else {
                return new Result.Error(new Exception("Password is incorrect!"));
            }
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }


    public Result<LoggedInUser> signup(String username, String password){
        try {
            User user = userRepository.findByUsername(username);
            if(user != null && user.username.equals(username)) return new Result.Error(new Exception("This username already exists!"));
            else{
                UUID userId = java.util.UUID.randomUUID();
                User registeredUser = new User();
                registeredUser.username = username;
                registeredUser.password = password;
                registeredUser.uuid = userId;
                userRepository.insert(registeredUser);

                Statistics initStats = new Statistics();
                initStats.ambos = 0;
                initStats.cinquinos = 0;
                initStats.matchesPlayed = 0;
                initStats.username = username;
                initStats.matchesWon = 0;
                initStats.ternos = 0;
                initStats.quaternos = 0;
                statRepository.insert(initStats);

                LoggedInUser loggedUser = new LoggedInUser(userId.toString(), username);
                return new Result.Success<>(loggedUser);
            }
        } catch (Exception e){
            return new Result.Error(new IOException("Error in db", e));
        }
    }

    public void logout() {
    }
}