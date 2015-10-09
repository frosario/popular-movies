package com.example.frosario.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class Utility {
    public static void networkNotAvailableToast(Context context){
        String message = "Network not available, please try again later";
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();

        if (activeNetworkInfo != null) {
            return activeNetworkInfo.isConnected();
        } else {
            return false;
        }
    }

    public static void refreshMovies(Context context, GridView gv, ProgressBar pb) {
        //Empty the database
        String uri_string = "content://com.example.frosario.popularmovies/";
        Uri uri = Uri.parse(uri_string);
        String[] selection = {};
        context.getContentResolver().delete(uri, "", selection);

        BackgroundRefreshTask backgroundRefreshTask = new BackgroundRefreshTask(context, gv, pb);

        if (isNetworkAvailable(context)) {
            Object[] params = {"movies"};
            backgroundRefreshTask.execute(params);
        } else {
            networkNotAvailableToast(context);
            if (!hasEmptyTable(context,"movies")) {
                backgroundRefreshTask.connectAdapter();
                pb.setVisibility(View.INVISIBLE);
            }
        }
    }

    public static void refreshTrailers(Context context, TextView textView, Long id) {
        BackgroundRefreshTask backgroundRefreshTask = new BackgroundRefreshTask(context, textView);
        List paramsList = new ArrayList();
        paramsList.add("trailers");
        if (id != null) { paramsList.add(id); }

        if (isNetworkAvailable(context)) {
            Object[] params = paramsList.toArray();
            backgroundRefreshTask.execute(params);
        } else {
            networkNotAvailableToast(context);
        }
    }
    
    public static void connectGridViewAdapter(Context context, GridView gv, ProgressBar pb){
        BackgroundRefreshTask backgroundRefreshTask = new BackgroundRefreshTask(context, gv, pb);
        backgroundRefreshTask.connectAdapter();
    }

    public static void checkForEmptyApiKey(Context c,SharedPreferences sp) {
        String apiKey = sp.getString("API_Key", "");
        if (apiKey.equals("")) { startApiKeyActivity(c); }
    }

    public static void startApiKeyActivity(Context context) {
        Intent intent = new Intent(context,com.example.frosario.popularmovies.ApiKeyActivity.class);
        context.startActivity(intent);
    }

    public static boolean hasEmptyTable(Context context, String table) {
        Boolean empty;
        String uri_string = "content://com.example.frosario.popularmovies/";
        uri_string += table;
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

    public static boolean movieMissingTrailer(long id, Context context){
        boolean missing;
        String uri_string = "content://com.example.frosario.popularmovies/trailers/";
        uri_string += String.valueOf(id);
        Uri uri = Uri.parse(uri_string);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        try {
            cursor.moveToFirst();
            cursor.getString(0);
            cursor.close();
            missing = false;
        } catch (android.database.CursorIndexOutOfBoundsException e) {
            missing = true;
        }

        return missing;
    }

    public static void displayTrailerLinks(long id, Context context, TextView textView){
        String uri_string = "content://com.example.frosario.popularmovies/trailers/" + String.valueOf(id);
        Uri uri = Uri.parse(uri_string);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        String text = "";

        while (cursor.moveToNext()) {
            Integer column_name = cursor.getColumnIndexOrThrow("name");
            Integer column_key = cursor.getColumnIndexOrThrow("key");
            String name = cursor.getString(column_name);
            String key = cursor.getString(column_key);
            String href = "<a href=\"" + "https://www.youtube.com/watch?v=" + key + "\">" + name + "</a>";
            text += href + "\n";
        }

        //TODO: Iterate over curser and set links to fire youtube events
        textView.setText(text);
    }

}
