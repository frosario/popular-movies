package com.example.frosario.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"CanBeFinal", "WeakerAccess"})
public class BackgroundRefreshTask extends AsyncTask {
    private Context context;
    private GridView gridView;
    private ProgressBar progressBar;
    private RelativeLayout trailersLayout;
    private RelativeLayout reviewsLayout;
    private ProgressBar spinnerTrailers;
    private ProgressBar spinnerReviews;
    private String TAG = "BackgroundRefreshTask";

    public BackgroundRefreshTask(Context c, GridView gv, ProgressBar pb) {
        //This constructor used when refreshing MainFragment with new movies
        context = c;
        gridView = gv;
        progressBar = pb;
    }

    public BackgroundRefreshTask(Context c, RelativeLayout trailers, RelativeLayout reviews, ProgressBar spinTrailers, ProgressBar spinReviews) {
        //This constructor used when refreshing with new trailers and reviews
        context = c;
        trailersLayout = trailers;
        reviewsLayout = reviews;
        spinnerTrailers = spinTrailers;
        spinnerReviews = spinReviews;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        /*
        params array should one of:
        {"movies"}
        {"trailers"}
        {"trailers",movie_id_number_here}
        {"reviews"}
        {"reviews",movie_id_number_here}
        {"favorites"}
        */

        String table;
        Long movieID = null;

        if (params[0] ==  null) {
            throw new RuntimeException("Please specify table name as string in first object of this params array");
        } else {
            table = params[0].toString();
        }

        if (params.length > 1) { movieID = (Long) params[1]; }

        //noinspection IfCanBeSwitch
        if (table.equals("movies")) {

            if (Utility.hasEmptyTable(context, "movies")) {
                Log.d(TAG, "Movies table empty. Refreshing data.");
                Intent intent = new Intent(context, com.example.frosario.popularmovies.SyncService.class);
                intent.putExtra("data","movies");

                SharedPreferences sharedPrefs = Utility.getSharedPrefs(context);
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

        } else if (table.equals("trailers") || table.equals("reviews")) {

            if (Utility.hasEmptyTable(context, table)) {
                Log.d(TAG, table + " table empty. Refreshing data.");
                Intent intent = new Intent(context, com.example.frosario.popularmovies.SyncService.class);
                intent.putExtra("data",table);

                //Query for all movies id's, iterate over each to get all trailers or reviews
                Uri uri = Uri.parse("content://com.example.frosario.popularmovies/movies");
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

                while (cursor.moveToNext()) {
                    int column_id = cursor.getColumnIndex("id");
                    long movie_id = cursor.getLong(column_id);
                    intent.putExtra("movie_id", movie_id);
                    context.startService(intent);
                }

                cursor.close();
            }

            int attempts = 0;
            while (Utility.hasEmptyTable(context, table) && attempts < 2) {
                Log.d(TAG, "Waiting on fresh " + table + " data...");
                SystemClock.sleep(10000);
                attempts += 1;
            }

        } else {
            throw new UnsupportedOperationException("Table not supported: " + table);
        }

        try {
            if (movieID != null) {
                if (table.equals("trailers")) {
                    //Async call to sleep waiting for trailers to refresh
                    int attempts = 0;
                    while (Utility.movieMissingExtraData("trailers", movieID, context) && attempts < 1) {
                        attempts += 1;
                        Log.d(TAG, "Waiting on trailers for movie: " + String.valueOf(movieID));
                        SystemClock.sleep(10000);
                    }

                    JSONObject json = new JSONObject().put("table", table);
                    json.put("movieID", movieID);
                    return json;

                } else if (table.equals("reviews")) {
                    //Async call to sleep waiting for reviews to refresh
                    int attempts = 0;
                    while (Utility.movieMissingExtraData("reviews", movieID, context) && attempts < 1) {
                        attempts += 1;
                        Log.d(TAG, "Waiting on reviews for movie: " + String.valueOf(movieID));
                        SystemClock.sleep(10000);
                    }

                    JSONObject json = new JSONObject().put("table", table);
                    json.put("movieID", movieID);
                    return json;
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        /*
        This will only return if there is no id included in the params from a calling method.
        Refreshing movies during onCreate() as an example
         */
        return table;
    }

    @Override
    protected void onPostExecute(Object object) {
        String table = "";
        long movieID = -1;

        if (object.getClass().equals(String.class)) {
            table = object.toString();
            if (table.equals("movies")) {
                connectAdapter("popular");
                progressBar.setVisibility(View.INVISIBLE);
            }

        } else if (object.getClass().equals(JSONObject.class)) {
            try {
                JSONObject json = (JSONObject) object;
                movieID = json.getLong("movieID");
                table = json.getString("table");
            } catch (JSONException e) {
                Log.d(TAG,e.getMessage());
            }

            switch (table) {
                case "trailers":
                    displayTrailerLinks(movieID);
                    break;

                case "reviews":
                    displayReviewLinks(movieID);
                    break;
            }
        }
    }

    protected void connectAdapter(String purpose) {
        switch (purpose) {
            case "popular":
                gridView.setAdapter(new MovieAdapter(context));
                break;

            case "favorites":
                gridView.setAdapter(new MovieAdapter(context, "favorites"));
                break;

            default:
                throw new  UnsupportedOperationException("Purpose not supported");
        }

        try {
            boolean isTablet = context.getResources().getBoolean(R.bool.isTablet);
            if (isTablet){
                final AppCompatActivity activity = (AppCompatActivity) context;
                final FragmentManager fragmentManager = activity.getSupportFragmentManager();

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        DetailsActivityFragment details = (DetailsActivityFragment) fragmentManager.findFragmentById(R.id.fragmentDetails);
                        View view = activity.findViewById(R.id.fragmentDetails);
                        details.refresh(id);
                        view.setVisibility(View.VISIBLE);
                    }

                });
            }

        } catch (RuntimeException e) {
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent intent = new Intent(context, com.example.frosario.popularmovies.DetailsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("id", id);
                    context.startActivity(intent);
                }

            });
        }

