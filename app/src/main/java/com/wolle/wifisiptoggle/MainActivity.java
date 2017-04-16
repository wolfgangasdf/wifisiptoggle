package com.wolle.wifisiptoggle;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private class UpdateStatusTask extends AsyncTask<Void, Void, Boolean[]> {
        @Override
        protected Boolean[] doInBackground(Void... voids) {
            JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo ji = scheduler.getPendingJob(MyWidget.JOB_ID);
            Boolean res1 = MyJobService.getReceiveSipCalls(getApplicationContext());
            Boolean res2 = ji != null;
            return new Boolean[]{res1, res2};
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

        Button btUpdateStatus = (Button)findViewById(R.id.btUpdateStatus);
        btUpdateStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UpdateStatusTask().execute();
                SharedPreferences sharedPreferences = getSharedPreferences("WifiSipToggle", 0);
                int number = sharedPreferences.getInt("MODE", 0);
                TextView tv = (TextView)findViewById(R.id.tvTest);
                tv.setText(String.valueOf(number));
            }
        });

        TextView tvSSIDs = (TextView)findViewById(R.id.tvSSIDs);
        Button btAddCurrentSSID = (Button)findViewById(R.id.btAddCurrentSSID);
        btAddCurrentSSID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ssid = MyJobService.getSSID(getApplicationContext());
                if (ssid != null) ((TextView)findViewById(R.id.tvSSIDs)).append(ssid + ",");
            }
        });

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
