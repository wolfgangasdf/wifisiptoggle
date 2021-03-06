package com.wolle.wifisiptoggle;

import android.content.SharedPreferences;
import android.net.sip.SipManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MyWidget.class.getSimpleName();

    private class UpdateStatusTask extends AsyncTask<Void, Void, Boolean[]> {
        @Override
        protected Boolean[] doInBackground(Void... voids) {
            Boolean res1 = BackgroundService.getReceiveSipCalls(getApplicationContext());
            return new Boolean[]{res1, BackgroundService.isrunning};
        }

        @Override
        protected void onPostExecute(Boolean[] booleen) {
            CheckBox cbDoAuto = (CheckBox)findViewById(R.id.cbDoAuto);
            CheckBox cbDoRecv = (CheckBox)findViewById(R.id.cbDoRecv);
            cbDoRecv.setChecked(booleen[0]);
            cbDoAuto.setChecked(booleen[1]);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final CheckBox cbDisableAutoCompletely = (CheckBox)findViewById(R.id.cbDisableAutoCompletely);
        cbDisableAutoCompletely.setChecked(getSharedPreferences("WifiSipToggle", 0).getBoolean("disableautocompletely", false));
        cbDisableAutoCompletely.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Log.i(TAG, "onclick: checked = " + cbDisableAutoCompletely.isChecked());
               SharedPreferences sharedPreferences = getSharedPreferences("WifiSipToggle", 0);
               sharedPreferences.edit().putBoolean("disableautocompletely", cbDisableAutoCompletely.isChecked()).apply();
           }
        });

        Button btUpdateStatus = (Button) findViewById(R.id.btUpdateStatus);
        btUpdateStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UpdateStatusTask().execute();
                SharedPreferences sharedPreferences = getSharedPreferences("WifiSipToggle", 0);
                int number = sharedPreferences.getInt("MODE", 0);
                TextView tv = (TextView)findViewById(R.id.tvTest);
                tv.setText(String.valueOf(number));
                Log.i(TAG, "isSipWifiOnly: " + SipManager.isSipWifiOnly(getApplicationContext()));
                // can't change this option...
            }
        });

        TextView tvSSIDs = (TextView)findViewById(R.id.tvSSIDs);
        tvSSIDs.setText(getSharedPreferences("WifiSipToggle", 0).getString("ssids", ""));
        tvSSIDs.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                SharedPreferences sharedPreferences = getSharedPreferences("WifiSipToggle", 0);
                sharedPreferences.edit().putString("ssids", charSequence.toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        Button btAddCurrentSSID = (Button)findViewById(R.id.btAddCurrentSSID);
        btAddCurrentSSID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ssid = BackgroundService.getSSID(getApplicationContext());
                if (ssid != null) ((TextView)findViewById(R.id.tvSSIDs)).append(ssid + ",");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
