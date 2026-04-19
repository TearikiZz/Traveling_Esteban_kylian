package com.kcorteel.travel_esteban_kylian.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSessionManager {

    private static final String PREFS_NAME = "traveling_session";
    private static final String KEY_CURRENT_USER_ID = "current_user_id";
    private static final long ANONYMOUS_USER_ID = 4L;

    private final SharedPreferences sharedPreferences;

    public AppSessionManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public long getCurrentUserId() {
        return sharedPreferences.getLong(KEY_CURRENT_USER_ID, ANONYMOUS_USER_ID);
    }

    public void setCurrentUserId(long userId) {
        sharedPreferences.edit().putLong(KEY_CURRENT_USER_ID, userId).apply();
    }

    public void clearToAnonymous() {
        setCurrentUserId(ANONYMOUS_USER_ID);
    }
}
