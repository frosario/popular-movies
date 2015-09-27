package com.example.frosario.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

public class CheckEmptyDbTask extends AsyncTask {
    private Context context;
    private GridView gridView;
    private ProgressBar progressBar;
    private String TAG = "CheckEmptyDbTask";

    public CheckEmptyDbTask(Context c, GridView gv, ProgressBar pb) {
        context = c;
        gridView = gv;
        progressBar = pb;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        if (hasEmptyDB()) {
            Log.i(TAG, "Database empty. Performing initial sync");
            Intent intent = new Intent(context,com.example.frosario.popularmovies.SyncService.class);

            String file = context.getString(R.string.shared_preferences);
            SharedPreferences sharedPrefs = context.getSharedPreferences(file, Context.MODE_PRIVATE);
            String currentSort = sharedPrefs.getString("currentSort", null);

            if (currentSort != null) { intent.putExtra("sortPreference",currentSort); }
            context.startService(intent);
        }

        while (hasEmptyDB()) {
            Log.d(TAG, "Sleeping...");
            SystemClock.sleep(10000);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        connectAdapter(gridView);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private boolean hasEmptyDB() {
        Boolean empty;
        String uri_string = "content://com.example.frosario.popularmovies/";
        Uri uri = Uri.parse(uri_string);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        try {
            cursor.moveToFirst();
            cursor.getString(0);
            cursor.close();
            empty = false;
        } catch (android.database.CursorIndexOutOfBoundsException e) {
            empty = true;
        }

        return empty;
    }

    private void connectAdapter(GridView posterGrid) {
        posterGrid.setAdapter(new ImageAdapter(context));
        posterGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(context, com.example.frosario.popularmovies.DetailsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("id", id);
                context.startActivity(intent);
            }

        });
    }
}
