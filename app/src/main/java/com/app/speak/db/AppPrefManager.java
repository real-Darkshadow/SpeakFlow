package com.app.speak.db;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.speak.models.User;

import org.jetbrains.annotations.NonNls;


public class AppPrefManager {

    @NonNls
    private static final String PREF_NAME = "deathnote-bookingapp-userapp";
    @NonNls
    private static final String PREF_NAME_1 = "deathnote-bookingapp-userapp1";
    @NonNls
    private static final String PREF_IS_USER_LOGGED_ID = "bookingapp-is_use_logged_in";
    @NonNls


    private static final String PREF_IS_ONBOARDED = "user_onboarded";
    private static final String PREF_REVIEWED = "reviewed";
    private static final String PREF_SUBSCRIPTION_IS_ACTIVE = "subscription_is_active";


    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final SharedPreferences pref1;
    private final SharedPreferences.Editor editor1;
    private final Context _context;
    // shared pref mode
    private final int PRIVATE_MODE = 0;

    public AppPrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        pref1 = _context.getSharedPreferences(PREF_NAME_1, PRIVATE_MODE);
        editor1 = pref1.edit();
    }

    //set if user is reviewed the app
    public void setUserReviewed() {
        editor.putBoolean(PREF_REVIEWED, true);
        editor.commit();
    }


    public void setSubscriptionIsActive(Boolean isActive) {
        editor.putBoolean(PREF_SUBSCRIPTION_IS_ACTIVE, isActive);
        editor.commit();
    }


    public boolean getSubscriptionIsActive() {
        return pref.getBoolean(PREF_SUBSCRIPTION_IS_ACTIVE, false);
    }

    public boolean isUserReviewed() {
        return pref.getBoolean(PREF_REVIEWED, false);
    }

    public void setUserData(String uid, String email) {
        editor.putString("uid", uid);
        editor.putString("email", email);
        editor.putBoolean(PREF_IS_USER_LOGGED_ID, true);
        editor.commit();
    }

    public User getUser() {
        String uid = pref.getString("uid", null);
        String email = pref.getString("email", null);
        return new User(uid, email);
    }


    public Boolean isUserOnboarded() {
        return pref1.getBoolean(PREF_IS_ONBOARDED, false);
    }


    public void setUserOnboarded() {
        editor1.putBoolean(PREF_IS_ONBOARDED, true);
        editor1.commit();
    }

    public boolean isUserLoggedIn() {
        return pref.getBoolean(PREF_IS_USER_LOGGED_ID, false);
    }

    public void logoutUser() {
        editor.clear().commit();
    }

}