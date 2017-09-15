package com.burntrac.sunrunai;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kim on 8/29/17.
 */

public class ActivityView extends LinearLayout {
    private View mValue;
    private ImageView mImage;

    private int mPosition;
    private JSONObject mActivity;

    private ActivityDetailsAdapter mActivityDetailsAdapter;

    public ActivityView(Context context, AttributeSet attrs, int position, JSONObject activity) {
        super(context, attrs);

        mPosition = position;
        mActivity = activity;

        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_activity, this, true);

        JSONArray details = null;
        try {
            details = mActivity != null && mActivity.has("details") ? mActivity.getJSONArray("details") : null;
        } catch (JSONException e) {
            // No problem.
        }

        setActivity(context);

        ListView listview = (ListView)findViewById(R.id.activitydetailsview);
        mActivityDetailsAdapter = new ActivityDetailsAdapter(context, mPosition, details, null);
        listview.setAdapter(mActivityDetailsAdapter);
    }

    public void override(int position, JSONObject activity) {
        mPosition = position;
        mActivity = activity;
    }

    private void setActivity(Context context) {
        if (mActivity == null) {
            ((TextView)findViewById(R.id.activityname)).setText("Rest");

            ImageView icon = (ImageView)findViewById(R.id.activityicon);
            String iconName = ResourceResolver.getIconFromClasses("bt-activity-icon-rest");
            icon.setImageResource(ResourceResolver.getIdentifierForDrawable(icon.getContext(), iconName));

            return;
        }

        String name = null;
        try {
            name = Generic.hasValue(mActivity.getString("name")) ? mActivity.getString("name") : "-";
        } catch (JSONException e) {
            name = "-";
        }

        ((TextView)findViewById(R.id.activityname)).setText(name + "UU");

    }
    @Override
    public void invalidate() {
        super.invalidate();

        setActivity(getContext());
    }
}
