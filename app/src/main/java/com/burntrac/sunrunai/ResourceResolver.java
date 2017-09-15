package com.burntrac.sunrunai;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import im.delight.android.ddp.db.Collection;
import im.delight.android.ddp.db.Database;
import im.delight.android.ddp.db.Document;

/**
 * Created by kim on 8/30/17.
 */

public final class ResourceResolver {
    public final static String prefsPlan = "sunrunai.plan";

    private final static Pattern cssIcon = Pattern.compile("bt-[a-z]+-icon-[a-z]+");

    public static String getSafeIdentifier(String unsafe) {
        return unsafe.replaceAll("[-]+", "_");
    }

    public static String labelToIdentifier(String label) {
        String identifier = label.toLowerCase()
                                .replaceAll("[^a-zA-Z0-9 ]", " ")
                                .replaceAll(" +", " ");

        StringBuffer identifierWUpperCase = new StringBuffer("" + identifier.charAt(0));
        for (int index = 1 ; index < identifier.length(); index++) {
            if (identifier.charAt(index - 1) == ' ') {
                identifierWUpperCase.append(("" + identifier.charAt(index)).toUpperCase());
            } else {
                identifierWUpperCase.append(identifier.charAt(index));
            }
        }

        return identifierWUpperCase.toString().replaceAll(" +", "");
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

    public static String getTagStringForSubgroups(JSONArray tags, int kind) {
        return getTagString(tags, kind, "subgroups");
    }

    public static String getTagString(JSONArray tags, int kind, String type) {
        Database database = MeteorWrapper.meteor.getDatabase();

        if (database == null) {
            return "";
        }

        Collection collection = database.getCollection("activitytypes");

        if (collection == null) {
            return "";
        }

        Document result = MeteorWrapper.meteor.getDatabase().getCollection("activitytypes").whereEqual("activityno", kind).findOne();

        if (result == null) {
            return "";
        }

        ArrayList typeArray = (ArrayList)result.getField(type);

        if (typeArray.size() == 0) {
            return "";
        }

        ArrayList<String> tagStrings = new ArrayList<String>();
        for (int index = 0; index < typeArray.size(); index++) {
            for (int tag = 0; tag < tags.length(); tag++) {
                try {
                    if (tags.getString(tag).equals(ResourceResolver.labelToIdentifier((String)typeArray.get(index)))) {
                        tagStrings.add((String)typeArray.get(index));
                    }
                } catch (JSONException e) {
                    // Ignore. This would be a model error.
                }
            }
        }

        return TextUtils.join(", ", tagStrings);
    }
}