        progressBar.setVisibility(View.INVISIBLE);
    }

    protected void displayTrailerLinks(long id){
        //Get trailer info from MovieProvider
        String uri_string = "content://com.example.frosario.popularmovies/trailers/" + String.valueOf(id);
        Uri uri = Uri.parse(uri_string);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        //Prepare fragment_details for updates with trailer links
        int belowThisId = R.id.textviewTrailers;
        int wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT;
        int below = RelativeLayout.BELOW;

        //Hide spinning Gif
        spinnerTrailers.setVisibility(View.INVISIBLE);


        //Clear out trailers from previous movie selection
        trailersLayout.removeAllViews();


        //Update with message if no trailers found
        if (cursor.getCount() < 1) {
            TextView textView = new TextView(context);
            textView.setText("No trailers found");
            RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(wrapContent,wrapContent);
            relativeParams.addRule(below, belowThisId);
            trailersLayout.addView(textView, relativeParams);
            cursor.close();
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
            trailersLayout.addView(textView, relativeParams);

            //Update with last textview created
            belowThisId = textView.getId();
        }

        cursor.close();
    }

    protected void displayReviewLinks(long id) {
        //Get review info from MovieProvider
        String uri_string = "content://com.example.frosario.popularmovies/reviews/" + String.valueOf(id);
        Uri uri = Uri.parse(uri_string);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        //Prepare fragment_details for updates with review links
        int belowThisId = R.id.textviewReviews;
        int wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT;
        int below = RelativeLayout.BELOW;

        //Get rid of spinning gif
        spinnerReviews.setVisibility(View.INVISIBLE);


        //Clear out reviews from previous movie selection
        reviewsLayout.removeAllViews();

        
        //Update with message if no reviews found
        if (cursor.getCount() < 1) {
            TextView textView = new TextView(context);
            textView.setText("No reviews found");
            RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(wrapContent,wrapContent);
            relativeParams.addRule(below, belowThisId);
            reviewsLayout.addView(textView, relativeParams);
            return;
        }


        //Create a new textview for each review returned
        while (cursor.moveToNext()) {
            Integer column_author = cursor.getColumnIndexOrThrow("author");
            Integer column_url = cursor.getColumnIndexOrThrow("url");
            final String author = cursor.getString(column_author);
            final String url = cursor.getString(column_url);

            //Create textview for the review
            int viewId = View.generateViewId();
            TextView textView = new TextView(context);
            textView.setId(viewId);
            textView.setText(author);
            textView.setTextColor(Color.BLUE);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(intent);
                }
            });

            //Add new textview to fragment_details
            RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(wrapContent, wrapContent);
            relativeParams.addRule(below, belowThisId);
            reviewsLayout.addView(textView, relativeParams);

            //Update with last textview created
            belowThisId = textView.getId();
        }

        cursor.close();
    }
}
