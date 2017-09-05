package com.burntrac.sunrunai;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

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

    public static List<JSONObject> forecasts = Collections.synchronizedList(new ArrayList<JSONObject>());

    private WeatherWrapper(Handler handler) {
        super(handler);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        //super.onReceiveResult(resultCode, resultData);
        if (resultCode == WeatherIntentService.SUCCESS) {
            try {
                JSONObject apiResult = new JSONObject(resultData.getString("json"));

                forecasts.add(apiResult);
            }
            catch(JSONException je) {

            }
        } else {
            Log.d("WW", "Error: " + resultCode);
        }

    }

    public static void getJSONObject(Context context) {
        WeatherWrapper receiver = new WeatherWrapper(new Handler());

        Intent intent = new Intent(context, WeatherIntentService.class);
        intent.putExtra("receiver", receiver);

        context.startService(intent);
    }

    public static String getRain() {
        return "12mm";
    }

    public static String getTemperature() {
        return "12°C";
    }

    public static String getWindDirection() {
        return "NW";
    }

    public static String getWindSpeed() {
        return "12kph";
    }
}
