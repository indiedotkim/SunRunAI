package com.burntrac.sunrunai;

import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.TimeZone;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import im.delight.android.ddp.db.Document;
import im.delight.android.ddp.db.memory.InMemoryDatabase;
import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.ResultListener;

public class MainActivity extends AppCompatActivity implements MeteorCallback, ActivityCompat.OnRequestPermissionsResultCallback, OnCompletionListener {
    private final static String CONTEXT = "MainActivity";

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    private Menu mMenu;

    public static ActivityHelper sActivityHelper;
    public static Typeface sSpeedFont;

    private DayAdapter mDayAdapter;

    private String activityId;
    private String activityPlanId;
    private String activityTypesId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        WeatherWrapper.sContext = getApplicationContext();

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorGradientActivityStart, null)));

        synchronized (CONTEXT) {
            if (sActivityHelper == null) {
                sActivityHelper = new ActivityHelper(getApplicationContext());

                //sActivityHelper.deleteScheduledActivities();
            }
        }

        sSpeedFont = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/FasterOne-Regular.ttf");

        //mActivityHelper.addActivity(ActivityHelper.createActivity());

        final ImageView aiImage = (ImageView)findViewById(R.id.aiimage);
        final Switch aiSwitch = (Switch)findViewById(R.id.aiswitch);
        aiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    aiImage.setImageResource(R.drawable.ai);

                    AI.getOptimizedPlan(getApplicationContext());
                    AI.isActivated = true;

                    mDayAdapter.notifyDataSetChanged();
                } else {
                    aiImage.setImageResource(R.drawable.ai_grey);

                    AI.isActivated = false;

                    mDayAdapter.notifyDataSetChanged();
                }
            }
        });

        GridView gridview = (GridView)findViewById(R.id.daygridview);
        mDayAdapter = new DayAdapter(this, gridview.getContext());
        gridview.setAdapter(mDayAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (view instanceof DayView) {
                    ((DayView)view).showDialog();
                }
            }
        });

        MeteorWrapper.meteor = new Meteor(this, PrivateConfig.socket, new InMemoryDatabase());
        //MeteorWrapper.meteor = new Meteor(this, "ws://192.168.1.106:3000/websocket", new InMemoryDatabase());
        MeteorWrapper.meteor.addCallback(this);
        MeteorWrapper.meteor.connect();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = getLayoutInflater().inflate(R.layout.dialog_intro, null);
        builder.setView(view);
        builder.setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
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

        /*
        final WebView chart = (WebView)findViewById(R.id.ChartWebView);
        chart.getSettings().setJavaScriptEnabled(true);
        chart.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                chart.evaluateJavascript("document.getElementsByTagName(\"h1\")[0].innerHTML = \"HI!\";", null);
            }
        });
        String html = "<html><head><title>Bla</title></head><body><h1>Test!</h1><p>Hello!</p></body></html>";
        chart.loadData(html, "text/html", null);
         */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);

        mMenu = menu;

        updateMenuIcons();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;

        switch (item.getItemId()) {
            case R.id.info:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                View view = getLayoutInflater().inflate(R.layout.dialog_info, null);

                final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean randomize = sharedPrefs.getBoolean(SettingsActivity.PREF_RANDOMIZE, SettingsActivity.DEFAULT_RANDOMIZE);

                Switch randomizeSwitch = (Switch)view.findViewById(R.id.randomizeswitch);
                randomizeSwitch.setChecked(randomize);
                randomizeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        sharedPrefs.edit().putBoolean(SettingsActivity.PREF_RANDOMIZE, isChecked).commit();

                        if (isChecked) {
                            AI.getOptimizedPlan(getApplicationContext());

                            mDayAdapter.notifyDataSetChanged();
                        } else {
                            AI.getOptimizedPlan(getApplicationContext());

                            mDayAdapter.notifyDataSetChanged();
                        }
                    }
                });

                builder.setView(view);
                builder.setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
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

                return true;
            case R.id.plans:
                i = new Intent(getApplicationContext(), PlanActivity.class);
                startActivityForResult(i, Generic.STATUS_PLAN_ADDED);
                return true;
            case R.id.settings:
                i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onConnect(boolean signedInAutomatically) {
        if (signedInAutomatically) {
            MeteorWrapper.meteor.logout();
            signedInAutomatically = false;
        }

        if (signedInAutomatically) {
            //Log.d(CONTEXT, "Successfully logged in automatically");
        } else {
            MeteorWrapper.meteor.loginWithEmail(PrivateConfig.email, PrivateConfig.password, new ResultListener() {

                @Override
                public void onSuccess(String result) {
                    //Log.d(CONTEXT, "Successfully logged in: " + result);
                }

                @Override
                public void onError(String error, String reason, String details) {
                    //Log.d(CONTEXT, "Could not log in: " + error + " / " + reason + " / " + details);
                }

            });
        }

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();

        from.setTimeZone(TimeZone.getTimeZone("GMT"));
        from.add(Calendar.MONTH, -1);
        from.set(Calendar.HOUR_OF_DAY, 0);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);

        to.setTimeZone(TimeZone.getTimeZone("GMT"));
        to.set(Calendar.HOUR_OF_DAY, 23);
        to.set(Calendar.MINUTE, 59);
        to.set(Calendar.SECOND, 59);

        activityId = MeteorWrapper.meteor.subscribe("activity", new Object[]{from.getTime(), to.getTime()});

        // offset, limit, createdBy, types, goals
        activityPlanId = MeteorWrapper.meteor.subscribe("activityplan", new Object[]{0, 10, null, null, null});

        activityTypesId = MeteorWrapper.meteor.subscribe("activitytypes");

        // call an arbitrary method
        //meteor.call("myMethod");

        mLocationManager = (LocationManager)this.getSystemService(android.content.Context.LOCATION_SERVICE);

        final MainActivity self = this;
        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (location == null) {
                    return;
                }

                WeatherIntentService.location = location;

                WeatherWrapper.update(self.getApplicationContext(), self);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        getLocation();

        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    @Override
    public void onDestroy() {
        MeteorWrapper.meteor.unsubscribe(activityId);
        MeteorWrapper.meteor.unsubscribe(activityTypesId);

        MeteorWrapper.meteor.disconnect();
        MeteorWrapper.meteor.removeCallback(this);
        // or
        // meteor.removeCallbacks();

        // ...

        super.onDestroy();
    }

    @Override
    public void onException(Exception e) {
        System.out.println("Exception");
        if (e != null) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnect() {
        System.out.println("Disconnected");
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 200);
            return;
        }

        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            WeatherIntentService.location = location;

            WeatherWrapper.update(getApplicationContext(), this);
        } else {
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                WeatherIntentService.location = location;

                WeatherWrapper.update(getApplicationContext(), this);
            }
        }

        //mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);

        //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 60 * 1000, 1000, mLocationListener);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2 * 1000, 1000, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 200: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                }
            }
        }
    }

    public synchronized void onDataAdded(String collectionName, String documentID, String newValuesJson) {
        int retries = 100;

        do {
            try {
                // parse the JSON and manage the data yourself (not recommended)
                // or
                // enable a database (see section "Using databases to manage data") (recommended)
                /*
                Log.d(CONTEXT, "Added: " + collectionName + ", " + documentID + ", " + newValuesJson);
                Log.d(CONTEXT, "Collections: " + MeteorWrapper.meteor.getDatabase().count() + "(" + TextUtils.join(", ", MeteorWrapper.meteor.getDatabase().getCollectionNames()) + ")");
                Log.d(CONTEXT, "Activities: " + MeteorWrapper.meteor.getDatabase().getCollection("activity").count());
                Log.d(CONTEXT, "Activity Plans: " + MeteorWrapper.meteor.getDatabase().getCollection("activityplan").count());
                 */

                if (MeteorWrapper.meteor.getDatabase().getCollection("activityplan").count() > 0) {
                    Document doc = MeteorWrapper.meteor.getDatabase().getCollection("activityplan").findOne();
                    //Log.d(CONTEXT, "x");
                }

                retries = 0;
            } catch (ConcurrentModificationException cme) {
                retries--;
            }
        } while (retries > 0);

        mDayAdapter.notifyDataSetChanged();
    }

    public synchronized void onDataChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson) {
        // parse the JSON and manage the data yourself (not recommended)
        // or
        // enable a database (see section "Using databases to manage data") (recommended)
        //Log.d(CONTEXT, "Changed: " + collectionName + ", " + documentID + ", " + updatedValuesJson);
    }

    public synchronized void onDataRemoved(String collectionName, String documentID) {
        // parse the JSON and manage the data yourself (not recommended)
        // or
        // enable a database (see section "Using databases to manage data") (recommended)
        //Log.d(CONTEXT, "Removed: " + collectionName + ", " + documentID);
    }

    @Override
    public void onCompletion() {
        updateMenuIcons();

        AI.getOptimizedPlan(getApplicationContext());

        mDayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        updateMenuIcons();

        AI.getOptimizedPlan(getApplicationContext());

        mDayAdapter.notifyDataSetChanged();
    }

    private void updateMenuIcons() {
        ImageView helperIcon = (ImageView)findViewById(R.id.helpericon);
        TextView helperText = (TextView)findViewById(R.id.helpertext);

        if (sActivityHelper.hasScheduledActivities()) {
            mMenu.findItem(R.id.plans).setIcon(R.drawable.ic_0852_clipboard4_change);

            helperIcon.setVisibility(View.GONE);
            helperText.setVisibility(View.GONE);
        } else {
            mMenu.findItem(R.id.plans).setIcon(R.drawable.ic_0852_clipboard4_plus);

            helperIcon.setVisibility(View.VISIBLE);
            helperText.setVisibility(View.VISIBLE);
        }
    }

    public void switchViaImage(View view) {
        ImageView aiImage = (ImageView)findViewById(R.id.aiimage);
        Switch aiSwitch = (Switch)findViewById(R.id.aiswitch);

        boolean isChecked = !aiSwitch.isChecked();

        aiSwitch.setChecked(isChecked);

        if (isChecked) {
            aiImage.setImageResource(R.drawable.ai);

            AI.getOptimizedPlan(getApplicationContext());
            AI.isActivated = true;

            mDayAdapter.notifyDataSetChanged();
        } else {
            aiImage.setImageResource(R.drawable.ai_grey);

            AI.isActivated = false;

            mDayAdapter.notifyDataSetChanged();
        }
    }

    public void setTrophies(int totalPlanned, int withActual) {
        TextView trophyText3 = (TextView)findViewById(R.id.trophytext3);

        if (totalPlanned > 0) {
            trophyText3.setText("" + String.format("%.0f", Math.ceil((float) withActual / (float) totalPlanned * 100)) + "%");
        } else {
            trophyText3.setText("0%");
        }
    }
}
