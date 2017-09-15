package com.burntrac.sunrunai;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * Created by kim on 8/29/17.
 */

public class ActivityDetailsView extends LinearLayout {
    private View mValue;
    private ImageView mImage;

    private int mPosition;
    private JSONObject mActivity;
    private Date mPlanStart;

    public ActivityDetailsView(Context context, AttributeSet attrs, int position, JSONObject activity, Date planstart) {
        super(context, attrs);

        mPosition = position;
        mActivity = activity;
        mPlanStart = planstart;

        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_activity_details, this, true);

        setViewValues();
    }

    public void override(int position, JSONObject activity, Date planstart) {
        mPosition = position;
        mActivity = activity;
        mPlanStart = planstart;

        setViewValues();
    }

    private void setViewValues() {
        if (mActivity == null) {
            // Used?
            ((TextView)findViewById(R.id.activityname)).setText("IS NULL");

            return;
        }

        int kind;
        try {
            kind = mActivity.getInt("kind");
        } catch (JSONException e) {
            // Nope! Need at least a kind!
            return;
        }

        String cssIcon = (String)MeteorWrapper.findKVMatch("activitytypes", "activityno", kind, "icon");
        ImageView icon = (ImageView)findViewById(R.id.activityicon);
        if (cssIcon != null) {
            String iconName = ResourceResolver.getIconFromClasses(cssIcon);
            icon.setImageResource(ResourceResolver.getIdentifierForDrawable(icon.getContext(), iconName));
        }

        if (mActivity.has("selectedSubgroups")) {
            JSONArray subgroups;

            try {
                subgroups = mActivity.getJSONArray("selectedSubgroups");
            } catch (JSONException e) {
                subgroups = new JSONArray();
            }

            TextView tags = (TextView)findViewById(R.id.tags);

            if (subgroups.length() > 0) {
                tags.setText(ResourceResolver.getTagStringForSubgroups(subgroups, kind));
            } else {
                tags.setText("-\n ");
            }
        }

        TextView week = (TextView)findViewById(R.id.week);
        TextView day = (TextView)findViewById(R.id.startday);

        if (week != null) {
            try {
                String weekValue = Generic.hasValue(mActivity.getInt("week")) ? "" + mActivity.getInt("week") : "-";
                week.setText(weekValue);
            } catch (JSONException e) {
                // No problem.
            }
        }
        if (day != null) {
            try {
                String dayValue = Generic.hasValue(mActivity.getInt("day")) ? "" + mActivity.getInt("day") : "-";
                day.setText(dayValue);
            } catch (JSONException e) {
                // No problem.
            }
        }

        if (mPlanStart != null && week != null && day != null) {
            try {
                int weekNo = Generic.hasValue(mActivity.getInt("week")) ? mActivity.getInt("week") : 1;
                int dayNo = Generic.hasValue(mActivity.getInt("day")) ? mActivity.getInt("day") : 1;

                Date activityDate = DateHelper.getNDaysAhead(mPlanStart, (weekNo - 1) * 8 + dayNo - 1);

                ((TextView)findViewById(R.id.date)).setText(DateHelper.formatDate(activityDate));
                ((TextView)findViewById(R.id.dateordinal)).setText(DateHelper.formatDateSuffix(activityDate));
            } catch (JSONException e) {
                // No problem.
            }
        }
        //String name = Generic.hasValue((String)mActivity.get("name")) ? (String)mActivity.get("name") : "-";
        //((TextView)findViewById(R.id.activityname)).setText(name);

        try {
            ((TextView)findViewById(R.id.time)).setText(TimeHelper.formatHMS(mActivity.getInt("hours"), mActivity.getInt("minutes"), mActivity.getInt("seconds")));
        } catch (JSONException e) {
            ((TextView)findViewById(R.id.time)).setText("");
        }

        try {
            ((TextView)findViewById(R.id.distance)).setText(DistanceHelper.formatDistance(mActivity.getInt("distance"), mActivity.getInt("distancetype")));
        } catch (JSONException e) {
            ((TextView)findViewById(R.id.distance)).setText("");
        }
    }
    @Override
    public void invalidate() {
        super.invalidate();

        setViewValues();
    }
}
