package com.example.bingo.data.model;

import androidx.annotation.NonNull;
import androidx.room.*;

import java.util.UUID;

@Entity
public class User {
    @PrimaryKey @NonNull
    public UUID uuid;

    public String username;
    public String password;
}
