package com.burntrac.sunrunai;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.delight.android.ddp.db.Document;

/**
 * Created by kim on 8/29/17.
 */

public class DayView extends LinearLayout {
    DayAdapter mDayAdapter;
    private Context mContext;
    private int mPosition;
    private Date mDate;
    private View mValue;
    private ImageView mImage;
    private ArrayList mActivities;
    private ArrayList mActivitiesActual;

    private ActivityAdapter mActivityAdapter;

    public DayView(DayAdapter dayAdapter, Context context, AttributeSet attrs, int position, Date date, ArrayList activities, ArrayList activitiesActual) {
        super(context, attrs);

        mDayAdapter = dayAdapter;
        mContext = context;
        mPosition = position;
        mDate = date;
        mActivities = activities;
        mActivitiesActual = activitiesActual;

        /*
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
         */

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_day, this, true);

        //TextView title = (TextView)getChildAt(0);
        //title.setText(titleText);

        //mValue = getChildAt(1);
        //mValue.setBackgroundColor(valueColor);

        //mImage = (ImageView) getChildAt(2);

        final DayView self = this;
        TextView dayActivityListOverlay = (TextView)findViewById(R.id.dayactivitylistoverlay);
        dayActivityListOverlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                self.showDialog();
            }
        });

        setViewValues();
    }

    public void setActualDistance(View view) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean useMetric = sharedPrefs.getBoolean(SettingsActivity.PREF_USE_METRIC, SettingsActivity.DEFAULT_USE_METRIC);

        SeekBar distanceBar = (SeekBar)view.findViewById(R.id.dayactualslider);

        TextView dayActualDistance = (TextView)view.findViewById(R.id.dayactualdistance);
        float distance = (float)distanceBar.getProgress();
        if (distance > 0) {
            dayActualDistance.setText(DistanceHelper.formatDistance(mContext, distance, useMetric ? 2 : 3));
        } else {
            dayActualDistance.setText("none");
        }
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final View view = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_day, null);

        TextView dialogDayHeader = (TextView)view.findViewById(R.id.dialogdayheader);
        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/FasterOne-Regular.ttf");
        dialogDayHeader.setTypeface(typeface);

        TextView dayPlannedDistance = (TextView)view.findViewById(R.id.dayplanneddistance);
        if (mActivities != null &&
            mActivities.size() > 0 &&
                ((JSONObject)mActivities.get(0)).has("details")) {
            float metricDistance = 0;

            try {
                metricDistance = ActivityHelper.getDetailsDistanceSum(((JSONObject)mActivities.get(0)).getJSONArray("details"));
            } catch (Exception e) {
                // Too late...
            }

            dayPlannedDistance.setText(DistanceHelper.formatDistance(mContext, metricDistance, 2));
        } else {
            dayPlannedDistance.setText("none");
        }

        setActualDistance(view);

        final DayView self = this;
        final SeekBar distanceBar = (SeekBar)view.findViewById(R.id.dayactualslider);
        distanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                self.setActualDistance(view);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        builder.setView(view);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                boolean useMetric = sharedPrefs.getBoolean(SettingsActivity.PREF_USE_METRIC, SettingsActivity.DEFAULT_USE_METRIC);

                HashMap detail = new HashMap();
                ArrayList details = ActivityHelper.createActivityDetails(new ArrayList[] {},
                0,
                0,
                0,
                4,
                        (float)distanceBar.getProgress(),
                useMetric ? 2 : 3);
                JSONObject activity = ActivityHelper.createActivity("Achievement", null, mDate, 0,
                        details);

                Map activityMap = ActivityHelper.jsonObjectToMap(activity);
                MainActivity.sActivityHelper.addActivity(activityMap);

                mDayAdapter.notifyDataSetChanged();
                mActivityAdapter.notifyDataSetChanged();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //Intent i = new Intent(getApplicationContext(), PlanActivity.class);
                //startActivity(i);
            }
        });

        dialog.show();
    }

    public void override(int position, Date date, ArrayList activities, ArrayList activitiesActual) {
        mPosition = position;
        mDate = date;
        mActivities = activities;
        mActivitiesActual = activitiesActual;

        setViewValues();
    }

    private void setViewValues() {
        /*
        if ((mPosition % 2) == 0) {
            this.setBackgroundColor(Color.rgb(0xd2, 0xe7, 0xff));
        } else {
            this.setBackgroundColor(Color.rgb(0xc2, 0xd7, 0xef));
        }
        */

        ((TextView)findViewById(R.id.date)).setText(DateHelper.formatDate(mDate));
        ((TextView)findViewById(R.id.datesuffix)).setText(DateHelper.formatDateSuffix(mDate));

        ((TextView)findViewById(R.id.temperature)).setText(WeatherWrapper.getTemperature(mDate));

        String maxTemp = WeatherWrapper.getTemperatureMax(mDate);
        if (maxTemp != null && maxTemp.length() > 0) {
            ((TextView) findViewById(R.id.temperature)).setText(WeatherWrapper.getTemperatureMax(mDate));
        }

        String tempMin = WeatherWrapper.getTemperatureMin(mDate);
        if (tempMin == null || tempMin.length() == 0) {
            ((TextView)findViewById(R.id.temperaturemin)).setText("");
            ((TextView)findViewById(R.id.temperaturehi)).setText("Temperature");
            ((TextView)findViewById(R.id.temperaturelo)).setText("");
        } else {
            ((TextView)findViewById(R.id.temperaturemin)).setText(WeatherWrapper.getTemperatureMin(mDate));
            ((TextView)findViewById(R.id.temperaturehi)).setText("Hi");
            ((TextView)findViewById(R.id.temperaturelo)).setText("Lo");
        }

        ((TextView)findViewById(R.id.rain)).setText(WeatherWrapper.getRain(mDate));
        ((TextView)findViewById(R.id.winddirection)).setText(WeatherWrapper.getWindDirection(mDate));
        ((TextView)findViewById(R.id.windspeed)).setText(WeatherWrapper.getWindSpeed(mDate));

        GridView gridview = (GridView)findViewById(R.id.dayactivitylist);
        mActivityAdapter = new ActivityAdapter(gridview.getContext(), mActivities, mActivitiesActual);
        gridview.setAdapter(mActivityAdapter);
    }

    @Override
    public void invalidate() {
        super.invalidate();

        mActivityAdapter.notifyDataSetChanged();
    }
}
