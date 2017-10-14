package com.burntrac.sunrunai;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.codehaus.jackson.map.util.JSONPObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by kim on 8/29/17.
 */

public class ActivityDetailsView extends LinearLayout {
    private Context mContext;
    private View mValue;
    private ImageView mImage;

    private int mPosition;
    private JSONObject mActivity;
    private JSONObject mActivityActual;
    private JSONObject mActivityDetails;
    private Date mPlanStart;

    public ActivityDetailsView(Context context, AttributeSet attrs, int position, JSONObject activitydetails, Date planstart) {
        this(context, attrs, position, activitydetails, planstart, null, null);
    }

    public ActivityDetailsView(Context context, AttributeSet attrs, int position, JSONObject activitydetails, Date planstart, JSONObject activity, JSONObject activityActual) {
        super(context, attrs);

        mContext = context;
        mPosition = position;
        mActivity = activity;
        mActivityActual = activityActual;
        mActivityDetails = activitydetails;
        mPlanStart = planstart;

        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (planstart != null) {
            inflater.inflate(R.layout.view_activity_details, this, true);
        } else {
            inflater.inflate(R.layout.view_activity_details_ai, this, true);

            TextView actualDiff = (TextView)findViewById(R.id.actualdiff);
            Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/FasterOne-Regular.ttf");
            actualDiff.setTypeface(typeface);
        }

        setViewValues();
    }

    public void override(int position, JSONObject activity, Date planstart, JSONObject activityactual) {
        mPosition = position;
        mActivityDetails = activity;
        mPlanStart = planstart;
        mActivityActual = activityactual;

        setViewValues();
    }

