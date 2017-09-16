package com.burntrac.sunrunai;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import im.delight.android.ddp.db.Document;

public class PlanActivity extends AppCompatActivity {

    private PlanAdapter mPlanAdapter;

    private static int INITIAL_GOAL = 10;
    public static int userGoal = INITIAL_GOAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorGradientActivityStart, null)));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Message").setTitle("Title");
        AlertDialog dialog = builder.create();

        //dialog.show();

        final TextView goalComposite = (TextView)findViewById(R.id.goalcomposite);

        SeekBar goalBar = (SeekBar)findViewById(R.id.goalbar);
        goalBar.setProgress(INITIAL_GOAL);

        goalBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean useMetric = sharedPrefs.getBoolean(SettingsActivity.PREF_USE_METRIC, SettingsActivity.DEFAULT_USE_METRIC);

                goalComposite.setText(DistanceHelper.formatDistance(getApplicationContext(), (float)progress, useMetric ? 2 : 3));

                userGoal = progress;

                mPlanAdapter.notifyDataSetChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        ListView view = (ListView)findViewById(R.id.planlist);
        mPlanAdapter = new PlanAdapter(view.getContext());
        view.setAdapter(mPlanAdapter);

        final Context self = this;
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parentView, final View childView, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(self);

                final Document plan = ((PlanView)childView).getPlan();
                View view = new PlanView(self, null, -1, plan);
                builder.setView(view);

                builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Date planStart = ((PlanView)childView).getStartDate();

                        for (Object object : (ArrayList)plan.getField("details")) {
                            LinkedHashMap detail = (LinkedHashMap)object;
                            HashMap activity = new HashMap();

                            activity.put("name", "");
                            activity.put("comments", new ArrayList());

                            Date activityDate = DateHelper.getNDaysAhead(planStart, ((int)detail.get("week") - 1) * 8 + (int)detail.get("day") - 1);
                            activity.put("datetime", activityDate.getTime());

                            activity.put("deleted", false);
                            activity.put("schedule", Generic.SCHEDULE_PLANNED);

                            List details = new LinkedList();
                            details.add(detail);

                            activity.put("details", details);

                            MainActivity.sActivityHelper.addActivity(activity);
                        }


                        setResult(Generic.STATUS_PLAN_ADDED);
                        finish();
                    }
                });

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
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.plans_menu, menu);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.planclear:
                MainActivity.sActivityHelper.deleteScheduledActivities();
                finish();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void changeUnits(View view) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean useMetric = sharedPrefs.getBoolean(SettingsActivity.PREF_USE_METRIC, SettingsActivity.DEFAULT_USE_METRIC);

        sharedPrefs.edit().putBoolean(SettingsActivity.PREF_USE_METRIC, !useMetric).commit();

        mPlanAdapter.notifyDataSetChanged();
    }
}
