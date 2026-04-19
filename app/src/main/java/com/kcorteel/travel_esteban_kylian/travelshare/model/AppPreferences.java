package com.kcorteel.travel_esteban_kylian.travelshare.model;

public class AppPreferences {

    private final long prefId;
    private final long userId;
    private final AppTheme theme;
    private final String language;
    private final boolean notificationsEnabled;

    public AppPreferences(long prefId, long userId, AppTheme theme, String language, boolean notificationsEnabled) {
        this.prefId = prefId;
        this.userId = userId;
        this.theme = theme;
        this.language = language;
        this.notificationsEnabled = notificationsEnabled;
    }

    public long getPrefId() {
        return prefId;
    }

    public long getUserId() {
        return userId;
    }

    public AppTheme getTheme() {
        return theme;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
}
