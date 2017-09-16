package com.burntrac.sunrunai;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;

import im.delight.android.ddp.db.Collection;
import im.delight.android.ddp.db.Database;
import im.delight.android.ddp.db.Document;

/**
 * Created by kim on 9/9/17.
 */

public class PlanAdapter extends BaseAdapter {
    private Context mContext;
    private HashMap<Integer, PlanView> mItems;

    public PlanAdapter(Context context) {
        mContext = context;
        mItems = new HashMap<Integer, PlanView>();
    }

    @Override
    public int getCount() {
        if (MeteorWrapper.meteor == null) {
            mItems.clear();

            return 0;
        }

        int count = 0;
        int retries = 50;
        do {
            try {
                Database database = MeteorWrapper.meteor.getDatabase();

                if (database == null) {
                    return 0;
                }

                Collection collection = database.getCollection("activityplan");

                if (collection == null) {
                    return 0;
                }

                return collection.count();
            } catch (ConcurrentModificationException cme) {
                retries--;
            }
        } while (retries > 0);

        return count;
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        PlanView view;
        Document[] documentArray = MeteorWrapper.meteor.getDatabase().getCollection("activityplan").find(1, position);
        Document document = documentArray != null && documentArray.length == 1 ? documentArray[0] : null;

        Date date = DateHelper.getNDaysAhead(DateHelper.getMidnight(new Date()), position);

        if (convertView == null) {
            view = new PlanView(mContext, null, position, document);
            view.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, view.getCalculatedHeight()));

            mItems.put(position, view);
        } else {
            view = (PlanView)convertView;
            view.override(position, document);
            view.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, view.getCalculatedHeight()));

            mItems.put(position, view);
        }

        //view.setText(texts[position]);

        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        for (PlanView view : mItems.values()) {
            view.invalidate();
        }
    }
}
