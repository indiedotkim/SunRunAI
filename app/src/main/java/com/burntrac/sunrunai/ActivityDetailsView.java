package com.burntrac.sunrunai;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Created by kim on 8/29/17.
 */

public class ActivityDetailsView extends LinearLayout {
    private View mValue;
    private ImageView mImage;

    private int mPosition;
    private HashMap mActivity;

    public ActivityDetailsView(Context context, AttributeSet attrs, int position, HashMap activity) {
        super(context, attrs);

        mPosition = position;
        mActivity = activity;

        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_activity_details, this, true);

        setViewValues();
    }

    public void override(int position, HashMap activity) {
        mPosition = position;
        mActivity = activity;

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
        TextView day = (TextView)findViewById(R.id.day);

        if (week != null) {
            String weekValue = Generic.hasValue((Integer)mActivity.get("week")) ? "" + (Integer)mActivity.get("week") : "-";
            week.setText(weekValue);
        }
        if (day != null) {
            String dayValue = Generic.hasValue((Integer)mActivity.get("day")) ? "" + (Integer)mActivity.get("day") : "-";
            day.setText(dayValue);
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
