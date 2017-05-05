package com.wolle.wifisiptoggle;

import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MyWidget extends AppWidgetProvider {

    private static final String TAG = MyWidget.class.getSimpleName();
    public static final int JOB_ID = 1;

    private void killJob(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.cancel(JOB_ID);
    }

    private void scheduleJob(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        Log.i(TAG, "scheduleJob: " + scheduler.getPendingJob(JOB_ID));
        if (scheduler.getPendingJob(JOB_ID) == null) {
            ComponentName serviceName = new ComponentName(context, MyJobService.class);
            JobInfo jobInfo = new JobInfo.Builder(JOB_ID, serviceName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false)
                    .build();

            int result = scheduler.schedule(jobInfo);
            if (result == JobScheduler.RESULT_SUCCESS) {
                Toast.makeText(context, "WifiSipToggle: Started job", Toast.LENGTH_LONG).show();
                Log.i(TAG, "started job!");
            }
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.i(TAG, "onDisabled ");
        killJob(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.i(TAG, "onEnabled ");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive ");
        if (intent.hasExtra("increment")) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("WifiSipToggle", 0);
            int number = (sharedPreferences.getInt("MODE", 0) + 1) % 3;
            if (sharedPreferences.getBoolean("disableautocompletely", false) && number == 0) number = 1;
            sharedPreferences.edit().putInt("MODE", number).apply();
        }
        super.onReceive(context, intent); // will call onUpdate below
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i(TAG, "onUpdate");
        ComponentName thisWidget = new ComponentName(context, MyWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        SharedPreferences sharedPreferences = context.getSharedPreferences("WifiSipToggle", 0);
        int currentMode = sharedPreferences.getInt("MODE", 0);
        boolean oldIsReceiving = MyJobService.getReceiveSipCalls(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        Log.i(TAG, "currentMode=" + String.valueOf(currentMode));
        remoteViews.setTextViewText(R.id.widgettext, currentMode == 0 ? "A" : "M");
        boolean updatecolorhere = true;
        switch (currentMode) {
            case 0:
                scheduleJob(context);
                break;
            case 1:
                killJob(context);
                if (!oldIsReceiving) {
                    MyJobService.setReceiveSipCalls(context, true);
                    updatecolorhere = false;
                    Toast.makeText(context, "WifiSipToggle: SIPrecv on!", Toast.LENGTH_LONG).show();
                }
                break;
            case 2:
                killJob(context);
                if (oldIsReceiving) {
                    MyJobService.setReceiveSipCalls(context, false);
                    updatecolorhere = false;
                    Toast.makeText(context, "WifiSipToggle: SIPrecv off!", Toast.LENGTH_LONG).show();
                }
                break;
        }
        if (updatecolorhere) {
            remoteViews.setTextColor(R.id.widgettext, oldIsReceiving ? Color.GREEN : Color.RED);
        }

        // Register an onClickListener
        Intent intent = new Intent(context, MyWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        intent.putExtra("increment", true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widgetlayout, pendingIntent);
        appWidgetManager.updateAppWidget(allWidgetIds, remoteViews);
    }
}
