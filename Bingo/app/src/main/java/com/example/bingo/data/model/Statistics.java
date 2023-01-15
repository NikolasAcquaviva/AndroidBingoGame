package com.example.bingo.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Statistics {
    @PrimaryKey @NonNull
    public String username;

    public int matchesPlayed;
    public int matchesWon;
    public int ambos;
    public int ternos;
    public int quaternos;
    public int cinquinos;
}
