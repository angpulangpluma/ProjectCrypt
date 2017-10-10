package com.dlsu.getbetter.getbetter.sessionmanagers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.dlsu.getbetter.getbetter.activities.LoginActivity;

import java.util.HashMap;

/**
 * Created by mikedayupay on 17/02/2016.
 * GetBetter 2016
 */
public class SystemSessionManager {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;

    private static final String PREFER_NAME = "SystemPref";

    public static final String LOGIN_USER_NAME = "loginUserName";
    private static final String IS_USER_LOGGED_IN = "isUserLoggedIn";
    public static final String HEALTH_CENTER_NAME = "healthCenterName";
    public static final String HEALTH_CENTER_ID = "healthCenterId";
    public SystemSessionManager (Context context) {

        this._context = context;
        int PRIVATE_MODE = 0;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);

        editor = pref.edit();
    }

    public void createUserSession (String userName) {

        editor.putString(LOGIN_USER_NAME, userName);
        editor.putBoolean(IS_USER_LOGGED_IN, true);
        editor.commit();
    }

    public HashMap<String, String> getUserDetails (){

        HashMap<String, String> userDetails = new HashMap<>();

        userDetails.put(LOGIN_USER_NAME, pref.getString(LOGIN_USER_NAME, null));

        return userDetails;
    }

    public void setHealthCenter (String healthCenterName, String healthCenterId) {

        editor.putString(HEALTH_CENTER_NAME, healthCenterName);
        editor.putString(HEALTH_CENTER_ID, healthCenterId);
        editor.commit();
    }

    public HashMap<String, String> getHealthCenter () {

        HashMap<String, String> healthCenter = new HashMap<>();
        healthCenter.put(HEALTH_CENTER_NAME, pref.getString(HEALTH_CENTER_NAME, null));
        healthCenter.put(HEALTH_CENTER_ID, pref.getString(HEALTH_CENTER_ID, null));

        return healthCenter;
    }

    public boolean checkLogin() {

        if(!this.isUserLoggedIn()) {

            Intent i = new Intent(_context, LoginActivity.class);

            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            _context.startActivity(i);

            return true;
        }

        return false;
    }

    public void logoutUser() {

        editor.clear();
        editor.commit();

        Intent i = new Intent(_context, LoginActivity.class);

        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        _context.startActivity(i);
    }

    private boolean isUserLoggedIn() {
        return pref.getBoolean(IS_USER_LOGGED_IN, false);
    }


}
