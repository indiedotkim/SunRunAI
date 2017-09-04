package com.burntrac.sunrunai;

import android.content.Context;
import android.content.res.Resources;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kim on 8/30/17.
 */

public final class ResourceResolver {
    public final static String prefsPlan = "sunrunai.plan";

    private final static Pattern cssIcon = Pattern.compile("bt-[a-z]+-icon-[a-z]+");

    public static String getSafeIdentifier(String unsafe) {
        return unsafe.replaceAll("[-]+", "_");
    }

    public static String getIconFromClasses(String classes) {
        Matcher m = cssIcon.matcher(classes);

        if (m.find()) {
            return getSafeIdentifier(m.group());
        }

        return null;
    }

    public static String getDrawableForIcon(Context context, String icon) {
        Resources resources = context.getResources();

        return resources.getString(resources.getIdentifier(icon, "string", context.getPackageName()));
    }

    public static int getIdentifierForDrawable(Context context, String drawable) {
        return context.getResources().getIdentifier(drawable, "drawable", context.getPackageName());
    }

    public static int getIdentifierForDrawableIcon(Context context, String icon) {
        String drawable = getDrawableForIcon(context, icon);

        return getIdentifierForDrawableIcon(context, drawable);
    }
}
