package com.burntrac.sunrunai;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.TextView;

import org.codehaus.jackson.map.util.JSONPObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import im.delight.android.ddp.db.Document;

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

    public static JSONObject createActivity(String name,
                                            List[] comments,
                                            Date datetime,
                                            int schedule,
                                            Map detail) {
        List details = new LinkedList();

        details.add(detail);

        return createActivity(name, comments, datetime, schedule, details);
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

    public ArrayList<JSONObject> getActivities(boolean fetchActual) {
        return getActivities(null, null, fetchActual);
    }

    public synchronized ArrayList<JSONObject> getActivities(Date from, Date to, boolean fetchActual) {
        ArrayList<JSONObject> activities = new ArrayList<JSONObject>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor;

        String actualConstraint = fetchActual ? "schedule == 0" : "schedule == 1";

        if (from != null && to != null) {
            cursor = db.rawQuery("SELECT json FROM " + TABLE_NAME + " WHERE " + actualConstraint + " AND datetime >= " + from.getTime() + " AND datetime < " + to.getTime() + " ORDER BY datetime ASC;", null);
        } else {
            cursor = db.rawQuery("SELECT json FROM " + TABLE_NAME + " WHERE " + actualConstraint + " ORDER BY datetime ASC;", null);
        }

        if (cursor.getCount() == 0) {
            cursor.close();

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

        cursor.close();

        return activities;
    }

    public synchronized long addActivity(Map activity) {
        SQLiteDatabase db = null;
        SQLiteDatabase rdb = getReadableDatabase();

        String whereClause_ = "schedule == " + activity.get("schedule") + " AND datetime == " + activity.get("datetime");
        String whereClause = " WHERE " + whereClause_ + ";";
        Cursor cursor = rdb.rawQuery("SELECT json FROM " + TABLE_NAME + whereClause, null);

        if (cursor.getCount() > 0) {
            cursor.close();

            db = getWritableDatabase();
            db.delete(TABLE_NAME, whereClause_, new String[] {});
        } else {
            cursor.close();
        }

        ///

        if (activity.containsKey("details")) {
            ArrayList details = (ArrayList)activity.get("details");

            if (details != null && details.size() == 1 && (float)((Map)details.get(0)).get("distance") == 0) {
                return  0;
            }
        }

        ///

        JSONObject object = new JSONObject(activity);

        db = db != null ? db : getWritableDatabase();

        ContentValues content = new ContentValues();

        content.put("datetime", (long)activity.get("datetime"));
        content.put("schedule", (int)activity.get("schedule"));
        content.put("json", object.toString());

        long result = db.insert(TABLE_NAME, null, content);

        return result;
    }

    public static List jsonArrayToList(JSONArray array) {
        if (array == null) {
            return null;
        }

        ArrayList list = new ArrayList();
        for (int index = 0; index < array.length(); index++) {
            try {
                Object object = array.get(index);

                if (object instanceof JSONArray) {
                    list.add(jsonArrayToList((JSONArray)object));
                } else if (object instanceof JSONObject) {
                    list.add(jsonObjectToMap((JSONObject)object));
                } else {
                    list.add(object);
                }
            }
            catch (JSONException je) {
                // Ignore.
            }
        }

        return list;
    }

    public static Map jsonObjectToMap(JSONObject object) {
        if (object == null) {
            return null;
        }

        LinkedHashMap map = new LinkedHashMap();

        Iterator<String> keyIterator = object.keys();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();

            try {
                Object value = object.get(key);

                if (value instanceof JSONArray) {
                    map.put(key, jsonArrayToList((JSONArray)value));
                } else if (value instanceof JSONObject) {
                    map.put(key, jsonObjectToMap((JSONObject) value));
                //} else if (key == "date" || key == "datetime") {
                //    map.put(key, new Date((long)value));
                } else {
                    map.put(key, value);
                }

            } catch (JSONException e) {
                // Ignore.
            }
        }

        return map;
    }

    public synchronized int deleteScheduledActivities() {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(TABLE_NAME, "", new String[] {});
    }

    public synchronized void dropActivities() {
        SQLiteDatabase db = getWritableDatabase();

        db.rawQuery("DROP TABLE " + TABLE_NAME, null);
    }

    public synchronized boolean hasScheduledActivities() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT json FROM " + TABLE_NAME + " WHERE schedule == " + Generic.SCHEDULE_PLANNED + ";", null);

        boolean hasRows = cursor.getCount() > 0;

        cursor.close();

        return hasRows;
    }

    public static LinkedHashMap findGoal(Document plan) {
        ArrayList details = (ArrayList)plan.getField("details");

        for (Object object : details) {
            LinkedHashMap detail = (LinkedHashMap)object;
            ArrayList selectedTypes = (ArrayList)detail.get("selectedTypes");

            if (selectedTypes.contains("race")) {
                return detail;
            }
        }

        return null;
    }

    public static float getMetricGoalDistance(LinkedHashMap goal) {
        if (goal != null && goal.get("distance") != null && goal.get("distancetype") != null) {
            float distance = Float.parseFloat(goal.get("distance").toString());
            int distancetype = Integer.parseInt(goal.get("distancetype").toString());

            if (distancetype == 3) {
                return distance * 1.60934f;
            } else {
                return distance;
            }
        } else {
            return 0f;
        }
    }

    public static float getMetricDistance(JSONObject detail) {
        if (detail != null && detail.has("distance") && detail.has("distancetype")) {
            try {
                float distance = Float.parseFloat(detail.get("distance").toString());
                int distancetype = Integer.parseInt(detail.get("distancetype").toString());

                if (distancetype == 3) {
                    return distance * 1.60934f;
                } else {
                    return distance;
                }
            }
            catch (JSONException je) {
                return 0f;
            }
        } else {
            return 0f;
        }
    }

    public static float getDetailsDistanceSum(JSONArray details) {
        float metricSum = 0;

        for (int index = 0; index < details.length(); index++) {
            try {
                JSONObject detail = details.getJSONObject(index);

                metricSum += getMetricDistance(detail);
            }
            catch (JSONException je) {
                // Ignore. Assume 0.
            }
        }

        return metricSum;
    }
}
