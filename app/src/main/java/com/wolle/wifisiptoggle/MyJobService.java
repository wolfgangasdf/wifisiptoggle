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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyJobService extends JobService {
    private static final String TAG = MyJobService.class.getSimpleName();

    private class SetReceiveSipCallsTask extends AsyncTask<Boolean, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Boolean... bools) {
            setReceiveSipCalls(getApplicationContext(), bools[0]);
            return bools[0];
        }
        @Override
        protected void onPostExecute(Boolean aBoolean) { // on main thread
            Toast.makeText(getApplicationContext(), "WifiSipToggle: SIPrecv (a)=" + String.valueOf(aBoolean), Toast.LENGTH_LONG).show();
        }
    }

    static void setReceiveSipCalls(Context context, Boolean b) {
        try {
            Log.i("job: ", "setReceivesipcalls " + b);
            Runtime.getRuntime().exec(new String[]{"/system/bin/su","-c","settings put system sip_receive_calls " + (b ? "1" : "0")});
            Runtime.getRuntime().exec(new String[]{"/system/bin/su","-c","am broadcast -a com.android.phone.SIP_CALL_OPTION_CHANGED"});

            int col = b ? Color.GREEN : Color.RED;
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
            remoteViews.setTextColor(R.id.widgettext, col);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, MyWidget.class);
            remoteViews.setTextColor(R.id.widgettext, col);
            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
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

    public static String getSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService (Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info.getSSID().equals("<unknown ssid>"))
            return null;
        else
            return info.getSSID().replaceAll("\"", "");
    }

    private void autoUpdateSipRecv(Context context) {
        String ssid = getSSID(context);
        boolean dosip = false;
        if (ssid != null) {
            List<String> ssids = new ArrayList<>(Arrays.asList(getSharedPreferences("WifiSipToggle", 0).getString("ssids", "").split(",")));
            if (ssids.size() == 1 && "".equals(ssids.get(0))) ssids.clear();
            if (ssids.contains(ssid) || ssids.isEmpty()) dosip = true;
        }
        Log.i(TAG, "updatesip: ssid=" + ssid + " dosip=" + dosip);
        if (dosip) {
            if (!getReceiveSipCalls(getApplicationContext())) new SetReceiveSipCallsTask().execute(true);
        } else {
            if (getReceiveSipCalls(getApplicationContext())) new SetReceiveSipCallsTask().execute(false);
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        autoUpdateSipRecv(this);
    }

    @Override
    public boolean onStartJob(JobParameters params) { // on main thread
        Log.i(TAG, "onStartJob id=" + params.getJobId());
        autoUpdateSipRecv(this);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob id=" + params.getJobId());
        autoUpdateSipRecv(this);
        return true;
    }


}