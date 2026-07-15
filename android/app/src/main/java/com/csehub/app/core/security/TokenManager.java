package com.csehub.app.core.security;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.csehub.app.core.utils.Constants;

/**
 * Secure token and session management using EncryptedSharedPreferences.
 * Handles JWT storage, user data, login state, and auto-login.
 */
public class TokenManager {

    private static TokenManager instance;
    private SharedPreferences prefs;

    private TokenManager(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            prefs = EncryptedSharedPreferences.create(
                    Constants.PREF_NAME,
                    masterKeyAlias,
                    context.getApplicationContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            // Fallback to regular SharedPreferences if encryption fails
            prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    // ============================================
    // Token Management
    // ============================================

    public void saveToken(String token) {
        prefs.edit().putString(Constants.KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(Constants.KEY_TOKEN, null);
    }

    public void saveRefreshToken(String refreshToken) {
        prefs.edit().putString(Constants.KEY_REFRESH_TOKEN, refreshToken).apply();
    }

    public String getRefreshToken() {
        return prefs.getString(Constants.KEY_REFRESH_TOKEN, null);
    }

    public boolean hasToken() {
        return getToken() != null && !getToken().isEmpty();
    }

    // ============================================
    // User Session
    // ============================================

    public void saveUserSession(String userId, String email, String name, String role, String token, String refreshToken) {
        prefs.edit()
                .putString(Constants.KEY_USER_ID, userId)
                .putString(Constants.KEY_USER_EMAIL, email)
                .putString(Constants.KEY_USER_NAME, name)
                .putString(Constants.KEY_USER_ROLE, role)
                .putString(Constants.KEY_TOKEN, token)
                .putString(Constants.KEY_REFRESH_TOKEN, refreshToken)
                .putBoolean(Constants.KEY_IS_LOGGED_IN, true)
                .apply();
    }

    public String getUserId() {
        return prefs.getString(Constants.KEY_USER_ID, "");
    }

    public String getUserEmail() {
        return prefs.getString(Constants.KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return prefs.getString(Constants.KEY_USER_NAME, "");
    }

    public String getUserRole() {
        return prefs.getString(Constants.KEY_USER_ROLE, "");
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false) && hasToken();
    }

    public boolean isAdmin() {
        return Constants.ROLE_ADMIN.equals(getUserRole());
    }

    public boolean isFaculty() {
        return Constants.ROLE_FACULTY.equals(getUserRole());
    }

    public boolean isStudent() {
        return Constants.ROLE_STUDENT.equals(getUserRole());
    }

    public boolean canCreate() {
        return isAdmin() || isFaculty();
    }

    // ============================================
    // FCM Token
    // ============================================

    public void saveFCMToken(String fcmToken) {
        prefs.edit().putString(Constants.KEY_FCM_TOKEN, fcmToken).apply();
    }

    public String getFCMToken() {
        return prefs.getString(Constants.KEY_FCM_TOKEN, "");
    }

    // ============================================
    // Settings
    // ============================================

    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean(Constants.KEY_DARK_MODE, enabled).apply();
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(Constants.KEY_DARK_MODE, false);
    }

    public void saveProfilePhoto(String url) {
        prefs.edit().putString(Constants.KEY_PROFILE_PHOTO, url).apply();
    }

    public String getProfilePhoto() {
        return prefs.getString(Constants.KEY_PROFILE_PHOTO, "");
    }

    // ============================================
    // Logout
    // ============================================

    public void clearSession() {
        String fcmToken = getFCMToken(); // Preserve FCM token
        prefs.edit().clear().apply();
        if (fcmToken != null && !fcmToken.isEmpty()) {
            saveFCMToken(fcmToken);
        }
    }
}
