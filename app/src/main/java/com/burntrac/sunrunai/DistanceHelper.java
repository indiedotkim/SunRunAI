package com.burntrac.sunrunai;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by kim on 9/14/17.
 */

public class DistanceHelper {
    public static String formatDistance(Context context, float distance, int distancetype) {
        return formatDistance(context, distance, distancetype, false);
    }

    public static String formatDistance(Context context, float distance, int distancetype, boolean noDecimals) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useMetric = sharedPrefs.getBoolean(SettingsActivity.PREF_USE_METRIC, SettingsActivity.DEFAULT_USE_METRIC);

        if (useMetric && distancetype == 3) {
            distance *= 1.60934;
            distancetype = 2;
        } else if (!useMetric && distancetype == 2) {
            distance /= 1.60934;
            distancetype = 3;
        }

        String type = "";

        if (distancetype == 1) {
            type = "m";
        } else if (distancetype == 2) {
            type = "km";
        } else if (distancetype == 3) {
            type = "mi";
        } else if (distancetype == 4) {
            type = "ft";
        }

        if (noDecimals) {
            return String.format("%.0f", distance) + type;
        } else {
            return String.format("%.1f", distance) + type;
        }
    }

    public static float plainDistance(Context context, float distance, int distancetype) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useMetric = sharedPrefs.getBoolean(SettingsActivity.PREF_USE_METRIC, SettingsActivity.DEFAULT_USE_METRIC);

        if (useMetric && distancetype == 3) {
            return distance * 1.60934f;
        } else if (!useMetric && distancetype == 2) {
            return distance / 1.60934f;
        } else {
            return distance;
        }
    }

    public static float getMetric(float distance, boolean isMetric) {
        if (!isMetric) {
            return distance * 1.60934f;
        }

        return distance;
    }

    public static float getImperial(float distance, boolean isMetric) {
        if (isMetric) {
            return distance / 1.60934f;
        }

        return distance;
    }
}
