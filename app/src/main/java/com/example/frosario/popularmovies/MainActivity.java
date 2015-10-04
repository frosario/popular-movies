package com.example.frosario.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
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
        String file = this.getString(R.string.shared_preferences);
        sharedPrefs = this.getSharedPreferences(file, Context.MODE_PRIVATE);
        editor = sharedPrefs.edit();

        gridView = (GridView)findViewById(R.id.gridView);
        progressBar = (ProgressBar) findViewById(R.id.spinner);
        Utility.checkForEmptyApiKey(this,sharedPrefs);
        Utility.checkForEmptyDB(this,gridView,progressBar);


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
                } else
                    editor.putString("currentSort", "popularity");
                    editor.apply();
                    updateUI();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateUI() {
        if (!Utility.isNetworkAvailable(this)) {
            Utility.networkNotAvailableToast(this);
            return;
        }

        //Empty DB
        String uri_string = "content://com.example.frosario.popularmovies/";
        Uri uri = Uri.parse(uri_string);
        String[] selection = {};
        this.getContentResolver().delete(uri, "", selection);

        //Set spinner and update db
        progressBar.setVisibility(View.VISIBLE);
        Utility.checkForEmptyDB(this, gridView, progressBar);
    }
}
