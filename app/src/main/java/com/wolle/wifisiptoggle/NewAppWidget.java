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

public class NewAppWidget extends AppWidgetProvider {

    private void killJob(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.cancel(MainActivity.JOB_ID);
    }

    private void scheduleJob(Context context) {
        ComponentName serviceName = new ComponentName(context, MyJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(MainActivity.JOB_ID, serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
//                .setOverrideDeadline(400) // Remove comment for faster testing.
                .build();

        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Toast.makeText(context, "Started job", Toast.LENGTH_LONG).show();
            Log.i(MainActivity.TAG, "started job!");
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Get all ids
        ComponentName thisWidget = new ComponentName(context, NewAppWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("WifiSipToggle", 0);
            int number = (sharedPreferences.getInt("MODE", 0) + 1) % 3;
            sharedPreferences.edit().putInt("MODE", number).apply();

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
            Log.w("WidgetExample", String.valueOf(number));
            // Set the text
            remoteViews.setTextViewText(R.id.widgettext, String.valueOf(number));
            int[] cols = new int[]{Color.GRAY, Color.GREEN, Color.RED};
            remoteViews.setTextColor(R.id.widgettext, cols[number]);

            switch (number) {
                case 0:
                    scheduleJob(context);
                    break;
                case 1:
                    killJob(context);
                    MyJobService.setReceiveSipCalls(true);
                    Toast.makeText(context, "SIP receive calls (w)=true", Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    killJob(context);
                    MyJobService.setReceiveSipCalls(false);
                    Toast.makeText(context, "SIP receive calls (w)=false", Toast.LENGTH_LONG).show();
                    break;
            }

            // Register an onClickListener
            Intent intent = new Intent(context, NewAppWidget.class);

            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widgettext, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}
