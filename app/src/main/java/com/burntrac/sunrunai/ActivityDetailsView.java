package com.burntrac.sunrunai;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private HashMap mActivity;
    private Date mPlanStart;

    public ActivityDetailsView(Context context, AttributeSet attrs, int position, HashMap activity, Date planstart) {
        super(context, attrs);

        mPosition = position;
        mActivity = activity;
        mPlanStart = planstart;

        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_activity_details, this, true);

        setViewValues();
    }

    public void override(int position, HashMap activity, Date planstart) {
        mPosition = position;
        mActivity = activity;
        mPlanStart = planstart;

        setViewValues();
    }

    private void setViewValues() {
        if (mActivity == null) {
            ((TextView)findViewById(R.id.activityname)).setText("IS NULL");

            return;
        }

        String cssIcon = (String)MeteorWrapper.findKVMatch("activitytypes", "activityno", mActivity.get("kind"), "icon");
        ImageView icon = (ImageView)findViewById(R.id.activityicon);
        if (cssIcon != null) {
            String iconName = ResourceResolver.getIconFromClasses(cssIcon);
            icon.setImageResource(ResourceResolver.getIdentifierForDrawable(icon.getContext(), iconName));
        }

        TextView week = (TextView)findViewById(R.id.week);
        TextView day = (TextView)findViewById(R.id.startday);

        if (week != null) {
            String weekValue = Generic.hasValue((Integer)mActivity.get("week")) ? "" + (Integer)mActivity.get("week") : "-";
            week.setText(weekValue);
        }
        if (day != null) {
            String dayValue = Generic.hasValue((Integer)mActivity.get("day")) ? "" + (Integer)mActivity.get("day") : "-";
            day.setText(dayValue);
        }

        if (mPlanStart != null && week != null && day != null) {
            int weekNo = Generic.hasValue((Integer)mActivity.get("week")) ? (Integer)mActivity.get("week") : 1;
            int dayNo = Generic.hasValue((Integer)mActivity.get("day")) ? (Integer)mActivity.get("day") : 1;

            Date activityDate = DateHelper.getNDaysAhead(mPlanStart, (weekNo - 1) * 8 + dayNo - 1);

            ((TextView)findViewById(R.id.date)).setText(DateHelper.formatDate(activityDate));
            ((TextView)findViewById(R.id.dateordinal)).setText(DateHelper.formatDateSuffix(activityDate));
        }
        //String name = Generic.hasValue((String)mActivity.get("name")) ? (String)mActivity.get("name") : "-";
        //((TextView)findViewById(R.id.activityname)).setText(name);

    }
    @Override
    public void invalidate() {
        super.invalidate();

        setViewValues();
    }
}
