package com.burntrac.sunrunai;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by kim on 9/14/17.
 */

public class AI {
    private static float temp;
    private static float tempMid;
    private static float tempVariation;
    private static float rain;
    private static float rainMid;
    private static float rainVariation;

    private static LinkedList<Boolean> coolDays = new LinkedList<Boolean>();
    private static LinkedList<Boolean> hotDays = new LinkedList<Boolean>();
    private static LinkedList<Boolean> dryDays = new LinkedList<Boolean>();
    private static LinkedList<Boolean> wetDays = new LinkedList<Boolean>();

    public static boolean tempDiffSufficient;
    public static boolean rainDiffSufficient;

    public static HashMap<Integer, Integer> swap = new HashMap<Integer, Integer>();
    public static HashMap<Integer, Integer> swapInv = new HashMap<Integer, Integer>();
    public static HashMap<Integer, String> reason = new HashMap<Integer, String>();

    public static boolean isActivated = false;
    public static boolean valuesValid = false;

    private static int reschedule(int baseIndex, LinkedList<String> types, LinkedList<Integer> indices, LinkedList<Boolean> boolDays, String[] categories) {
        return reschedule(baseIndex, types, indices, boolDays, categories);
    }

    private static int reschedule(int baseIndex, LinkedList<String> types, LinkedList<Integer> indices, LinkedList<Boolean> boolDays, String[] categories, HashMap<Integer, Integer> swap, HashMap<Integer, Integer> swapInv) {
        int switchWith = -1;

        if (swap != null && swapInv != null && (swap.containsKey(baseIndex) || swapInv.containsKey(baseIndex))) {
            return switchWith;
        }

        for (String type : categories) {
            if (switchWith == -1) {
                for (int i = 0; i < boolDays.size(); i++) {
                    if (boolDays.get(i)) {
                        if (types.get(i).equals(type)) {
                            if (swap == null || swapInv == null) {
                                return indices.get(i);
                            } else {
                                int index = indices.get(i);

                                if (!swap.containsKey(index) && !swapInv.containsKey(index)) {
                                    return indices.get(i);
                                }
                            }
                        }
                    }
                }
            }
        }

        return switchWith;
    }

    private static void determineCategories() {
        if (temp < tempMid - tempVariation * 0.1f) {
            coolDays.add(true);
            hotDays.add(false);
        } else if (temp > tempMid + tempVariation * 0.1f) {
            coolDays.add(false);
            hotDays.add(true);
        } else {
            coolDays.add(false);
            hotDays.add(false);
        }

        if (rain < rainMid - rainVariation * 0.1f) {
            dryDays.add(true);
            wetDays.add(false);
        } else if (rain > rainMid + rainVariation * 0.1f) {
            dryDays.add(false);
            wetDays.add(true);
        } else {
            dryDays.add(false);
            wetDays.add(false);
        }
    }

