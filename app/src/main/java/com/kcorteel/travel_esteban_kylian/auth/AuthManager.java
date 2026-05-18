package com.kcorteel.travel_esteban_kylian.auth;

import android.content.Context;

import com.kcorteel.travel_esteban_kylian.R;
import com.kcorteel.travel_esteban_kylian.travelshare.database.TravelShareDatabase;
import com.kcorteel.travel_esteban_kylian.travelshare.database.UserDao;
import com.kcorteel.travel_esteban_kylian.travelshare.model.User;

public class AuthManager {

    private final UserDao userDao;
    private final AppSessionManager appSessionManager;
    private final Context appContext;

    public AuthManager(Context context) {
        appContext = context.getApplicationContext();
        userDao = TravelShareDatabase.getInstance(appContext).userDao();
        appSessionManager = new AppSessionManager(appContext);
    }

    public String login(String identifier, String password) {
        String normalizedIdentifier = identifier == null ? "" : identifier.trim();
        String normalizedPassword = password == null ? "" : password.trim();

        if (normalizedIdentifier.isEmpty() || normalizedPassword.isEmpty()) {
            return appContext.getString(R.string.auth_login_missing_fields_error);
        }

        User user = userDao.getByUsernameOrEmail(normalizedIdentifier);
        if (user == null || user.isAnonymous()) {
            return appContext.getString(R.string.auth_invalid_credentials_error);
        }

        if (!user.getPasswordHash().equals(PasswordUtils.hash(normalizedPassword))) {
            return appContext.getString(R.string.auth_invalid_credentials_error);
        }

        appSessionManager.setCurrentUserId(user.getUserId());
        return null;
    }

    public String register(String username, String email, String password, String confirmPassword) {
        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedEmail = email == null ? "" : email.trim();
        String normalizedPassword = password == null ? "" : password.trim();
        String normalizedConfirmPassword = confirmPassword == null ? "" : confirmPassword.trim();

        if (normalizedUsername.isEmpty() || normalizedEmail.isEmpty()
                || normalizedPassword.isEmpty() || normalizedConfirmPassword.isEmpty()) {
            return appContext.getString(R.string.auth_register_missing_fields_error);
        }

        if (!normalizedPassword.equals(normalizedConfirmPassword)) {
            return appContext.getString(R.string.auth_register_password_mismatch_error);
        }

        if (userDao.getByUsername(normalizedUsername) != null) {
            return appContext.getString(R.string.auth_username_exists_error);
        }

        if (userDao.getByEmail(normalizedEmail) != null) {
            return appContext.getString(R.string.auth_email_exists_error);
        }

        User user = new User(
                userDao.getMaxUserId() + 1L,
                normalizedUsername,
                normalizedEmail,
                PasswordUtils.hash(normalizedPassword),
                false
        );

        userDao.insert(user);
        appSessionManager.setCurrentUserId(user.getUserId());
        return null;
    }

    public User getCurrentUser() {
        return userDao.getById(appSessionManager.getCurrentUserId());
    }

    public void continueAsAnonymous() {
        appSessionManager.clearToAnonymous();
    }

    public void logout() {
        appSessionManager.clearToAnonymous();
    }
}