    private void setViewValues() {
        if (mActivityDetails == null) {
            // Used?
            //((TextView)findViewById(R.id.activityname)).setText("IS NULL");

            return;
        }

        int kind;
        try {
            kind = mActivityDetails.getInt("kind");
        } catch (JSONException e) {
            // Nope! Need at least a kind!
            return;
        }

        if (mPlanStart == null) {
            findViewById(R.id.annotation).setVisibility(VISIBLE);
            findViewById(R.id.typeicon).setVisibility(VISIBLE);
            findViewById(R.id.time).setVisibility(VISIBLE);
            findViewById(R.id.distance).setVisibility(VISIBLE);
            findViewById(R.id.deltadays).setVisibility(VISIBLE);
            findViewById(R.id.daylabel).setVisibility(VISIBLE);
            findViewById(R.id.typelabel).setVisibility(VISIBLE);
            findViewById(R.id.tags).setVisibility(VISIBLE);
        }

        String cssIcon = (String)MeteorWrapper.findKVMatch("activitytypes", "activityno", kind, "icon");
        ImageView icon = (ImageView)findViewById(R.id.activityicon);
        if (cssIcon != null) {
            String iconName = ResourceResolver.getIconFromClasses(cssIcon);
            icon.setImageResource(ResourceResolver.getIdentifierForDrawable(icon.getContext(), iconName));
        }

        if (mActivityDetails.has("selectedSubgroups")) {
            JSONArray subgroups;

            try {
                subgroups = mActivityDetails.getJSONArray("selectedSubgroups");
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
                String weekValue = Generic.hasValue(mActivityDetails.getInt("week")) ? "" + mActivityDetails.getInt("week") : "-";
                week.setText(weekValue);
            } catch (JSONException e) {
                // No problem.
            }
        }
        if (day != null) {
            try {
                String dayValue = Generic.hasValue(mActivityDetails.getInt("day")) ? "" + mActivityDetails.getInt("day") : "-";
                day.setText(dayValue);
            } catch (JSONException e) {
                // No problem.
            }
        }

        if (mPlanStart == null) {
            TextView actualDiff = (TextView)findViewById(R.id.actualdiff);

            if (mActivityActual != null) {
                actualDiff.setVisibility(VISIBLE);

                try {
                    JSONArray details = mActivityActual.getJSONArray("details");
                    float distanceActual = ActivityHelper.getDetailsDistanceSum(details);

                    float distance = 0;
                    int distancetype = 2;
                    if (mActivity != null && mActivity.has("details")) {
                        details = mActivity.getJSONArray("details");
                        distance = ActivityHelper.getDetailsDistanceSum(details);
                        distancetype = ((JSONObject)details.get(0)).getInt("distancetype");
                    }

                    if (Math.abs(distanceActual - distance) > 0.1) {
                        if (distanceActual > distance) {
                            actualDiff.setText("+" + String.format("%.0f", distanceActual - distance));
                        } else {
                            actualDiff.setText("−" + String.format("%.0f", distance - distanceActual)); // Minus sign from Fontbook.
                        }
                    } else {
                        if (mActivity.getBoolean("sunrunai_restday")) {
                            actualDiff.setText(DistanceHelper.formatDistance(mContext, distance, distancetype, true));

                            findViewById(R.id.annotation).setVisibility(INVISIBLE);
                            findViewById(R.id.typeicon).setVisibility(INVISIBLE);
                            findViewById(R.id.time).setVisibility(INVISIBLE);
                            findViewById(R.id.distance).setVisibility(INVISIBLE);
                            findViewById(R.id.deltadays).setVisibility(INVISIBLE);
                            findViewById(R.id.daylabel).setVisibility(INVISIBLE);
                            findViewById(R.id.typelabel).setVisibility(INVISIBLE);
                            findViewById(R.id.tags).setVisibility(INVISIBLE);
                        } else {
                            actualDiff.setText("DONE");
                        }
                    }
                } catch (JSONException e) {
                    // Ignore.
                }
            } else {
                actualDiff.setVisibility(INVISIBLE);
            }
        }

        if (mPlanStart != null && week != null && day != null) {
            try {
                int weekNo = Generic.hasValue(mActivityDetails.getInt("week")) ? mActivityDetails.getInt("week") : 1;
                int dayNo = Generic.hasValue(mActivityDetails.getInt("day")) ? mActivityDetails.getInt("day") : 1;

                Date activityDate = DateHelper.getNDaysAhead(mPlanStart, (weekNo - 1) * 8 + dayNo - 1);

                ((TextView)findViewById(R.id.date)).setText(DateHelper.formatDate(activityDate));
                ((TextView)findViewById(R.id.dateordinal)).setText(DateHelper.formatDateSuffix(activityDate));
            } catch (JSONException e) {
                // No problem.
            }
        } else if (mActivity != null) {
            try {
                if (mPlanStart != null) {
                    // Viewing a plan.
                    Date activityDate = new Date(mActivity.getLong("datetime"));

                    ((TextView) findViewById(R.id.date)).setText(DateHelper.formatDate(activityDate));
                    ((TextView) findViewById(R.id.dateordinal)).setText(DateHelper.formatDateSuffix(activityDate));
                } else {
                    // Inside day view.
                    ((TextView) findViewById(R.id.deltadays)).setText(mActivity.get("sunrunai_change").toString());
                    ((TextView) findViewById(R.id.annotation)).setText(mActivity.get("sunrunai_reason").toString());

                    ImageView typeIcon = (ImageView)findViewById(R.id.typeicon);
                    if ((int)mActivity.get("sunrunai_change") != 0) {
                        typeIcon.setImageResource(R.drawable.ic_robot);
                    } else {
                        typeIcon.setImageResource(R.drawable.ic_0852_clipboard4_blank);
                    }
                }
            } catch (JSONException e) {
                // No problem.
            }
        }
        //String name = Generic.hasValue((String)mActivityDetails.get("name")) ? (String)mActivityDetails.get("name") : "-";
        //((TextView)findViewById(R.id.activityname)).setText(name);

        try {
            ((TextView)findViewById(R.id.time)).setText(TimeHelper.formatHMS(mActivityDetails.getInt("hours"), mActivityDetails.getInt("minutes"), mActivityDetails.getInt("seconds")));
        } catch (JSONException e) {
            ((TextView)findViewById(R.id.time)).setText("");
        }

        try {
            ((TextView)findViewById(R.id.distance)).setText(DistanceHelper.formatDistance(mContext.getApplicationContext(), mActivityDetails.getInt("distance"), mActivityDetails.getInt("distancetype")));
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
