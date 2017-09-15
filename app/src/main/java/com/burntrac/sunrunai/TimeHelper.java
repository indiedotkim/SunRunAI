package com.burntrac.sunrunai;

/**
 * Created by kim on 9/14/17.
 */

public class TimeHelper {
    public static String formatHMS(int hours, int minutes, int seconds) {
        if (hours == 0 && minutes == 0 && seconds == 0) {
            return "";
        }

        if (hours == 0) {
            return String.format("%02d:%02d", minutes, seconds);
        } else {
            return String.format("%2d:%02d:%02d", hours, minutes, seconds);
        }
    }
}
