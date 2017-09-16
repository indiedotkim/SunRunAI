package com.burntrac.sunrunai;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by kim on 8/29/17.
 */

public class WeatherWrapper extends ResultReceiver {
    private OnCompletionListener mOnCompletionListener;
    private final static SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");

    public static JSONObject today = null;
    public static List<JSONObject> forecasts = Collections.synchronizedList(new ArrayList<JSONObject>());
    public static boolean useMetric = SettingsActivity.DEFAULT_USE_METRIC;

    private static Random random = new Random(2);

    private WeatherWrapper(Handler handler, OnCompletionListener completionListener) {
        super(handler);

        mOnCompletionListener = completionListener;
    }

    private WeatherWrapper(Handler handler) {
        this(handler, null);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        //super.onReceiveResult(resultCode, resultData);
        if (resultCode == WeatherIntentService.SUCCESS) {
            for (String operation : WeatherIntentService.QUERIES) {
                try {
                    String resultString = resultData.getString(operation);

                    if (resultString == null) {
                        continue;
                    }

                    JSONObject apiResult = new JSONObject(resultString);

                    if (apiResult.has("forecasts")) {
                        forecasts.clear();

                        Date midnight = DateHelper.getMidnight(new Date());
                        JSONArray apiArray = (JSONArray)apiResult.get("forecasts");
                        for (int i = 0; i < apiArray.length(); i++) {
                            JSONObject forecast = (JSONObject)apiArray.get(i);

                            // Do not include "today":
                            if (forecast.has("fcst_valid_local") && !midnight.equals(dateParser.parse((String)forecast.get("fcst_valid_local")))) {
                                forecasts.add(forecast);
                            }
                        }
                    } else if (apiResult.has("observation")) {
                        today = (JSONObject)apiResult.get("observation");
                    } else {
                        // Not supported; not implemented.
                    }
                }
                catch (JSONException je) {

                }
                catch (ParseException pe) {

                }
            }
        } else {
            Log.d("WW", "Error: " + resultCode);
        }

        useMetric = resultData.getBoolean("usemetric");

        if (mOnCompletionListener != null && resultCode == WeatherIntentService.SUCCESS) {
            mOnCompletionListener.onCompletion();
        }
    }

    private static JSONObject getForecast() {
        if (forecasts.size() == 0) {
            return null;
        }

        JSONObject forecast = forecasts.get(0);

        return forecast;
    }

    public static void update(Context context, OnCompletionListener completionListener) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        useMetric = sharedPrefs.getBoolean(SettingsActivity.PREF_USE_METRIC, SettingsActivity.DEFAULT_USE_METRIC);

        WeatherWrapper receiver = new WeatherWrapper(new Handler(), completionListener);

        Intent intent = new Intent(context, WeatherIntentService.class);
        intent.putExtra("receiver", receiver);

        context.startService(intent);
    }

    public static int getDaysWithDataAvailable() {
        return (today == null ? 0 : 1) + forecasts.size();
    }

    public static boolean hasDataForDay(Date date) {
        return getObjectForDate(date, false) != null;
    }

    private static JSONObject getObjectForDate(Date date) {
        return getObjectForDate(date, false);
    }
    private static JSONObject getObjectForDate(Date date, boolean forceForecast) {
        date = DateHelper.getMidnight(date);

        if (!forceForecast) {
            Date dateToday = DateHelper.getMidnight(new Date());

            if (dateToday.equals(date)) {
                return today;
            }
        }

        for (JSONObject forecast : forecasts) {
            try {
                if (forecast.has("fcst_valid_local") && date.equals(dateParser.parse(forecast.getString("fcst_valid_local")))) {
                    return forecast;
                }
            } catch (ParseException e) {
                // Ignore. Should have been caught earlier!
            } catch (JSONException e) {
                // Ignore. Should have been caught earlier!
            }
        }

        return null;
    }

    private static String getPrecipitationUnit() {
        if (useMetric) {
            return "mm";
        } else {
            return "\"";
        }
    }

    private static String getTemperatureUnit() {
        if (useMetric) {
            return "°C";
        } else {
            return "°F";
        }
    }

    public static float getRainValue(Date date) {
        JSONObject object = getObjectForDate(date);

        if (object == null) {
            return 0f;
        }

        try {
            if (object.has("qpf")) {
                return Float.valueOf(object.getString("qpf"));
            }
        } catch (JSONException e) {
            // Ignore. Too late to catch here.
        }

        return 0f;
    }

    public static String getRain(Date date) {
        JSONObject object = getObjectForDate(date);

        if (object == null) {
            return "";
        }

        try {
            if (object.has("qpf")) {
                return object.getString("qpf") + getPrecipitationUnit();
            }
        } catch (JSONException e) {
            // Ignore. Too late to catch here.
        }

        return "-";
    }

    public static float getTemperatureValue(Date date) {
        JSONObject object = getObjectForDate(date);

        if (object == null) {
            return 0f;
        }

        try {
            JSONObject details;

            if (object.has("metric")) {
                details = object.getJSONObject("metric");
            } else if (object.has("imperial")) {
                details = object.getJSONObject("imperial");
            } else {
                return 0f;
            }

            if (details.has("temp")) {
                return Math.round(details.getLong("temp") * 10f) / 10f;
            }

        } catch (JSONException e) {
            // Ignore. Too late to catch here.
        }

        return 0f;
    }

    public static String getTemperature(Date date) {
        JSONObject object = getObjectForDate(date);

        if (object == null) {
            return "";
        }

        try {
            JSONObject details;

            if (object.has("metric")) {
                details = object.getJSONObject("metric");
            } else if (object.has("imperial")) {
                details = object.getJSONObject("imperial");
            } else {
                return "-";
            }

            if (details.has("temp")) {
                return getTemperatureValue(date) + getTemperatureUnit();
            }
        } catch (JSONException e) {
            // Ignore. Too late to catch here.
        }

        return "-";
    }

    public static String getTemperatureMin(Date date) {
        JSONObject object = getObjectForDate(date, true);

        if (object == null) {
            return "";
        }

        try {
            if (object.has("min_temp")) {
                return object.getString("min_temp") + getTemperatureUnit();
            }
        } catch (JSONException e) {
            // Ignore. Too late to catch here.
        }

        return "-";
    }

    public static float getTemperatureMaxValue(Date date) {
        random = new Random(date.getTime());

        JSONObject object = getObjectForDate(date, true);

        if (object == null) {
            return 0f;
        }

        try {
            if (12 == 72) {
                throw new JSONException("B");
            }
            if (object.has("max_temp")) {
                return Math.round((22f - 10f * (random.nextFloat() - 0.5f)) * 10f) / 10f;
                //return object.getLong("max_temp");
            }
        } catch (JSONException e) {
            // Ignore. Too late to catch here.
        }

        return 0f;
    }

    public static String getTemperatureMax(Date date) {
        JSONObject object = getObjectForDate(date, true);

        if (object == null) {
            return "";
        }

        if (object.has("max_temp")) {
            return getTemperatureMaxValue(date) + getTemperatureUnit();
        }

        return "-";
    }

    public static String getWindDirection(Date date) {
        JSONObject forecast = getForecast();

        if (forecast == null) {
            return "";
        }

        return "NW";
    }

    public static String getWindSpeed(Date date) {
        JSONObject forecast = getForecast();

        if (forecast == null) {
            return "";
        }

        return "12kph";
    }
}
