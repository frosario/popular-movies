package com.example.frosario.popularmovies;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BackgroundRefreshTask extends AsyncTask {
    private Context context;
    private GridView gridView;
    private ProgressBar progressBar;
    private RelativeLayout relativeLayout;
    private String TAG = "BackgroundRefreshTask";

    public BackgroundRefreshTask(Context c, GridView gv, ProgressBar pb) {
        //This constructor used when refreshing MainFragment with new movies
        context = c;
        gridView = gv;
        progressBar = pb;
    }

    public BackgroundRefreshTask(Context c, RelativeLayout rl) {
        //This constructor used when refreshing with new trailers
        context = c;
        relativeLayout = rl;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        //params array should be {"movies"}, {"trailers"}, or {"trailers",movie_id_number_here}
        String table = null;
        Long movieID = null;

        if (params[0] ==  null) {
            throw new RuntimeException("Please specify table name as string in first object of this params array");
        } else {
            table = params[0].toString();
        }

        if (params.length > 1) { movieID = (Long) params[1]; }

        if (table.equals("movies")) {

            if (Utility.hasEmptyTable(context, "movies")) {
                Log.i(TAG, "Movies table empty. Refreshing data.");
                Intent intent = new Intent(context, com.example.frosario.popularmovies.SyncService.class);
                intent.putExtra("data","movies");

                String file = context.getString(R.string.shared_preferences);
                SharedPreferences sharedPrefs = context.getSharedPreferences(file, Context.MODE_PRIVATE);
                String currentSort = sharedPrefs.getString("currentSort", null);

                if (currentSort != null) {
                    intent.putExtra("sortPreference", currentSort);
                }
                context.startService(intent);
            }

            while (Utility.hasEmptyTable(context, "movies")) {
                Log.d(TAG, "Waiting on fresh movies data...");
                SystemClock.sleep(10000);
            }

        } else if (table.equals("trailers")) {

            if (Utility.hasEmptyTable(context, "trailers")) {
                Log.i(TAG, "Trailers table empty. Refreshing data.");
                Intent intent = new Intent(context, com.example.frosario.popularmovies.SyncService.class);
                intent.putExtra("data","trailers");

                //Query for all movies id's, iterate over each to get all trailers
                Uri uri = Uri.parse("content://com.example.frosario.popularmovies/movies");
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

                while (cursor.moveToNext()) {
                    int column_id = cursor.getColumnIndex("id");
                    long movie_id = cursor.getLong(column_id);
                    intent.putExtra("movie_id", movie_id);
                    context.startService(intent);
                }
            }

            while (Utility.hasEmptyTable(context, "movies")) {
                Log.d(TAG, "Waiting on fresh trailers data...");
                SystemClock.sleep(10000);
            }

        }  else {
            throw new UnsupportedOperationException("Table not supported: " + table);
        }

        if (movieID != null) {
            //Async call to sleep waiting for trailers to refresh
            int attempts = 0;
            while (Utility.movieMissingTrailer(movieID,context) && attempts < 1) {
                attempts += 1;
                Log.d(TAG, "Waiting on trailers for movie: " + String.valueOf(movieID));
                SystemClock.sleep(10000);
            }

            //After waiting check to see if there are trailers available
            String uri_string = "content://com.example.frosario.popularmovies/trailers/";
            uri_string += String.valueOf(movieID);
            Uri uri = Uri.parse(uri_string);
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            int trailerCount = cursor.getCount();
            cursor.close();

            if (trailerCount > 0){
                return movieID;
            } else {
                Log.d(TAG,"No trailers found for movie: " + String.valueOf(movieID));
            }

        }

        /*
        This will on return if there is no id included in the params from a calling method.
        Refreshing movies as an example
         */
        return table;
    }

    @Override
    protected void onPostExecute(Object object) {
        if (object.getClass().equals(String.class)) {
            String table = object.toString();
            if (table.equals("movies")) {
                connectAdapter();
                progressBar.setVisibility(View.INVISIBLE);
            }
        } else if (object.getClass().equals(Long.class)) {
            long movieID = (Long) object;
            displayTrailerLinks(movieID, relativeLayout);
        }


    }

    protected void connectAdapter() {
        gridView.setAdapter(new ImageAdapter(context));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(context, com.example.frosario.popularmovies.DetailsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("id", id);
                context.startActivity(intent);
            }

        });
        progressBar.setVisibility(View.INVISIBLE);
    }

    protected void displayTrailerLinks(long id, RelativeLayout relativeLayout){
        //Get trailer info from MovieProvider
        String uri_string = "content://com.example.frosario.popularmovies/trailers/" + String.valueOf(id);
        Uri uri = Uri.parse(uri_string);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        //Prepare fragment_details for updates with trailer links
        int belowThisId = R.id.trailers;
        int wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT;
        int below = RelativeLayout.BELOW;

        //Get rid of spinning gif
        View spinningGif = relativeLayout.findViewById(R.id.trailerSpinner);
        relativeLayout.removeView(spinningGif);

        //Update with message if no trailers found
        if (cursor.getCount() < 1) {
            TextView textView = new TextView(context);
            textView.setText("No trailers found");
            RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(wrapContent,wrapContent);
            relativeParams.addRule(below, belowThisId);
            relativeLayout.addView(textView,relativeParams);
            return;
        }

        //Create a new textview for each trailer returned
        while (cursor.moveToNext()) {
            Integer column_name = cursor.getColumnIndexOrThrow("name");
            Integer column_key = cursor.getColumnIndexOrThrow("key");
            final String name = cursor.getString(column_name);
            final String key = cursor.getString(column_key);

            //Create textview for the trailer
            int viewId = View.generateViewId();
            TextView textView = new TextView(context);
            textView.setId(viewId);
            textView.setText(name);
            textView.setTextColor(Color.BLUE);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String youtubeLink = "https://www.youtube.com/watch?v=" + key;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeLink));
                    context.startActivity(intent);
                }
            });

            //Add new textview to fragment_details
            RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(wrapContent,wrapContent);
            relativeParams.addRule(below, belowThisId);
            relativeLayout.addView(textView, relativeParams);

            //Update with last textview created
            belowThisId = textView.getId();
        }

        cursor.close();
    }
}
