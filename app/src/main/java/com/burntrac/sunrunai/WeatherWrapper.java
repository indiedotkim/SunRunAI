package com.burntrac.sunrunai;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by kim on 8/29/17.
 */

public class WeatherWrapper extends ResultReceiver {

    public static JSONObject today = null;
    public static List<JSONObject> forecasts = Collections.synchronizedList(new ArrayList<JSONObject>());

    private WeatherWrapper(Handler handler) {
        super(handler);
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

                        JSONArray apiArray = (JSONArray)apiResult.get("forecasts");
                        for (int i = 0; i < apiArray.length(); i++) {
                            forecasts.add((JSONObject) apiArray.get(i));
                        }
                    } else if (apiResult.has("observation")) {
                        today = (JSONObject) apiResult.get("observation");
                    } else {
                        // Not supported; not implemented.
                    }
                } catch (JSONException je) {

                }
            }
        } else {
            Log.d("WW", "Error: " + resultCode);
        }

        Log.d("WW", "Done.");
    }

    private static JSONObject getForecast() {
        if (forecasts.size() == 0) {
            return null;
        }

        JSONObject forecast = forecasts.get(0);

        return forecast;
    }

    public static void update(Context context) {
        WeatherWrapper receiver = new WeatherWrapper(new Handler());

        Intent intent = new Intent(context, WeatherIntentService.class);
        intent.putExtra("receiver", receiver);

        context.startService(intent);
    }

    public static String getRain() {
        JSONObject forecast = getForecast();

        if (forecast == null) {
            return "";
        }

        return "12mm";
    }

    public static String getTemperature() {
        JSONObject forecast = getForecast();

        if (forecast == null) {
            return "";
        }

        return "12°C";
    }

    public static String getTemperatureMin() {
        JSONObject forecast = getForecast();

        if (forecast == null) {
            return "";
        }

        return "12°C";
    }

    public static String getTemperatureMax() {
        JSONObject forecast = getForecast();

        if (forecast == null) {
            return "";
        }

        return "12°C";
    }

    public static String getWindDirection() {
        JSONObject forecast = getForecast();

        if (forecast == null) {
            return "";
        }

        return "NW";
    }

    public static String getWindSpeed() {
        JSONObject forecast = getForecast();

        if (forecast == null) {
            return "";
        }

        return "12kph";
    }
}
