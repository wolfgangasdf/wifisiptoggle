package com.wolle.wifisiptoggle;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BackgroundService extends Service {
    private String TAG = "BGS";
    private BroadcastReceiver bcReceiver;
    public static boolean isrunning = false;
    private final static int NOTIFICATIONID = 101;
    private static Notification.Builder notificationbuilder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "BGS: onbind: " + intent);
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "BGS: oncreate!");
        super.onCreate();

        notificationbuilder = new Notification.Builder(this)
                .setContentTitle("Automatic SIP receive call switching based on wifi")
                .setContentText("This notification has to be present")
                .setSmallIcon(R.mipmap.ic_stat_wst)
//                    .setContentIntent(pendingIntent)
                .setTicker("ticker1");

        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        bcReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "BGS: onrecv!!!!!!" + intent);
                autoUpdateSipRecv(context);
                // Toast.makeText(context, "Broadcast Successful!!!", Toast.LENGTH_LONG).show();
            }
        };

        this.registerReceiver(bcReceiver, theFilter);

        startForeground(NOTIFICATIONID, notificationbuilder.build());
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "BGS: ondestroy!");
        super.onDestroy();
        // Do not forget to unregister the receiver!!!
        this.unregisterReceiver(bcReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onstart: " + flags + " " + startId);
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(TAG, "ontaskrem");
        super.onTaskRemoved(rootIntent);
    }

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

            final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationbuilder.setContentText("Receive SIP calls: " + b);
            manager.notify(NOTIFICATIONID, notificationbuilder.build());

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





}
