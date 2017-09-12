package com.burntrac.sunrunai;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Date;

import im.delight.android.ddp.db.Document;

/**
 * Created by kim on 9/9/17.
 */

public class PlanView extends LinearLayout {
    private static final int UNBOUNDED = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

    private int mPosition;
    private Date mDate;
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
        if (mPlan == null) {
            return;
        }

        ((TextView)findViewById(R.id.name)).setText((String)mPlan.getField("name"));

        if (mPosition == -1) {
            GridView view = (GridView)findViewById(R.id.planactivitylist);
            mActivityDetailsAdapter = new ActivityDetailsAdapter(view.getContext(), mPosition, (ArrayList) mPlan.getField("details"));
            view.setAdapter(mActivityDetailsAdapter);
        }
    }

    public Document getPlan() {
        return mPlan;
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
