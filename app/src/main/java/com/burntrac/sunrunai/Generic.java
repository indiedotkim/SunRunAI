package com.burntrac.sunrunai;

/**
 * Created by kim on 9/3/17.
 */

public class Generic {
    public static final int SCHEDULE_PLANNED = 1;

    public static final int STATUS_PLAN_ADDED = 1;

    public static final int MDIGITS = 2; // Mantissa digits.

    public static final boolean hasValue(Integer integer) {
        if (integer == null) {
            return false;
        } else if (integer != 0) {
            return true;
        } else {
            return false;
        }
    }

    public static final boolean hasValue(String string) {
        if (string == null) {
            return false;
        } else if (string.length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static final float displayFloat(float value, int digits) {
        return (float)(Math.round(value * Math.pow(10, digits)) / Math.pow(value, digits));
    }

    public static final float displayFloat(float value) {
        return displayFloat(value, MDIGITS);
    }
}
