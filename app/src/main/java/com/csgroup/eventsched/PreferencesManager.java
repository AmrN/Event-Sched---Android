package com.csgroup.eventsched;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by pc on 8/18/2015.
 */
public class PreferencesManager {
    private Context mAppContext;
    private SharedPreferences mPref;
    private String mPrefName;

    private final String PREF_API_KEY = "api_key";
    private final String PREF_REMEMBER_LOGIN = "remember_login";
    private final String PREF_USER_ID = "user_id";
    private final String PREF_EVENTS_IDS = "events_ids";
    private final String DEFAULT_PREF_NAME = "pref0";

    public PreferencesManager(Context appContext, String prefName) {
        this.mAppContext = appContext;
        this.mPrefName = (prefName == null) ? DEFAULT_PREF_NAME : prefName;
        this.mPref = appContext.getSharedPreferences(this.mPrefName, 0);
    }

    private void setString(String key, String value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void setBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void setStringSet(String key, Set<String> strSet) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putStringSet(key, strSet);
        editor.apply();
    }

    public void clearAll() {
        this.mPref.edit().clear().commit();
    }

    public void setApiKey(String apiKey) {
        this.setString(PREF_API_KEY, apiKey);
    }

    public String getApiKey() {
        return mPref.getString(PREF_API_KEY, null);
    }

    public void setRememberLogin(boolean remember) {
        this.setBoolean(PREF_REMEMBER_LOGIN, remember);
    }

    public boolean getRememberLogin() {
        return mPref.getBoolean(PREF_REMEMBER_LOGIN, false);
    }

    public void setUserId(String userId) {
        this.setString(PREF_USER_ID, userId);
    }

    public String getUserId() {
        return mPref.getString(PREF_USER_ID, null);
    }

    public void setEventsIds(List<Event> eventList) {
        Set<String> idStrSet = new HashSet<>();
        for (Event ev: eventList) {
            idStrSet.add(Integer.toString(ev.getId()));
        }
        this.setStringSet(PREF_EVENTS_IDS, idStrSet);
    }

    public Set<Integer> getEventsIds() {
        Set<String> idsStrSet = mPref.getStringSet(PREF_EVENTS_IDS, null);
        if (idsStrSet == null) {
            return null;
        }
        Set<Integer> idsSet = new HashSet<>();
        for (String idStr : idsStrSet) {
            idsSet.add(Integer.parseInt(idStr));
        }
        return idsSet;
    }

    public void logout() {

        if (RetrieveNewEventsService.isRunning) {
            // stop service
            Intent serviceIntent = new Intent(mAppContext, RetrieveNewEventsService.class);
            mAppContext.stopService(serviceIntent);
        }
        // clear preferences
        this.clearAll();
    }

    public void clearApiKey() {
        mPref.edit().remove(PREF_API_KEY).commit();
    }



}
