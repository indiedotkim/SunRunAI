package com.burntrac.sunrunai;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * Created by kim on 8/29/17.
 */

public class ActivityView extends LinearLayout {
    private static final int AT_MOST = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST);
    private static final int UNBOUNDED = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

    private View mValue;
    private ImageView mImage;

    private int mPosition;
    private JSONObject mActivity;
    private JSONObject mActivityActual;

    private ActivityDetailsAdapter mActivityDetailsAdapter;

    public ActivityView(Context context, AttributeSet attrs, int position, JSONObject activity, JSONObject activityActual) {
        super(context, attrs);

        mPosition = position;
        mActivity = activity;
        mActivityActual = activityActual;

        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_activity, this, true);

        JSONArray details = null;
        try {
            details = mActivity != null && mActivity.has("details") ? mActivity.getJSONArray("details") : null;
        } catch (JSONException e) {
            // No problem.
        }

        setViewValues();
    }

    public void override(int position, JSONObject activity, JSONObject activityactual) {
        mPosition = position;
        mActivity = activity;
        mActivityActual = activityactual;

        setViewValues();
    }

    @Override
    public void invalidate() {
        super.invalidate();

        mActivityDetailsAdapter.notifyDataSetChanged();
    }

    public int getCalculatedHeight() {
        int height; // Not needed here: = mActivityDetailsAdapter != null && mActivity != null ? mActivityDetailsAdapter.getMeasuredHeight() : 0;

        this.measure(AT_MOST, UNBOUNDED);
        height = this.getMeasuredHeight();

        return height;
    }

    private void setViewValues() {
        GridView view = (GridView)findViewById(R.id.activitydetailsview);

        JSONArray details = null; // new JSONArray((Collection)mPlan.getField("details"));

        try {
            details = mActivity != null ? mActivity.getJSONArray("details") : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mActivityDetailsAdapter = new ActivityDetailsAdapter(view.getContext(), mPosition, details, null, mActivity, mActivityActual);

        view.setAdapter(mActivityDetailsAdapter);
    }
}
