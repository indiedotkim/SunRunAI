package com.burntrac.sunrunai;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kim on 8/29/17.
 */

public class DayView extends LinearLayout {
    private View mValue;
    private ImageView mImage;
    private ArrayList mActivities;

    private ActivityAdapter activityAdapter;

    public DayView(Context context, AttributeSet attrs, ArrayList activities) {
        super(context, attrs);

        mActivities = activities;

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.DayView, 0, 0);
        String titleText = a.getString(R.styleable.DayView_temperature);
        /*
        @SuppressWarnings("ResourceAsColor")
        int valueColor = a.getColor(R.styleable.ColorOptionsView_valueColor,
                android.R.color.holo_blue_light);
         */
        a.recycle();

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

        ((TextView)findViewById(R.id.temperature)).setText(WeatherWrapper.getTemperature());
        ((TextView)findViewById(R.id.rain)).setText(WeatherWrapper.getRain());
        ((TextView)findViewById(R.id.winddirection)).setText(WeatherWrapper.getWindDirection());
        ((TextView)findViewById(R.id.windspeed)).setText(WeatherWrapper.getWindSpeed());

        GridView gridview = (GridView)findViewById(R.id.dayactivitylist);
        activityAdapter = new ActivityAdapter(gridview.getContext(), mActivities);
        gridview.setAdapter(activityAdapter);
    }

    public DayView(Context context) {
        this(context, null, null);
    }

    @Override
    public void invalidate() {
        super.invalidate();

        activityAdapter.notifyDataSetChanged();
    }
}
