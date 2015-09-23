package com.example.frosario.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {
    Intent intent;
    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkForEmptyApiKey();

        GridView gridView = (GridView)findViewById(R.id.gridView);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.spinner);
        Object[] params = {};
        CheckEmptyDbTask checkEmptyDbTask = new CheckEmptyDbTask(this,gridView,progressBar);
        checkEmptyDbTask.execute(params);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startApiKeyActivity();
                break;
            case R.id.sync:
                startSyncService();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void startApiKeyActivity() {
        intent = new Intent(this,com.example.frosario.popularmovies.ApiKeyActivity.class);
        startActivity(intent);
    }


    private void startSyncService() {
        intent = new Intent(this,com.example.frosario.popularmovies.SyncService.class);
        startService(intent);
    }


    private void checkForEmptyApiKey() {
        String file = this.getString(R.string.shared_preferences);
        SharedPreferences sharedPrefs = this.getSharedPreferences(file, Context.MODE_PRIVATE);
        String apiKey = sharedPrefs.getString("API_Key", "");

        if (apiKey.equals("")) {
            startApiKeyActivity();
        }
    }
}
