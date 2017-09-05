package com.burntrac.sunrunai;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
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

public class WeatherWrapper {

    public static void getJSONObject(Context context) {
        WeatherIntentService service = new WeatherIntentService();
        //mReceiver.setReceiver(this);
        Intent intent = new Intent(context, WeatherIntentService.class);

        /* Send optional extras to Download IntentService
        intent.putExtra("url", url);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("requestId", 101);
        */

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