    public static void getOptimizedPlan(Context context) {
        valuesValid = false;

        swap.put(1, 2);
        swapInv.put(2, 1);
        reason.put(1, "debug");
        AI.valuesValid = true;

        if (WeatherWrapper.getDaysWithDataAvailable() <= 2) {
            return;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useMetric = sharedPrefs.getBoolean(SettingsActivity.PREF_USE_METRIC, SettingsActivity.DEFAULT_USE_METRIC);

        Date today = DateHelper.getMidnight(new Date());

        float tempLo = Float.NaN;
        float tempHi = Float.NaN;
        float rainLo = Float.NaN;
        float rainHi = Float.NaN;
        for (int ahead = 1; ahead < 10; ahead++) {
            Date day = DateHelper.getNDaysAhead(today, ahead);

            temp = WeatherWrapper.getTemperatureMaxValue(day);
            rain = WeatherWrapper.getRainValue(day);

            tempLo = Float.isNaN(tempLo) || temp < tempLo ? temp : tempLo;
            tempHi = Float.isNaN(tempHi) || temp > tempHi ? temp : tempHi;
            rainLo = Float.isNaN(rainLo) || rain < rainLo ? rain : rainLo;
            rainHi = Float.isNaN(rainHi) || rain > rainHi ? rain : rainHi;
        }

        tempMid = tempLo + (tempHi - tempLo) / 2;
        rainMid = rainLo + (rainHi - rainLo) / 2;

        tempDiffSufficient = true;
        rainDiffSufficient = true;
        if (useMetric) {
            tempVariation = 1.5f;
            rainVariation = 5f;

            if (tempHi - tempLo <= tempVariation) {
                tempDiffSufficient = false;
            }

            if (rainHi - rainLo <= rainVariation) {
                rainDiffSufficient = false;
            }
        } else {
            tempVariation = 3f;
            rainVariation = 0.2f;

            if (tempHi - tempLo <= tempVariation) {
                tempDiffSufficient = false;
            }

            if (rainHi - rainLo <= rainVariation) {
                rainDiffSufficient = false;
            }
        }

        coolDays.clear();
        hotDays.clear();
        dryDays.clear();
        wetDays.clear();

        LinkedList<Integer> indices = new LinkedList<Integer>();
        LinkedList<String> types = new LinkedList<String>();
        LinkedList<Date> dates = new LinkedList<Date>();

        JSONObject longRun = null;
        Date longDate = null;
        float longTemperature = Float.NaN;
        float longRain = Float.NaN;
        int longIndex = -1;

        for (int ahead = 1; ahead < 10; ahead++) {
            Date from = DateHelper.getNDaysAhead(today, ahead);
            Date to = DateHelper.getNDaysAhead(today, ahead + 1);

            temp = WeatherWrapper.getTemperatureMaxValue(from);
            rain = WeatherWrapper.getRainValue(to);

            ArrayList<JSONObject> activities = MainActivity.sActivityHelper.getActivities(from, to, false);
            // MainActivity.sActivityHelper.getActivities(from, to).get(0).getJSONArray("details").get(0)
            // MainActivity.sActivityHelper.getActivities(from, to).get(0).getJSONArray("details").get(0).getJSONArray("selectedSubgroups")

            if (activities == null || activities.size() == 0) {
                dates.add(from);
                types.add("rest");
                indices.add(ahead - 1);

                determineCategories();
            }

            for (JSONObject activity : activities) {
                if (!activity.has("details")) {
                    continue;
                }

                JSONArray details;
                try {
                    details = activity.getJSONArray("details");
                }
                catch (JSONException e) {
                    continue;
                }

                if (details.length() == 0) {
                    continue;
                }

                // TODO Only looks at the first entry!

                JSONObject detail;
                try {
                    detail = details.getJSONObject(0);
                } catch (JSONException e) {
                    continue;
                }

                try {
                    if (detail.getInt("kind") != 4) {
                        // TODO
                        continue;
                    }

                    temp = WeatherWrapper.getTemperatureMaxValue(from);
                    rain = WeatherWrapper.getRainValue(from);
                    JSONArray selectedSubgroups = detail.getJSONArray("selectedSubgroups");

                    boolean processed = false;
                    for (int i = 0; i < selectedSubgroups.length(); i++) {
                        String subgroup = selectedSubgroups.getString(i);

                        if (processed) {
                            continue;
                        }

                        if (subgroup.equals("easySlowPace")) {
                            dates.add(from);
                            types.add("easySlowPace");
                            indices.add(ahead - 1);

                            determineCategories();

                            processed = true;
                            break;
                        } else if (subgroup.equals("fartlek") || subgroup.equals("intervals")) {
                            dates.add(from);
                            types.add("intervals");
                            indices.add(ahead - 1);

                            determineCategories();

                            processed = true;
                            break;
                        } else if (subgroup.equals("longRun")) {
                            // Note: Focus on first long run.
                            if (longRun == null) {
                                longIndex = ahead - 1;
                                longDate = from;
                                longRun = activity;
                                longTemperature = temp;
                                longRain = rain;
                            }

                            processed = true;
                            break;
                        }
                    }

                    if (!processed) {
                        dates.add(from);
                        types.add("none");
                        indices.add(ahead - 1);

                        determineCategories();
                    }
                } catch (JSONException e) {
                    continue;
                }
            }
        }

        // 1. See if the long run should be swapped.
        //    a. Yes? Look to swap for a HIIT run first.
        //    b. Look for a "normal" run.
        //    c. Alternatively, check for an easy run.

        // 2. See if HIIT runs should be swapped.
        //    a. Look for a "normal" run.
        //    b. Look for an easy run.

        int switchWithIndex = -1;
        swap.clear();
        swapInv.clear();
        reason.clear();
        if (longRun != null) {
            String reasonString = null;

            if (tempDiffSufficient && longTemperature > tempMid + tempVariation * 0.1f) {
                // Too hot!
                switchWithIndex = reschedule(longIndex, types, indices, coolDays, new String[] { "intervals", "none", "easySlowPace", "rest" });
                reasonString = "cooler";
            } else if (rainDiffSufficient && longRain > rainMid + rainVariation * 0.1f) {
                // Too wet!
                switchWithIndex = reschedule(longIndex, types, indices, dryDays, new String[] { "intervals", "none", "easySlowPace", "rest" });
                reasonString = "drier";
            }

            if (switchWithIndex >= 0) {
                swap.put(longIndex, switchWithIndex);
                swapInv.put(switchWithIndex, longIndex);
                reason.put(longIndex, reasonString);
            }
        }

        LinkedList<Boolean>[] boolDays = new LinkedList[] { hotDays, wetDays };
        LinkedList<Boolean>[] boolDaysInv = new LinkedList[] { coolDays, dryDays };

        for (int j = 0; j < 2; j++) {
            if ((!tempDiffSufficient && boolDays[j] == hotDays) || (!rainDiffSufficient && boolDays[j] == wetDays)) {
                continue;
            }

            for (int i = 0; i < types.size(); i++) {
                int index = indices.get(i);

                if (types.get(i).equals("intervals") && boolDays[j].get(index) && !swap.containsKey(index) && !swapInv.containsKey(index)) {
                    switchWithIndex = reschedule(index, types, indices, boolDaysInv[j], new String[]{ "none", "easySlowPace", "rest"}, swap, swapInv);

                    if (switchWithIndex >= 0) {
                        swap.put(index, switchWithIndex);
                        swapInv.put(switchWithIndex, index);

                        if (boolDaysInv[j] == dryDays) {
                            reason.put(index, "drier");
                        } else {
                            reason.put(index, "cooler");
                        }
                    }
                }
            }
        }

        for (int j = 0; j < 2; j++) {
            if ((!tempDiffSufficient && boolDays[j] == hotDays) || (!rainDiffSufficient && boolDays[j] == wetDays)) {
                continue;
            }

            for (int i = 0; i < types.size(); i++) {
                int index = indices.get(i);

                if ((types.get(i).equals("none") || types.get(i).equals("easySlowPace")) && boolDays[j].get(index) && !swap.containsKey(index) && !swapInv.containsKey(index)) {
                    switchWithIndex = reschedule(index, types, indices, boolDaysInv[j], new String[]{"rest"}, swap, swapInv);

                    if (switchWithIndex >= 0) {
                        swap.put(index, switchWithIndex);
                        swapInv.put(switchWithIndex, index);

                        if (boolDaysInv[j]== dryDays) {
                            reason.put(index, "drier");
                        } else {
                            reason.put(index, "cooler");
                        }
                    }
                }
            }
        }

        /*
        Log.d("x", "" + DateHelper.getNDaysAhead(today, 1));
        for (int src : swap.keySet()) {
            int dest = swap.get(src);

            Log.d("x", "SWAPPING " + types.get(src) + " with " + types.get(dest));
            Log.d("x", " - " + dates.get(src) + " with " + dates.get(dest));
            Log.d("x", " reason: " + reason.get(src));
        }
        Log.d("x", "y");
         */

        valuesValid = true;
    }
}
