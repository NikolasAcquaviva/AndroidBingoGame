package com.example.bingo.data;

import android.app.Application;

import com.example.bingo.AppDatabase;
import com.example.bingo.data.dao.StatisticsDao;
import com.example.bingo.data.model.Statistics;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class StatisticsRepository {
    private StatisticsDao statDao;

    StatisticsRepository(Application application){
        AppDatabase db = AppDatabase.getDatabase(application);
        statDao = db.statisticsDao();
    }

    public void insert(Statistics stat){
        AppDatabase.databaseWriteExecutor.execute(() -> {
                statDao.insert(stat);
        });
    }

    public void update(Statistics stat){
        AppDatabase.databaseWriteExecutor.execute(() -> {
            statDao.update(stat);
        });
    }

    public Statistics findByUsername(String user){
        Future<Statistics> statistics = AppDatabase.databaseWriteExecutor.submit(new Callable<Statistics>() {
            @Override
            public Statistics call(){
                return statDao.findByUsername(user);
            }
        });

        try{
            return statistics.get();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
