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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kim on 8/29/17.
 */

public class ActivityView extends LinearLayout {
    private View mValue;
    private ImageView mImage;

    private int mPosition;
    private HashMap mActivity;

    private ActivityDetailsAdapter mActivityDetailsAdapter;

    public ActivityView(Context context, AttributeSet attrs, int position, HashMap activity) {
        super(context, attrs);

        mPosition = position;
        mActivity = activity;

        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_activity, this, true);

        ArrayList details = mActivity != null && mActivity.containsKey("details") ? (ArrayList)mActivity.get("details") : null;

        setActivity(context);

        ListView listview = (ListView)findViewById(R.id.activitydetailsview);
        mActivityDetailsAdapter = new ActivityDetailsAdapter(context, mPosition, details);
        listview.setAdapter(mActivityDetailsAdapter);
    }

    public void override(int position, HashMap activity) {
        mPosition = position;
        mActivity = activity;
    }

    private void setActivity(Context context) {
        if (mActivity == null) {
            ((TextView)findViewById(R.id.activityname)).setText("IS NULL");

            return;
        }

        String name = Generic.hasValue((String)mActivity.get("name")) ? (String)mActivity.get("name") : "-";
        ((TextView)findViewById(R.id.activityname)).setText(name);

    }
    @Override
    public void invalidate() {
        super.invalidate();

        setActivity(getContext());
    }
}
