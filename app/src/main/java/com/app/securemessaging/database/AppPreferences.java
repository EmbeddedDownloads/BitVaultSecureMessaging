package com.app.securemessaging.database;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.app.securemessaging.bean.ContactsModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 02-06-2017.
 */

public class AppPreferences {
    private static final String APP_SHARED_PREFS = "com.securemessaging"; //  Name of the file -.xml
    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor prefsEditor;
    private static volatile AppPreferences appPreferences;
    private final String CONTACT="contact";


    public static AppPreferences getInstance(Context context) {
        if (appPreferences == null) {
            synchronized (AppPreferences.class) {
                if (appPreferences == null)
                    appPreferences = new AppPreferences(context);
            }
        }
        return appPreferences;
    }

    private AppPreferences(Context context) {
        this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this.prefsEditor = appSharedPrefs.edit();
    }


    /**
     * this method to get contact list from prefrences
     * @return
     */
    public ArrayList<ContactsModel> getContact() {
        String value = this.appSharedPrefs.getString(CONTACT, null);
        if (value != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<ContactsModel>>() {
            }.getType();
            ArrayList<ContactsModel> obj = gson.fromJson(value, type);
            return obj;
        }
        return null;

    }
    /**
     * this method to set contact list into prefrences
     * @return
     */
    public void setContact(ArrayList<ContactsModel> contact) {
        Gson gson = new Gson();
        String json = gson.toJson(contact);
        prefsEditor.putString(CONTACT, json);
        prefsEditor.commit();
    }

}
