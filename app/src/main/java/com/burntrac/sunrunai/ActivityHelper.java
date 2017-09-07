package com.burntrac.sunrunai;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by kim on 9/2/17.
 */

public class ActivityHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SunRunAIActivities.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "activities";
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    "datetime INTEGER, " +
                    "json TEXT" +
                    ");";

    private static int debug = 0;

    public static HashMap createActivity() {
        HashMap activity = new HashMap();

        activity.put("name", "Name " + debug);
        activity.put("comments", new ArrayList());
        activity.put("datetime", new Date().getTime() + debug * 24 * 60 * 60 * 1000);
        activity.put("deleted", false);
        activity.put("kind", 4);
        activity.put("schedule", 0);

        ArrayList details = new ArrayList();

        HashMap detail = new HashMap();
        detail.put("comments", new ArrayList());

        detail.put("hours", 0);
        detail.put("minutes", 30);
        detail.put("seconds", 10);

        detail.put("kind", 4);
        detail.put("distance", 10);
        detail.put("distancetype", 2);

        detail.put("gear", null);
        detail.put("injury", null);

        activity.put("details", details);

        return activity;
    }

    public ActivityHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ArrayList<JSONObject> getActivities() {
        ArrayList<JSONObject> activities = new ArrayList<JSONObject>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT json FROM " + TABLE_NAME + ";", null);

        if (cursor.getCount() == 0) {
            return activities;
        }

        do {
            String jsonString = cursor.getString(0);

            try {
                JSONObject json = new JSONObject(jsonString);

                activities.add(json);
            }
            catch(JSONException je) {
                // TODO Report corrupt database.
            }
        } while(cursor.moveToNext());

        return activities;
    }
}
