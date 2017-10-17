package com.burntrac.sunrunai;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import im.delight.android.ddp.db.Collection;
import im.delight.android.ddp.db.Database;
import im.delight.android.ddp.db.Document;

/**
 * Created by kim on 9/9/17.
 */

public class PlanAdapter extends BaseAdapter {
    private Context mContext;
    private HashMap<Integer, PlanView> mItems;

    private Document[] mDocuments;

    public PlanAdapter(Context context) {
        mContext = context;
        mItems = new HashMap<Integer, PlanView>();
        mDocuments = new Document[0];
    }

    @Override
    public int getCount() {
        if (MeteorWrapper.meteor == null) {
            mItems.clear();

            return 0;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean useMetric = sharedPrefs.getBoolean(SettingsActivity.PREF_USE_METRIC, SettingsActivity.DEFAULT_USE_METRIC);

        int count = 0;
        int retries = 50;
        do {
            try {
                Database database = MeteorWrapper.meteor.getDatabase();

                if (database == null) {
                    mDocuments = new Document[0];

                    return 0;
                }

                Collection collection = database.getCollection("activityplan");

                if (collection == null) {
                    mDocuments = new Document[0];

                    return 0;
                }

                count = collection.count();

                retries = 0;
            } catch (ConcurrentModificationException cme) {
                retries--;
            }
        } while (retries > 0);

        Document[] documentArray = null;
        retries = 50;
        do {
            try {
                documentArray = MeteorWrapper.meteor.getDatabase().getCollection("activityplan").find();

                retries = 0;
            } catch (ConcurrentModificationException cme) {
                retries--;
            }
        } while (retries > 0);

        if (documentArray == null) {
            mDocuments = new Document[0];

            return 0;
        }

        final float metricGoal = PlanActivity.userGoal;
        final float metricGoalDiff = useMetric ? PlanActivity.userGoalDiff : DistanceHelper.getMetric(PlanActivity.userGoalDiff, false);
        LinkedList<LinkedHashMap> goals = new LinkedList<>();
        for (Document document : documentArray) {
            List<String> fieldNames = Arrays.asList(document.getFieldNames());
            LinkedHashMap goal = ActivityHelper.findGoal(document);

            goal.put("selfreference", document);

            double competitionDistance;
            Object distanceObject = fieldNames.indexOf("competitiondistance") >= 0 ? document.getField("competitiondistance") : null;
            if (distanceObject == null) {
                competitionDistance = 0;
            } if (distanceObject instanceof Integer) {
                competitionDistance = ((Integer)distanceObject).doubleValue();
            } else {
                competitionDistance = (double)distanceObject;
            }
            if (competitionDistance >= metricGoal - metricGoalDiff &&
                competitionDistance <= metricGoal + metricGoalDiff) {
                goals.add(goal);
            }
        }

        /*
        Comparator<LinkedHashMap> comparator = new Comparator<LinkedHashMap>() {
            @Override
            public int compare(LinkedHashMap left, LinkedHashMap right) {
                float metricLeft = ActivityHelper.getMetricGoalDistance(left);
                float metricRight = ActivityHelper.getMetricGoalDistance(right);

                float distanceFromGoalLeft = Math.abs(metricLeft);
                float distanceFromGoalRight = Math.abs(metricRight);

                return (int)(-1 * Math.abs(distanceFromGoalLeft - distanceFromGoalRight));
            }
        };

        Collections.sort(goals, comparator);
        */

        mDocuments = new Document[goals.size()];
        for (int i = 0; i < mDocuments.length; i++) {
            mDocuments[i] = (Document)goals.get(i).get("selfreference");
        }

        return goals.size();
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

        Document document = position < mDocuments.length ? mDocuments[position] : null;

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
