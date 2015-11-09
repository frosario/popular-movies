package com.example.frosario.popularmovies;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {
    @SuppressWarnings("unused")
    private String TAG = "MainActivity";
    private SharedPreferences sharedPrefs;
    private  SharedPreferences.Editor editor;
    private static GridView gridView;
    private static ProgressBar progressBar;

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
            //noinspection ConstantConditions
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

        switch (id) {
            case R.id.action_settings:
                Utility.startApiKeyActivity(this);
                break;
            case R.id.refresh:
                String currentSort = sharedPrefs.getString("currentSort", null);
                updateUI(currentSort);
                break;

            case R.id.sort_ratings:
                updateUI("ratings");
                break;

            case R.id.sort_popularity:
                updateUI("popularity");
                break;

            case R.id.sort_favorites:
                updateUI("favorites");
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void updateUI(String sortCriteria) {
        if (!Utility.isNetworkAvailable(this)) {
            Utility.networkNotAvailableToast(this);
            return;
        }

        editor.putString("currentSort", sortCriteria);
        editor.apply();

        progressBar.setVisibility(View.VISIBLE);
        gridView.setOnItemClickListener(null);

        if (!sortCriteria.equals("favorites")) {
            Utility.refreshMovies(this, gridView, progressBar);
        } else {
            Utility.displayFavoriteMovies(this,gridView,progressBar);
        }
    }
}
