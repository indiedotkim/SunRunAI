package com.burntrac.sunrunai;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    "schedule INTEGER, " +
                    "json TEXT" +
                    ");";

    private static int debug = 0;

    public static ArrayList createActivityDetails(List[] comments,
                                                  int hours,
                                                  int minutes,
                                                  int seconds,
                                                  int kind,
                                                  float distance,
                                                  int distancetype) {
        ArrayList details = new ArrayList();

        HashMap detail = new HashMap();

        detail.put("comments", comments == null ? new ArrayList() : comments);

        detail.put("hours", hours);
        detail.put("minutes", minutes);
        detail.put("seconds", seconds);

        detail.put("kind", kind);
        detail.put("distance", distance);
        detail.put("distancetype", distancetype);

        detail.put("gear", null);
        detail.put("injury", null);

        details.add(detail);

        return details;
    }

    public static JSONObject createActivity(String name,
                                     List[] comments,
                                     Date datetime,
                                     int schedule,
                                     List details) {
        HashMap activity = new HashMap();

        activity.put("name", name + Math.abs(new java.util.Random().nextInt()));
        activity.put("comments", comments == null ? new ArrayList() : comments);
        activity.put("datetime", datetime.getTime());
        activity.put("deleted", false);
        activity.put("schedule", schedule);

        activity.put("details", details);

        return new JSONObject(activity);
    }

    public static JSONObject createActivity() {
        List details = createActivityDetails(null, 1, 0, 0, 4, 10, 2);
        JSONObject activity = createActivity("Test", null, new Date(), 0, details);

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
        return getActivities(null, null);
    }

    public synchronized ArrayList<JSONObject> getActivities(Date from, Date to) {
        ArrayList<JSONObject> activities = new ArrayList<JSONObject>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor;

        if (from != null && to != null) {
            cursor = db.rawQuery("SELECT json FROM " + TABLE_NAME + " WHERE datetime >= " + from.getTime() + " AND datetime < " + to.getTime() + " ORDER BY datetime ASC;", null);
        } else {
            cursor = db.rawQuery("SELECT json FROM " + TABLE_NAME + " ORDER BY datetime ASC;", null);
        }

        if (cursor.getCount() == 0) {
            return activities;
        }

        while (cursor.moveToNext()) {
            String jsonString = cursor.getString(0);

            try {
                JSONObject json = new JSONObject(jsonString);

                activities.add(json);
            }
            catch(JSONException je) {
                // TODO Report corrupt database.
            }
        };

        return activities;
    }

    public synchronized long addActivity(Map activity) {
        JSONObject object = new JSONObject(activity);

        SQLiteDatabase db = getWritableDatabase();

        ContentValues content = new ContentValues();

        content.put("datetime", (long)activity.get("datetime"));
        content.put("schedule", (int)activity.get("schedule"));
        content.put("json", object.toString());

        long result = db.insert(TABLE_NAME, null, content);

        return result;
    }

    public synchronized int deleteScheduledActivities() {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(TABLE_NAME, "schedule = 1", new String[] {});
    }

    public synchronized void dropActivities() {
        SQLiteDatabase db = getWritableDatabase();

        db.rawQuery("DROP TABLE " + TABLE_NAME, null);
    }
}
