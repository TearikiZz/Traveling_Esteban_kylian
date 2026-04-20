package com.kcorteel.travel_esteban_kylian.auth;

import android.content.Context;

import com.kcorteel.travel_esteban_kylian.travelshare.database.TravelShareDatabase;
import com.kcorteel.travel_esteban_kylian.travelshare.database.UserDao;
import com.kcorteel.travel_esteban_kylian.travelshare.model.User;

public class AuthManager {

    private final UserDao userDao;
    private final AppSessionManager appSessionManager;

    public AuthManager(Context context) {
        Context appContext = context.getApplicationContext();
        userDao = TravelShareDatabase.getInstance(appContext).userDao();
        appSessionManager = new AppSessionManager(appContext);
    }

    public String login(String identifier, String password) {
        String normalizedIdentifier = identifier == null ? "" : identifier.trim();
        String normalizedPassword = password == null ? "" : password.trim();

        if (normalizedIdentifier.isEmpty() || normalizedPassword.isEmpty()) {
            return "Veuillez renseigner votre identifiant et votre mot de passe.";
        }

        User user = userDao.getByUsernameOrEmail(normalizedIdentifier);
        if (user == null || user.isAnonymous()) {
            return "Identifiants invalides.";
        }

        if (!user.getPasswordHash().equals(PasswordUtils.hash(normalizedPassword))) {
            return "Identifiants invalides.";
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
            return "Tous les champs sont obligatoires.";
        }

        if (!normalizedPassword.equals(normalizedConfirmPassword)) {
            return "Les mots de passe ne correspondent pas.";
        }

        if (userDao.getByUsername(normalizedUsername) != null) {
            return "Ce nom d'utilisateur existe déjà.";
        }

        if (userDao.getByEmail(normalizedEmail) != null) {
            return "Cet email est déjà utilisé.";
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
