package com.example.frosario.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;
    static GridView gridView;
    static ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPrefs = Utility.getSharedPrefs(this);
        editor = sharedPrefs.edit();

        gridView = (GridView)findViewById(R.id.gridView);
        progressBar = (ProgressBar) findViewById(R.id.spinner);
        Utility.checkForEmptyApiKey(this, sharedPrefs);

        //Only refresh database when the app is launched
        try {
            String caller = this.getCallingActivity().getClassName();
            String thisClass = "com.example.frosario.popularmovies.MainActivity";

            if (!caller.equals(thisClass)) {
                Utility.refreshMovies(this, gridView, progressBar);
            } else {
                Utility.connectGridViewAdapter(this, gridView, progressBar);

            }

        } catch (RuntimeException e) {
            Utility.refreshMovies(this, gridView, progressBar);
        }

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
                Utility.startApiKeyActivity(this);
                break;
            case R.id.sync:
                updateUI();
                break;
            case R.id.sort:
                //Toggle to sort by other metric
                if (sort.equals("popularity")) {
                    editor.putString("currentSort", "ratings");
                    editor.apply();
                    updateUI();
                } else {
                    editor.putString("currentSort", "popularity");
                    editor.apply();
                    updateUI();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateUI() {
        if (!Utility.isNetworkAvailable(this)) {
            Utility.networkNotAvailableToast(this);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        gridView.setOnItemClickListener(null);
        Utility.refreshMovies(this, gridView, progressBar);
    }
}
