package com.burntrac.sunrunai;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
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

    private GridView mViewDayActivityList;
    private ConstraintLayout mLayoutDayGradient;
    private TextView mViewDate;
    private TextView mViewDateSuffix;
    private TextView mViewTemperature;
    private TextView mViewTemperatureHi;
    private TextView mViewTemperatureLo;
    private TextView mViewTemperatureMin;
    private TextView mViewRain;
    private TextView mViewWindDirection;
    private TextView mViewWindSpeed;
    private TextView mViewDayActivityListOverlay;

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

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_day, this, true);

        //TextView title = (TextView)getChildAt(0);
        //title.setText(titleText);

        //mValue = getChildAt(1);
        //mValue.setBackgroundColor(valueColor);

        //mImage = (ImageView) getChildAt(2);

        mLayoutDayGradient = (ConstraintLayout)findViewById(R.id.daygradient);

        mViewDayActivityList = (GridView)findViewById(R.id.dayactivitylist);

        mViewDate = findViewById(R.id.date);
        mViewDateSuffix = findViewById(R.id.datesuffix);
        mViewTemperature = findViewById(R.id.temperature);
        mViewTemperatureHi = findViewById(R.id.temperaturehi);
        mViewTemperatureLo = findViewById(R.id.temperaturelo);
        mViewTemperatureMin = findViewById(R.id.temperaturemin);
        mViewRain = findViewById(R.id.rain);
        mViewWindDirection = findViewById(R.id.winddirection);
        mViewWindSpeed = findViewById(R.id.windspeed);
        mViewDayActivityListOverlay = findViewById(R.id.dayactivitylistoverlay);

        final DayView self = this;
        mViewDayActivityListOverlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                self.showDialog();
            }
        });

        setViewValues();
    }

    public void setActualDistance(View view, float distancePlanned) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean useMetric = sharedPrefs.getBoolean(SettingsActivity.PREF_USE_METRIC, SettingsActivity.DEFAULT_USE_METRIC);

        SeekBar distanceBar = (SeekBar)view.findViewById(R.id.dayactualslider);

        if (distancePlanned > 0) {
            distanceBar.setProgress((int)Math.floor(distancePlanned));
        }

        TextView dayActualDistance = (TextView)view.findViewById(R.id.dayactualdistance);
        float distance = (float)distanceBar.getProgress();
        if (distance > 0) {
            dayActualDistance.setText(DistanceHelper.formatDistance(mContext, distance, useMetric ? 2 : 3));
        } else {
            dayActualDistance.setText("none");
        }
    }

    public void showDialog() {
        if (mActivities != null &&
            mActivities.size() > 0 &&
            ((JSONObject)mActivities.get(0)).has("details")) {

            try {
                JSONObject activity = (JSONObject)mActivities.get(0);

                Long datetime = activity.getLong("datetime");
                Date activityDate = new Date(activity.getLong("datetime"));
                Long change = activity.has("sunrunai_change") ? activity.getLong("sunrunai_change") : 0;

                if (change != 0) {
                    showDialogConfirm(datetime);
                } else {
                    showDialogAchievement();
                }
            } catch (Exception e) {
                // Too late...
            }
        }
    }

    public void showDialogConfirm(final long datetime) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final View view = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_ai_confirm, null);

        TextView dialogDayHeader = (TextView)view.findViewById(R.id.dialogconfirmheader);
        dialogDayHeader.setTypeface(MainActivity.sSpeedFont);

        builder.setView(view);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //JSONObject activity = ActivityHelper.createActivity("Achievement", null, mDate, 0, details);

                //Map activityMap = ActivityHelper.jsonObjectToMap(activity);
                JSONObject activity;
                try {
                    activity = (JSONObject)mActivities.get(0);
                    activity.put("sunrunai_fixed", true);
                } catch (JSONException e) {
                    // Ignore.

                    return;
                }

                MainActivity.sActivityHelper.addActivity(ActivityHelper.jsonObjectToMap(activity));

                mDayAdapter.notifyDataSetChanged();
                mActivityAdapter.notifyDataSetChanged();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
            }
        });

        dialog.show();
    }

    public void showDialogAchievement() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final View view = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_day, null);

        TextView dialogDayHeader = (TextView)view.findViewById(R.id.dialogdayheader);
        dialogDayHeader.setTypeface(MainActivity.sSpeedFont);

        boolean isFuture = mDate.getTime() > DateHelper.getMidnight(new Date()).getTime();

        final SeekBar distanceBar = (SeekBar)view.findViewById(R.id.dayactualslider);
        TextView dayFutureMessage = (TextView)view.findViewById(R.id.dayfuturemessage);
        if (isFuture) {
            dayFutureMessage.setVisibility(VISIBLE);
            distanceBar.setEnabled(true);
        } else {
            dayFutureMessage.setVisibility(GONE);
            distanceBar.setEnabled(false);
        }

        float metricDistance = 0;
        TextView dayPlannedDistance = (TextView)view.findViewById(R.id.dayplanneddistance);
        if (mActivities != null &&
            mActivities.size() > 0 &&
            ((JSONObject)mActivities.get(0)).has("details")) {

            try {
                JSONObject activity = (JSONObject)mActivities.get(0);

                Date activityDate = new Date(activity.getLong("datetime"));
                metricDistance = ActivityHelper.getDetailsDistanceSum(activity.getJSONArray("details"));
            } catch (Exception e) {
                // Too late...
            }

            dayPlannedDistance.setText(DistanceHelper.formatDistance(mContext, metricDistance, 2));
        } else {
            dayPlannedDistance.setText("none");
        }

        setActualDistance(view, DistanceHelper.plainDistance(mContext, metricDistance, 2));

        final DayView self = this;
        distanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                self.setActualDistance(view, 0);
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

        if (!isFuture) {
            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                    boolean useMetric = sharedPrefs.getBoolean(SettingsActivity.PREF_USE_METRIC, SettingsActivity.DEFAULT_USE_METRIC);

                    HashMap detail = new HashMap();
                    ArrayList details = ActivityHelper.createActivityDetails(new ArrayList[]{},
                            0,
                            0,
                            0,
                            4,
                            (float) distanceBar.getProgress(),
                            useMetric ? 2 : 3);
                    JSONObject activity = ActivityHelper.createActivity("Achievement", null, mDate, 0, details);

                    Map activityMap = ActivityHelper.jsonObjectToMap(activity);
                    MainActivity.sActivityHelper.addActivity(activityMap);

                    mDayAdapter.notifyDataSetChanged();
                    mActivityAdapter.notifyDataSetChanged();
                }
            });
        }

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

        boolean byAI = false;
        if (AI.isActivated && AI.valuesValid && mActivities.size() > 0) {
            JSONObject activity = (JSONObject)mActivities.get(0);

            if (activity.has("sunrunai_change")) {
                try {
                    int change = activity.getInt("sunrunai_change");

                    if (change != 0) {
                        byAI = true;
                    }
                }
                catch (JSONException je) {
                    // Ignore.
                }
            }
        }

        Date today = DateHelper.getMidnight(new Date());
        int gradientId = ResourceResolver.getIdentifierForDrawable(mContext, "gradient_activity_today");
        String gradientName = "gradient_activity";
        if (mDate.getTime() == today.getTime()) {
            gradientName = "gradient_activity_today";
        } else if (mDate.getTime() < today.getTime()) {
            gradientName = "gradient_activity_past";
        } else if (byAI) {
            gradientName = "gradient_activity_ai";
        }
        mLayoutDayGradient.setBackground(mContext.getResources().getDrawable(ResourceResolver.getIdentifierForDrawable(mContext, gradientName), null));

        mViewDate.setText(DateHelper.formatDate(mDate));
        mViewDateSuffix.setText(DateHelper.formatDateSuffix(mDate));

        mViewTemperature.setText(WeatherWrapper.getTemperature(mDate));

        String maxTemp = WeatherWrapper.getTemperatureMax(mDate);
        if (maxTemp != null && maxTemp.length() > 0) {
            mViewTemperature.setText(WeatherWrapper.getTemperatureMax(mDate));
        }

        String tempMin = WeatherWrapper.getTemperatureMin(mDate);
        if (tempMin == null || tempMin.length() == 0) {
            mViewTemperatureMin.setText("");
            mViewTemperatureHi.setText("Temperature");
            mViewTemperatureLo.setText("");
        } else {
            mViewTemperatureMin.setText(WeatherWrapper.getTemperatureMin(mDate));
            mViewTemperatureHi.setText("Hi");
            mViewTemperatureLo.setText("Lo");
        }

        mViewRain.setText(WeatherWrapper.getRain(mDate));
        mViewWindDirection.setText(WeatherWrapper.getWindDirection(mDate));
        mViewWindSpeed.setText(WeatherWrapper.getWindSpeed(mDate));

        mActivityAdapter = new ActivityAdapter(mViewDayActivityList.getContext(), mActivities, mActivitiesActual);
        mViewDayActivityList.setAdapter(mActivityAdapter);
    }

    @Override
    public void invalidate() {
        super.invalidate();

        mActivityAdapter.notifyDataSetChanged();
    }
}
