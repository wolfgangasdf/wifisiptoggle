package com.wolle.wifisiptoggle;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
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
        protected void onPostExecute(Boolean aBoolean) {
            Toast.makeText(getApplicationContext(), "SIP receive calls (a)=" + String.valueOf(aBoolean), Toast.LENGTH_LONG).show();
        }
    }

    static void setReceiveSipCalls(Boolean b) {
        try {
            Runtime.getRuntime().exec(new String[]{"/system/bin/su","-c","settings put system sip_receive_calls " + (b ? "1" : "0")});
            Runtime.getRuntime().exec(new String[]{"/system/bin/su","-c","am broadcast -a com.android.phone.SIP_CALL_OPTION_CHANGED"});
        } catch (IOException e) {
            Log.e("job: ", "error", e);
        }
    }

    public static String execCmd(String[] cmd) throws java.io.IOException {
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static Boolean getReceiveSipCalls() {
        try {
            String res = execCmd(new String[]{"/system/bin/su", "-c", "settings get system sip_receive_calls"});
            Log.i("job: ", "result=" + res);
            if (res.startsWith("1")) return true;
        } catch (IOException e) {
            Log.e("job: ", "error", e);
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
    @Override
    public boolean onStartJob(JobParameters params) {
        // Note: this is preformed on the main thread.
        Log.d(TAG, "onStartJob id=" + params.getJobId() + " ssid=" + getSSID());
        if (getSSID() != null) new SetReceiveSipCallsTask().execute(true);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob id=" + params.getJobId() + " ssid=" + getSSID());
        if (getSSID() == null) new SetReceiveSipCallsTask().execute(false);
        return true;
    }


}