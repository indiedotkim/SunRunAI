package com.burntrac.sunrunai;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kim on 9/3/17.
 */

public class ActivityAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList mActivities;
    private HashMap<Integer, ActivityView> mItems;

    public ActivityAdapter(Context context, ArrayList activities) {
        mContext = context;
        mActivities = activities;
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

        if (convertView == null) {
            HashMap activity = mActivities.size() > position ? (HashMap)mActivities.get(position) : null;

            view = new ActivityView(mContext, null, activity);
            view.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, getListHeight()));

            mItems.put(position, view);
        } else {
            view = (ActivityView)convertView;

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
        }
    }
}
