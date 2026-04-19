package com.kcorteel.travel_esteban_kylian.travelshare.model;

public class User {

    private final long userId;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final boolean isAnonymous;

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
