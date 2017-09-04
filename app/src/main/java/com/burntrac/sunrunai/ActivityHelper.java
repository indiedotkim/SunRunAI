package com.burntrac.sunrunai;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by kim on 9/2/17.
 */

public class ActivityHelper {
    public static HashMap createActivity() {
        HashMap activity = new HashMap();

        activity.put("name", "Name!");
        activity.put("comments", new ArrayList());
        activity.put("datetime", new Date().getTime());
        activity.put("deleted", false);
        activity.put("kind", 4);
        activity.put("schedule", 0);

        ArrayList details = new ArrayList();

        HashMap detail = new HashMap();
        detail.put("comments", new ArrayList());

        detail.put("hours", 0);
        detail.put("minutes", 30);
        detail.put("seconds", 10);

        detail.put("kind", 4);
        detail.put("distance", 10);
        detail.put("distancetype", 2);

        detail.put("gear", null);
        detail.put("injury", null);

        activity.put("details", details);

        return activity;
    }
}
