package com.wolle.wifisiptoggle;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;

public class MyJobService extends JobService {
    static final String TAG = MyJobService.class.getSimpleName();

    private class SetReceiveSipCallsTask extends AsyncTask<Boolean, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Boolean... bools) {
            setReceiveSipCalls(bools[0]);
            return bools[0];
        }
        @Override
        protected void onPostExecute(Boolean aBoolean) { // on main thread
            Toast.makeText(getApplicationContext(), "SIP receive calls (a)=" + String.valueOf(aBoolean), Toast.LENGTH_LONG).show();
        }
    }

    static void setReceiveSipCalls(Boolean b) {
        try {
            Log.i("job: ", "setReceivesipcalls " + b);
            Runtime.getRuntime().exec(new String[]{"/system/bin/su","-c","settings put system sip_receive_calls " + (b ? "1" : "0")});
            Runtime.getRuntime().exec(new String[]{"/system/bin/su","-c","am broadcast -a com.android.phone.SIP_CALL_OPTION_CHANGED"});
        } catch (IOException e) {
            Log.e("job: ", "error", e);
        }
    }

    @SuppressWarnings("unused")
    public static String execCmd(String[] cmd) throws java.io.IOException {
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static Boolean getReceiveSipCalls(Context context) {
        try { // much faster than execCmd
            return (1 == Settings.System.getInt(context.getContentResolver(), "sip_receive_calls"));
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getSSID() {
        WifiManager wifiManager = (WifiManager) getSystemService (Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo ();
        if (info.getSSID().equals("<unknown ssid>"))
            return null;
        else
            return info.getSSID();
    }

    private void setColor(final int col) {
        RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.new_app_widget);
        remoteViews.setTextColor(R.id.widgettext, col);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        ComponentName thisWidget = new ComponentName(getApplicationContext(), NewAppWidget.class);
        remoteViews.setTextColor(R.id.widgettext, col);
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

    @Override
    public boolean onStartJob(JobParameters params) { // on main thread
        Log.d(TAG, "onStartJob id=" + params.getJobId() + " ssid=" + getSSID());
        if (getSSID() != null) {
            if (!getReceiveSipCalls(getApplicationContext())) new SetReceiveSipCallsTask().execute(true);
            setColor(Color.GREEN);
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob id=" + params.getJobId() + " ssid=" + getSSID());
        if (getSSID() == null) {
            if (getReceiveSipCalls(getApplicationContext())) new SetReceiveSipCallsTask().execute(false);
            setColor(Color.RED);
        }
        return true;
    }


}