package com.wolle.wifisiptoggle;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static int JOB_ID = 1;
    public static String TAG = "WifiSipToggle";

    private class UpdateStatusTask extends AsyncTask<Void, Void, Boolean[]> {
        @Override
        protected Boolean[] doInBackground(Void... voids) {
            JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo ji = scheduler.getPendingJob(JOB_ID);
            Boolean res1 = MyJobService.getReceiveSipCalls();
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

    private class SetReceiveSipCallsTask extends AsyncTask<Boolean, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Boolean... bools) {
            MyJobService.setReceiveSipCalls(bools[0]);
            return bools[0];
        }
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Toast.makeText(getApplicationContext(), "SIP receive calls=" + String.valueOf(aBoolean), Toast.LENGTH_LONG).show();
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
            }
        });

        Button rbAuto = (RadioButton)findViewById(R.id.rbAuto);
        rbAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "rbAuto click!");
                scheduleJob();
            }
        });

        Button rbOn = (RadioButton)findViewById(R.id.rbOn);
        rbOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "rbOn click!");
                killJob();
                new SetReceiveSipCallsTask().execute(true);
            }
        });

        Button rbOff = (RadioButton)findViewById(R.id.rbOff);
        rbOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "rbOff click!");
                killJob();
                new SetReceiveSipCallsTask().execute(false);
            }
        });

    }

    private void killJob() {
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.cancel(JOB_ID);
    }

    private void scheduleJob() {
        ComponentName serviceName = new ComponentName(this, MyJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
//                .setOverrideDeadline(400) // Remove comment for faster testing.
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = scheduler.schedule(jobInfo);
        if (result == JobScheduler.RESULT_SUCCESS) {
            Toast.makeText(this, "Started job", Toast.LENGTH_LONG).show();
            Log.i(TAG, "started job!");
        }
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
