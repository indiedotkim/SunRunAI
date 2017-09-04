package com.burntrac.sunrunai;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by kim on 9/2/17.
 */

public class ActivityDetailsAdapter extends BaseAdapter {
    private Context mContext;

    public ActivityDetailsAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ActivityDetailsView view;

        if (convertView == null) {
            view = new ActivityDetailsView(mContext);
            view.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            view = (ActivityDetailsView)convertView;
        }

        //view.setText(texts[position]);

        return view;
    }
}
