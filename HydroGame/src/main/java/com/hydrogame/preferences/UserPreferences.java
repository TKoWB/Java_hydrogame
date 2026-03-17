package com.hydrogame.preferences;

import java.util.prefs.Preferences;

public class UserPreferences {

    private static final Preferences prefs = Preferences.userNodeForPackage(UserPreferences.class);
    private static final String KEY_EMAIL = "saved_email";
    private static final String KEY_PASSWORD = "saved_password";
    private static final String KEY_REMEMBER = "remember_me";
    private static final String KEY_ROLE = "saved_role";

    public static void saveCredentials(String email, String password, String role) {
        prefs.put(KEY_EMAIL, email);
        prefs.put(KEY_PASSWORD, password);
        prefs.put(KEY_ROLE, role);
        prefs.putBoolean(KEY_REMEMBER, true);
    }

    public static void clearCredentials() {
        prefs.remove(KEY_EMAIL);
        prefs.remove(KEY_PASSWORD);
        prefs.remove(KEY_ROLE);
        prefs.putBoolean(KEY_REMEMBER, false);
    }

    public static String getSavedEmail() {
        return prefs.get(KEY_EMAIL, "");
    }

    public static String getSavedPassword() {
        return prefs.get(KEY_PASSWORD, "");
    }

    public static String getSavedRole() {
        return prefs.get(KEY_ROLE, "USER");
    }

    public static boolean isRememberMe() {
        return prefs.getBoolean(KEY_REMEMBER, false);
    }
}
