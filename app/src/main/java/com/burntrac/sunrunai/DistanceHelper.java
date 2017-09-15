package com.burntrac.sunrunai;

/**
 * Created by kim on 9/14/17.
 */

public class DistanceHelper {
    public static String formatDistance(float distance, int distancetype) {
        return String.format("%.1f", distance) + "T" + distancetype;
    }
}
