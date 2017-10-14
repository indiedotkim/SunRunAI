package com.burntrac.sunrunai;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kim on 9/3/17.
 */

public class ActivityAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<JSONObject> mActivities;
    private ArrayList<JSONObject> mActivitiesActual;
    private HashMap<Integer, ActivityView> mItems;

    public ActivityAdapter(Context context, ArrayList activities, ArrayList activitiesActual) {
        mContext = context;
        mActivities = activities;
        mActivitiesActual = activitiesActual;
        mItems = new HashMap<Integer, ActivityView>();
    }

    @Override
    public int getCount() {
        return 2;
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
        ActivityView view;
        JSONObject activity = mActivities.size() > position ? mActivities.get(position) : null;
        JSONObject activityActual = mActivitiesActual.size() > 0 ? mActivitiesActual.get(0) : null;

        if (convertView == null) {
            view = new ActivityView(mContext, null, position, activity, activityActual);
            view.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, view.getCalculatedHeight()));

            mItems.put(position, view);
        } else {
            view = (ActivityView)convertView;
            view.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, view.getCalculatedHeight()));

            view.override(position, activity, activityActual);

            mItems.put(position, view);
        }

        //view.setText(texts[position]);

        return view;
    }

    private int getListHeight() {
        return getCount() * 200;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        for (ActivityView view : mItems.values()) {
            view.invalidate();
            view.forceLayout();
        }
    }
}
