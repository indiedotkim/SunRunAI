package com.burntrac.sunrunai;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Created by kim on 8/29/17.
 */

public class ActivityView extends LinearLayout {
    private View mValue;
    private ImageView mImage;

    private HashMap mActivity;

    private ActivityDetailsAdapter activityDetailsAdapter;

    public ActivityView(Context context, AttributeSet attrs, HashMap activity) {
        super(context, attrs);

        mActivity = activity;

        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_activity, this, true);

        setActivity(context);
        ListView listview = (ListView)findViewById(R.id.activitydetailsview);
        activityDetailsAdapter = new ActivityDetailsAdapter(context);
        listview.setAdapter(activityDetailsAdapter);
    }

    public ActivityView(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    public ActivityView(Context context) {
        this(context, null);
    }

    private void setActivity(Context context) {
        Log.d("X", ">>> " + MeteorWrapper.getCount("activitytypes"));
        String cssIcon = (String)MeteorWrapper.findKVMatch("activitytypes", "activityno", mActivity.get("kind"), "icon");
        ImageView icon = (ImageView)findViewById(R.id.activityicon);
        if (cssIcon != null) {
            String iconName = ResourceResolver.getIconFromClasses(cssIcon);
            icon.setImageResource(ResourceResolver.getIdentifierForDrawable(context, iconName));
        }

        String name = Generic.hasValue((String)mActivity.get("name")) ? (String)mActivity.get("name") : "-";
        ((TextView)findViewById(R.id.activityname)).setText(name);

    }
    @Override
    public void invalidate() {
        super.invalidate();

        setActivity(getContext());
    }
}
