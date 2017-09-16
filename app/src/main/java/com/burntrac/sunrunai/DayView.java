package com.burntrac.sunrunai;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by kim on 8/29/17.
 */

public class DayView extends LinearLayout {
    private int mPosition;
    private Date mDate;
    private View mValue;
    private ImageView mImage;
    private ArrayList mActivities;

    private ActivityAdapter mActivityAdapter;

    public DayView(Context context, AttributeSet attrs, int position, Date date, ArrayList activities) {
        super(context, attrs);

        mPosition = position;
        mDate = date;
        mActivities = activities;

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

        setViewValues();
    }

    public void override(int position, Date date, ArrayList activities) {
        mPosition = position;
        mDate = date;
        mActivities = activities;

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
        mActivityAdapter = new ActivityAdapter(gridview.getContext(), mActivities);
        gridview.setAdapter(mActivityAdapter);
    }

    @Override
    public void invalidate() {
        super.invalidate();

        mActivityAdapter.notifyDataSetChanged();
    }
}
