package com.burntrac.sunrunai;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by kim on 9/3/17.
 */

public class ActivityDetailsAdapter extends BaseAdapter {
    private static final int UNBOUNDED = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

    private Context mContext;
    private int mPosition;
    private JSONArray mActivities;
    private HashMap<Integer, ActivityDetailsView> mItems;
    private Date mPlanStart;

    public ActivityDetailsAdapter(Context context, int position, JSONArray activities, Date planstart) {
        mContext = context;
        mPosition = position;
        mActivities = activities == null ? new JSONArray() : activities;
        mItems = new HashMap<Integer, ActivityDetailsView>();
        mPlanStart = planstart;
    }

    @Override
    public int getCount() {
        return mActivities.length();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ActivityDetailsView view;
        JSONObject details = null;
        try {
            details = mActivities.length() > position ? mActivities.getJSONObject(position) : null;
        } catch (JSONException e) {
            details = null;
        }

        if (convertView == null) {
            view = new ActivityDetailsView(mContext, null, position, details, mPlanStart);
            view.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            mItems.put(position, view);
        } else {
            view = (ActivityDetailsView)convertView;

            view.override(position, details, mPlanStart);

            mItems.put(position, view);
        }

        //view.setText(texts[position]);

        return view;
    }

    public int getMeasuredHeight() {
        int height = 0;

        for (int position = 0; position < getCount(); position++) {
            View view = getView(position, null, null);

            view.measure(UNBOUNDED, UNBOUNDED);

            height += view.getMeasuredHeight();
        }

        return height;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        for (ActivityDetailsView view : mItems.values()) {
            view.invalidate();
        }
    }
}
