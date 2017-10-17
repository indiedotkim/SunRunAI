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

    private TextView mViewAnnotation;
    private ImageView mViewTypeIcon;
    private ImageView mViewActivityIcon;
    private TextView mViewTime;
    private TextView mViewDistance;
    private TextView mViewDeltaDays;
    private TextView mViewDayLabel;
    private TextView mViewTypeLabel;
    private TextView mViewTags;
    private TextView mViewActualDiff;

    private TextView mViewDate;
    private TextView mViewDateOrdinal;
    private TextView mViewWeek;
    private TextView mViewWeekLabel;
    private TextView mViewStartDay;

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

            mViewActivityIcon = (ImageView)findViewById(R.id.activityicon);
            mViewTime = (TextView)findViewById(R.id.time);
            mViewDistance = (TextView)findViewById(R.id.distance);
            mViewDayLabel = (TextView)findViewById(R.id.daylabel);
            mViewTags = (TextView)findViewById(R.id.tags);

            mViewDate = (TextView)findViewById(R.id.date);
            mViewDateOrdinal = (TextView)findViewById(R.id.dateordinal);

            mViewWeek = (TextView)findViewById(R.id.week);
            mViewWeekLabel = (TextView)findViewById(R.id.weeklabel);
            mViewStartDay = (TextView)findViewById(R.id.startday);
        } else {
            inflater.inflate(R.layout.view_activity_details_ai, this, true);

            mViewAnnotation = (TextView)findViewById(R.id.annotation);
            mViewTypeIcon = (ImageView)findViewById(R.id.typeicon);
            mViewActivityIcon = (ImageView)findViewById(R.id.activityicon);
            mViewTime = (TextView)findViewById(R.id.time);
            mViewDistance = (TextView)findViewById(R.id.distance);
            mViewDeltaDays = (TextView)findViewById(R.id.deltadays);
            mViewDayLabel = (TextView)findViewById(R.id.daylabel);
            mViewTypeLabel = (TextView)findViewById(R.id.typelabel);
            mViewTags = (TextView)findViewById(R.id.tags);

            mViewActualDiff = (TextView)findViewById(R.id.actualdiff);

            mViewActualDiff.setTypeface(MainActivity.sSpeedFont);
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
            mViewAnnotation.setVisibility(VISIBLE);
            mViewTypeIcon.setVisibility(VISIBLE);
            mViewTime.setVisibility(VISIBLE);
            mViewDistance.setVisibility(VISIBLE);
            mViewDeltaDays.setVisibility(VISIBLE);
            mViewDayLabel.setVisibility(VISIBLE);
            mViewTypeLabel.setVisibility(VISIBLE);
            mViewTags.setVisibility(VISIBLE);
        }

        String cssIcon = (String)MeteorWrapper.findKVMatch("activitytypes", "activityno", kind, "icon");
        if (cssIcon != null) {
            String iconName = ResourceResolver.getIconFromClasses(cssIcon);

            if (iconName != "bt_activity_icon_running") {
                mViewActivityIcon.setImageResource(ResourceResolver.getIdentifierForDrawable(mViewActivityIcon.getContext(), iconName));
            }
        }

        if (mActivityDetails.has("selectedSubgroups")) {
            JSONArray subgroups;

            try {
                subgroups = mActivityDetails.getJSONArray("selectedSubgroups");
            } catch (JSONException e) {
                subgroups = new JSONArray();
            }

            if (subgroups.length() > 0) {
                mViewTags.setText(ResourceResolver.getTagStringForSubgroups(subgroups, kind));
            } else {
                mViewTags.setText("-\n ");
            }
        }

        if (mViewWeek != null) {
            try {
                String weekValue = Generic.hasValue(mActivityDetails.getInt("week")) ? "" + mActivityDetails.getInt("week") : "-";
                mViewWeek.setText(weekValue);
            } catch (JSONException e) {
                // No problem.
            }
        }
        if (mViewStartDay != null) {
            try {
                String dayValue = Generic.hasValue(mActivityDetails.getInt("day")) ? "" + mActivityDetails.getInt("day") : "-";
                mViewStartDay.setText(dayValue);
            } catch (JSONException e) {
                // No problem.
            }
        }

        if (mPlanStart == null) {
            if (mActivityActual != null) {
                mViewActualDiff.setVisibility(VISIBLE);

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
                            mViewActualDiff.setText("+" + String.format("%.0f", distanceActual - distance));
                        } else {
                            mViewActualDiff.setText("−" + String.format("%.0f", distance - distanceActual)); // Minus sign from Fontbook.
                        }
                    } else {
                        if (mActivity.getBoolean("sunrunai_restday")) {
                            mViewActualDiff.setText(DistanceHelper.formatDistance(mContext, distance, distancetype, true));

                            mViewAnnotation.setVisibility(INVISIBLE);
                            mViewTypeIcon.setVisibility(INVISIBLE);
                            mViewTime.setVisibility(INVISIBLE);
                            mViewDistance.setVisibility(INVISIBLE);
                            mViewDeltaDays.setVisibility(INVISIBLE);
                            mViewDayLabel.setVisibility(INVISIBLE);
                            mViewTypeLabel.setVisibility(INVISIBLE);
                            mViewTags.setVisibility(INVISIBLE);
                        } else {
                            mViewActualDiff.setText("DONE");
                        }
                    }
                } catch (JSONException e) {
                    // Ignore.
                }
            } else {
                mViewActualDiff.setVisibility(INVISIBLE);
            }
        }

        if (mPlanStart != null && mViewWeek != null && mViewStartDay != null) {
            try {
                int weekNo = Generic.hasValue(mActivityDetails.getInt("week")) ? mActivityDetails.getInt("week") : 1;
                int dayNo = Generic.hasValue(mActivityDetails.getInt("day")) ? mActivityDetails.getInt("day") : 1;

                Date activityDate = DateHelper.getNDaysAhead(mPlanStart, (weekNo - 1) * 8 + dayNo - 1);

                mViewDate.setText(DateHelper.formatDate(activityDate));
                mViewDateOrdinal.setText(DateHelper.formatDateSuffix(activityDate));
            } catch (JSONException e) {
                // No problem.
            }
        } else if (mActivity != null) {
            try {
                if (mPlanStart != null) {
                    // Viewing a plan.
                    Date activityDate = new Date(mActivity.getLong("datetime"));

                    mViewDate.setText(DateHelper.formatDate(activityDate));
                    mViewDateOrdinal.setText(DateHelper.formatDateSuffix(activityDate));
                } else {
                    // Inside day view.
                    int change = mActivity.has("sunrunai_change") ? mActivity.getInt("sunrunai_change") : 0;
                    String changeString = change > 0 ? "+" + change : "" + change;

                    mViewDeltaDays.setText(changeString);
                    mViewAnnotation.setText(mActivity.get("sunrunai_reason").toString());

                    if (mActivity.has("sunrunai_fixed") && (boolean)mActivity.get("sunrunai_fixed")) {
                        mViewTypeIcon.setImageResource(R.drawable.ic_0922_link);
                    } else if ((int)mActivity.get("sunrunai_change") != 0) {
                        mViewTypeIcon.setImageResource(R.drawable.ic_robot);
                    } else {
                        mViewTypeIcon.setImageResource(R.drawable.ic_0852_clipboard4_blank);
                    }
                }
            } catch (JSONException e) {
                // No problem.
            }
        }
        //String name = Generic.hasValue((String)mActivityDetails.get("name")) ? (String)mActivityDetails.get("name") : "-";
        //((TextView)findViewById(R.id.activityname)).setText(name);

        try {
            mViewTime.setText(TimeHelper.formatHMS(mActivityDetails.getInt("hours"), mActivityDetails.getInt("minutes"), mActivityDetails.getInt("seconds")));
        } catch (JSONException e) {
            mViewTime.setText("");
        }

        try {
            mViewDistance.setText(DistanceHelper.formatDistance(mContext.getApplicationContext(), mActivityDetails.getInt("distance"), mActivityDetails.getInt("distancetype")));
        } catch (JSONException e) {
            mViewDistance.setText("");
        }
    }
    @Override
    public void invalidate() {
        super.invalidate();

        setViewValues();
    }
}
