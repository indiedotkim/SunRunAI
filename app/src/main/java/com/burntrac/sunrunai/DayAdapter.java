package com.burntrac.sunrunai;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import im.delight.android.ddp.db.Collection;
import im.delight.android.ddp.db.Database;

/**
 * Created by kim on 8/28/17.
 */

public class DayAdapter extends BaseAdapter {
    private MainActivity mMain;
    private Context mContext;
    private HashMap<Integer, DayView> mItems;

    private Date mStart;
    private Date mEnd;

    public DayAdapter(MainActivity main, Context context) {
        mMain = main;
        mContext = context;
        mItems = new HashMap<Integer, DayView>();
    }

    public int getCount() {
        if (MeteorWrapper.meteor == null) {
            //mItems.clear();

            //return 0;
        }

        // Determine max of:
        // - number of weather-data days, if no activities are present, or,
        // - number of activity days

        /*
        Collection collection = MeteorWrapper.meteor.getDatabase().getCollection("activity");

        return collection == null ? 0 : collection.count();
        */

        ArrayList<JSONObject> activities = MainActivity.sActivityHelper.getActivities();

        Date today = DateHelper.getMidnight(new Date());
        Date today10 = DateHelper.getMidnight(DateHelper.getNDaysAhead(new Date(), 11));

        // new Date(activities.get(0).getLong("datetime"))
        if (activities != null && activities.size() > 0) {
            try {
                Date start = DateHelper.getMidnight(new Date(activities.get(0).getLong("datetime")));
                Date end = DateHelper.getMidnight(new Date(activities.get(activities.size() - 1).getLong("datetime")));

                long weatherDays = WeatherWrapper.getDaysWithDataAvailable();

                if (today.before(start)) {
                    start = today;
                } else if (today.after(end)) {
                    end = today;
                }

                if (today10.after(end) && weatherDays > 0) {
                    end = today10;
                }

                long daysInclRestDays = 1 + (end.getTime() - start.getTime()) / (24 * 60 * 60 * 1000);

                mStart = start;
                mEnd = DateHelper.getMidnight(DateHelper.getNDaysAhead(end, 1));

                return (int)daysInclRestDays;
            } catch (JSONException e) {
                // Ignore. This would be a bug in the data model.
            }
        }

        mStart = today;
        mEnd = DateHelper.getMidnight(DateHelper.getNDaysAhead(today10, 1));

        return WeatherWrapper.getDaysWithDataAvailable();
    }

    public Object getItem(int position) {
        return mItems.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        DayView view;

        ArrayList activities;

        if (mStart != null && mEnd != null) {
            Date viewStart = DateHelper.getNDaysAhead(mStart, position);
            Date viewEnd = DateHelper.getNDaysAhead(mStart, position + 1);

            activities = MainActivity.sActivityHelper.getActivities(viewStart, viewEnd);
        } else {
            activities = new ArrayList();
            //activities.add(ActivityHelper.createActivity());
        }

        Date date = DateHelper.getNDaysAhead(DateHelper.getMidnight(new Date()), position);

        if (convertView == null) {
            view = new DayView(mContext, null, position, date, activities);
            view.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            mItems.put(position, view);
        } else {
            view = (DayView)convertView;
            view.override(position, date, activities);

            mItems.put(position, view);
        }

        //view.setText(texts[position]);

        return view;
    }

    @Override
    public synchronized void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        for (DayView view : mItems.values()) {
            view.invalidate();
            view.forceLayout();
        }
    }

    public Date getStartDate() {
        Date earliest = DateHelper.getMidnight(new Date());

        if (WeatherWrapper.getDaysWithDataAvailable() > 0) {
            // Still today.
        }

        return earliest;
    }
}
