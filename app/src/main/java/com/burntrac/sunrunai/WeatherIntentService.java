package com.burntrac.sunrunai;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by kim on 9/4/17.
 */

public class WeatherIntentService extends IntentService {
    public static final int SUCCESS = 0;
    public static final int PENDING_LOCATION = 1;
    public static final int ERROR_IO = 2;

    public static final String[] QUERIES = new String[] { "observations/current", "forecast/daily/10day" };

    public static Location location;

    private static final String apiKey = PrivateConfig.weatherApiKey;

    public WeatherIntentService() {
        super(WeatherIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        Bundle bundle = new Bundle();

        if (location == null) {
            receiver.send(PENDING_LOCATION, bundle);

            return;
        }

        bundle.putBoolean("usemetric", WeatherWrapper.useMetric);

        // https://api.weather.com/v1/geocode/34.063/-84.217/observations/current.json?language=en-US&units=e&apiKey=yourApiKey
        for (String operation : QUERIES){
            try {
                DecimalFormat formatter = new DecimalFormat();
                formatter.setMaximumFractionDigits(4);
                formatter.setMinimumFractionDigits(4);
                String latitude = formatter.format(location.getLatitude());
                String longitude = formatter.format(location.getLongitude());

                String units = WeatherWrapper.useMetric ? "m" : "e";
                String urlString = "https://api.weather.com/v1/geocode/" + latitude + "/" + longitude + "/" + operation + ".json?language=en-US&units=" + units + "&apiKey=" + apiKey;
                HttpURLConnection urlConnection = null;
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(5000);
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                String jsonString = sb.toString();

                bundle.putString(operation, jsonString);
            } catch (IOException ioe) {
                receiver.send(ERROR_IO, bundle);

                return;
            }
        }

        receiver.send(SUCCESS, bundle);
    }
}
