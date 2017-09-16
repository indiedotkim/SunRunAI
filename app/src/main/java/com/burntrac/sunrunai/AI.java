package com.burntrac.sunrunai;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by kim on 9/14/17.
 */

public class AI {
    public static void getOptimizedPlan() {

        WeatherWrapper.getTemperatureValue(new Date());

        JSONObject easyRun = null;
        JSONObject hiitRun = null;
        JSONObject longRun = null;
        ArrayList<JSONObject> xTraining = null;

        Date today = DateHelper.getMidnight(new Date());
        for (int ahead = 1; ahead <= 10; ahead++) {
            Date from = DateHelper.getNDaysAhead(today, ahead);
            Date to = DateHelper.getNDaysAhead(today, ahead + 1);

            ArrayList<JSONObject> activities = MainActivity.sActivityHelper.getActivities(from, to);

        }

        for (int ahead = 1; ahead <= 10; ahead++) {

            float temperature = WeatherWrapper.getTemperatureValue(new Date());
            float rain = WeatherWrapper.getRainValue(new Date());
        }
    }
}
