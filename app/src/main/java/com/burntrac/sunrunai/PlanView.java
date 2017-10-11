package com.burntrac.sunrunai;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
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

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;

import im.delight.android.ddp.db.Document;

/**
 * Created by kim on 9/9/17.
 */

public class PlanView extends LinearLayout {
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
    private static final int AT_MOST = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST);
    private static final int UNBOUNDED = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

    private Context mContext;
    private int mPosition;
    private static Date sDate = new Date();
    private View mValue;
    private Document mPlan;

    private ActivityDetailsAdapter mActivityDetailsAdapter;

    public PlanView(Context context, AttributeSet attrs, int position, Document plan) {
        super(context, attrs);

        mContext = context;
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
                    sDate = DateHelper.getPreviousDay(sDate);

                    PlanView.this.setViewValues(true);
                }
            });

            Button next = (Button)findViewById(R.id.nextbutton);
            next.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    sDate = DateHelper.getNextDay(sDate);

                    PlanView.this.setViewValues(true);
                }
            });
        } else {
            inflater.inflate(R.layout.view_plan, this, true);

            sDate = new Date();
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

            calendar.setTime(sDate);
            startDay.setText("" + calendar.get(Calendar.DAY_OF_MONTH));
            startOrdinal.setText(DateHelper.formatDateSuffix(sDate));
            startRemainder.setText(dateFormat.format(sDate));

            if (updateDateOnly) {
                return;
            }

            GridView view = (GridView)findViewById(R.id.planactivitylist);
            JSONArray details = new JSONArray((Collection)mPlan.getField("details"));

            mActivityDetailsAdapter = new ActivityDetailsAdapter(view.getContext(), mPosition, details, sDate);

            view.setAdapter(mActivityDetailsAdapter);
        }

        ((TextView)findViewById(R.id.name)).setText((String)mPlan.getField("name"));

        // mPlan.getField("comments").get(0).get("comment")
        ArrayList comments = (ArrayList)mPlan.getField("comments");
        if (comments != null && comments.size() > 0) {
            LinkedHashMap comment = (LinkedHashMap)comments.get(0);
            String commentString = (String)comment.get("comment");

            if (commentString != null) {
                ((TextView)findViewById(R.id.description)).setText(commentString);
            } else {
                ((TextView)findViewById(R.id.description)).setText("No description available.");
            }
        } else {
            ((TextView)findViewById(R.id.description)).setText("No description available.");
        }

        LinkedHashMap goal = ActivityHelper.findGoal(mPlan);
        if (goal != null && goal.get("distance") != null && goal.get("distancetype") != null) {
            float distance = Float.parseFloat(goal.get("distance").toString());
            int distancetype = Integer.parseInt(goal.get("distancetype").toString());

            ((TextView)findViewById(R.id.plangoal)).setText(DistanceHelper.formatDistance(mContext.getApplicationContext(), distance, distancetype));
        }
    }

    public Document getPlan() {
        return mPlan;
    }

    public static Date getStartDate() {
        return sDate;
    }

    public int getCalculatedHeight() {
        int height = mActivityDetailsAdapter != null ? mActivityDetailsAdapter.getMeasuredHeight() : 0;

        this.measure(AT_MOST, UNBOUNDED);
        height += this.getMeasuredHeight();

        return height;
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (mActivityDetailsAdapter != null) {
            mActivityDetailsAdapter.notifyDataSetChanged();
        }
    }
}
