package com.example.bingo;

import android.content.Context;

import androidx.room.*;

import com.example.bingo.data.dao.StatisticsDao;
import com.example.bingo.data.dao.UserDao;
import com.example.bingo.data.model.Statistics;
import com.example.bingo.data.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities={User.class, Statistics.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract StatisticsDao statisticsDao();
    private static volatile AppDatabase INSTANCE;
    private static final int NUM_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUM_THREADS);
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context,
                                    AppDatabase.class, "bingo_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
