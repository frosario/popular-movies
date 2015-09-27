package com.example.frosario.popularmovies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {
    Intent intent;
    String TAG = "MainActivity";
    SharedPreferences sharedPrefs;
    BroadcastReceiver receiver;
    SharedPreferences.Editor editor;
    static GridView gridView;
    static ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String file = this.getString(R.string.shared_preferences);
        sharedPrefs = this.getSharedPreferences(file, Context.MODE_PRIVATE);
        editor = sharedPrefs.edit();

        gridView = (GridView)findViewById(R.id.gridView);
        progressBar = (ProgressBar) findViewById(R.id.spinner);
        checkForEmptyApiKey();
        checkForEmptyDB();


        //Set default sorting preference if missing
        String currentSort = sharedPrefs.getString("currentSort", "");
        if (currentSort.equals("")) {
            editor.putString("currentSort", "popularity");
            editor.apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String sort = sharedPrefs.getString("currentSort", null);

        switch (id) {
            case R.id.action_settings:
                startApiKeyActivity();
                break;
            case R.id.sync:
                updateUI(sort);
                break;
            case R.id.sort:
                //Toggle to sort by other metric
                if (sort.equals("popularity")) {
                    editor.putString("currentSort", "ratings");
                    editor.apply();
                    updateUI("ratings");
                } else
                    editor.putString("currentSort", "popularity");
                    editor.apply();
                    updateUI("popularity");

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startApiKeyActivity() {
        intent = new Intent(this,com.example.frosario.popularmovies.ApiKeyActivity.class);
        startActivity(intent);
    }

    private void startSyncService(String sortPreference) {
        intent = new Intent(this,com.example.frosario.popularmovies.SyncService.class);
        if (sortPreference != null) { intent.putExtra("sortPreference", sortPreference); }
        startService(intent);
    }

    private void checkForEmptyApiKey() {
        String apiKey = sharedPrefs.getString("API_Key", "");
        if (apiKey.equals("")) { startApiKeyActivity(); }
    }

    private void checkForEmptyDB(){
        Object[] params = {};
        CheckEmptyDbTask checkEmptyDbTask = new CheckEmptyDbTask(this,gridView,progressBar);
        checkEmptyDbTask.execute(params);
    }

    private void updateUI(String sortString) {
        //Empty DB
        String uri_string = "content://com.example.frosario.popularmovies/";
        Uri uri = Uri.parse(uri_string);
        String[] selection = {};
        this.getContentResolver().delete(uri, "", selection);

        //Set spinner and update db
        progressBar.setVisibility(View.VISIBLE);
        checkForEmptyDB();
    }
}
