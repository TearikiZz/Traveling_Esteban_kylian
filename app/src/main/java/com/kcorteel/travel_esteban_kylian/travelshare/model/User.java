package com.kcorteel.travel_esteban_kylian.travelshare.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey
    private long userId;
    private String username;
    private String email;
    private String passwordHash;
    private boolean isAnonymous;

    public User(long userId, String username, String email, String passwordHash, boolean isAnonymous) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.isAnonymous = isAnonymous;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }
}
