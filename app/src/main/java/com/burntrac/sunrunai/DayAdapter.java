package com.burntrac.sunrunai;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import im.delight.android.ddp.db.Collection;

/**
 * Created by kim on 8/28/17.
 */

public class DayAdapter extends BaseAdapter {
    private Context mContext;
    private HashMap<Integer, DayView> mItems;

    public DayAdapter(Context context) {
        mContext = context;
        mItems = new HashMap<Integer, DayView>();
    }

    public int getCount() {
        if (MeteorWrapper.meteor == null) {
            mItems.clear();

            return 0;
        }
return 1;
        /*
        Collection collection = MeteorWrapper.meteor.getDatabase().getCollection("activity");

        return collection == null ? 0 : collection.count();
        */
    }

    public Object getItem(int position) {
        return mItems.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        DayView view;

        if (convertView == null) {
            ArrayList activities = new ArrayList();
            activities.add(ActivityHelper.createActivity());

            view = new DayView(mContext, null, activities);
            view.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            mItems.put(position, view);
        } else {
            view = (DayView)convertView;

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
        }
    }
}
