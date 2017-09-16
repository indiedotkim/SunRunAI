package com.burntrac.sunrunai;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import im.delight.android.ddp.db.Document;

/**
 * Created by kim on 9/9/17.
 */

public class PlanView extends LinearLayout {
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
    private static final int UNBOUNDED = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

    private int mPosition;
    private Date mDate = new Date();
    private View mValue;
    private Document mPlan;

    private ActivityDetailsAdapter mActivityDetailsAdapter;

    public PlanView(Context context, AttributeSet attrs, int position, Document plan) {
        super(context, attrs);

        mPosition = position;
        mPlan = plan;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (mPosition == -1) {
            inflater.inflate(R.layout.view_plan_details, this, true);

            Button previous = (Button)findViewById(R.id.previousbutton);
            previous.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDate = DateHelper.getPreviousDay(mDate);

                    PlanView.this.setViewValues(true);
                }
            });

            Button next = (Button)findViewById(R.id.nextbutton);
            next.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDate = DateHelper.getNextDay(mDate);

                    PlanView.this.setViewValues(true);
                }
            });
        } else {
            inflater.inflate(R.layout.view_plan, this, true);
        }

        setViewValues();
    }

    public void override(int position, Document plan) {
        mPosition = position;
        mPlan = plan;

        setViewValues();
    }

    private void setViewValues() {
        setViewValues(false);
    }

    private void setViewValues(boolean updateDateOnly) {
        if (mPlan == null) {
            return;
        }

        if (mPosition == -1) {
            TextView startDay = (TextView)findViewById(R.id.startday);
            TextView startOrdinal = (TextView)findViewById(R.id.startordinal);
            TextView startRemainder = (TextView)findViewById(R.id.startremainder);

            Calendar calendar = new GregorianCalendar();

            calendar.setTime(mDate);
            startDay.setText("" + calendar.get(Calendar.DAY_OF_MONTH));
            startOrdinal.setText(DateHelper.formatDateSuffix(mDate));
            startRemainder.setText(dateFormat.format(mDate));

            if (updateDateOnly) {
                return;
            }

            GridView view = (GridView)findViewById(R.id.planactivitylist);
            JSONArray details = new JSONArray((Collection)mPlan.getField("details"));

            mActivityDetailsAdapter = new ActivityDetailsAdapter(view.getContext(), mPosition, details, mDate);

            view.setAdapter(mActivityDetailsAdapter);
        }

        ((TextView)findViewById(R.id.name)).setText((String)mPlan.getField("name"));
    }

    public Document getPlan() {
        return mPlan;
    }

    public Date getStartDate() {
        return mDate;
    }

    public int getCalculatedHeight() {
        int height = mActivityDetailsAdapter != null ? mActivityDetailsAdapter.getMeasuredHeight() : 0;

        this.measure(UNBOUNDED, UNBOUNDED);
        height += this.getMeasuredHeight();

        return height;
    }

    @Override
    public void invalidate() {
        super.invalidate();

        mActivityDetailsAdapter.notifyDataSetChanged();
    }
}
