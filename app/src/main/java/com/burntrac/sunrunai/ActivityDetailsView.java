package com.burntrac.sunrunai;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

/**
 * Created by kim on 9/2/17.
 */

public class ActivityDetailsView extends LinearLayout {
    public ActivityDetailsView(Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_activity_details, this, true);
    }
}
