package com.burntrac.sunrunai;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by kim on 9/8/17.
 */

public class DateHelper {
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("MMM, d");

    public static Date getMidnight(Date date) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static Date getNextDay(Date date) {
        return getNDaysAhead(date, 1);
    }
    public static Date getPreviousDay(Date date) {
        return getNDaysAhead(date, -1);
    }

    public static Date getNDaysAhead(Date date, int n) {
        if (n == 0) {
            return date;
        }

        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.add(Calendar.DATE, n);

        return calendar.getTime();
    }

    public static String formatDate(Date date) {
        if (date == null) {
            date = getMidnight(new Date());
        }

        return dateFormat.format(date);
    }

    public static String formatDateSuffix(Date date) {
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        if (dayOfMonth >= 11 && dayOfMonth <= 13) {
            return "th";
        }

        switch (dayOfMonth % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }
}
