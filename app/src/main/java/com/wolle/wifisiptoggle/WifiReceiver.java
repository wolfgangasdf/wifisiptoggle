package com.wolle.wifisiptoggle;

//w https://gist.github.com/mjohnsullivan/1fec89187b1274dc256e

//
//  Copyright 2015  Google Inc. All Rights Reserved.
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.


        import android.app.Notification;
        import android.app.NotificationManager;
        import android.app.Service;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.net.wifi.WifiInfo;
        import android.net.wifi.WifiManager;
        import android.os.Handler;
        import android.os.IBinder;
        import android.support.v4.app.NotificationCompat;
        import android.util.Log;
        import android.widget.Toast;

/**
 * Receives wifi changes and creates a notification when wifi connects to a network,
 * displaying the SSID and MAC address.
 *
 * Put the following in your manifest
 *
 * <receiver android:name=".WifiReceiver" android:exported="false" >
 *   <intent-filter>
 *     <action android:name="android.net.wifi.WIFI_STATE_CHANGED" />
 *   </intent-filter>
 * </receiver>
 * <service android:name=".WifiReceiver$WifiActiveService" android:exported="false" />
 *
 * To activate logging use: adb shell setprop log.tag.WifiReceiver VERBOSE
 */
public class WifiReceiver extends BroadcastReceiver {

    private final static String TAG = WifiReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.e("WifiReceiver", "XXXX");
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            Log.e(TAG, "Have Wifi Connection");
            Toast.makeText(context, "have wifi"     , Toast.LENGTH_LONG).show();
        } else {
            Log.e(TAG, "Don't have Wifi Connection");
            Toast.makeText(context, "have no wifi"     , Toast.LENGTH_LONG).show();
        }

//        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
//        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())
//                && WifiManager.WIFI_STATE_ENABLED == wifiState) {
//            if (Log.isLoggable(TAG, Log.VERBOSE)) {
//                Log.v(TAG, "Wifi is now enabled");
//            }
//            context.startService(new Intent(context, WifiActiveService.class));
//        }
    }


}