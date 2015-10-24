package com.example.frosario.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utility {
    static String TAG = "Utility";

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
        if (isNetworkAvailable(context)) {
            //Empty the database
            String uri_string = "content://com.example.frosario.popularmovies/";
            Uri uri = Uri.parse(uri_string);
            String[] selection = {};
            context.getContentResolver().delete(uri, "", selection);

            BackgroundRefreshTask backgroundRefreshTask = new BackgroundRefreshTask(context, gv, pb);
            Object[] params = {"movies"};
            backgroundRefreshTask.execute(params);

        } else {
            networkNotAvailableToast(context);
        }
    }

    public static void displayFavoriteMovies(Context context, GridView gv, ProgressBar pb) {
        if (isNetworkAvailable(context)) {
            //TODO: User might go straight for this option, ensure trailers and reviews are not empty

            BackgroundRefreshTask backgroundRefreshTask = new BackgroundRefreshTask(context, gv, pb);
            backgroundRefreshTask.connectAdapter("favorites");

        } else {
            networkNotAvailableToast(context);
        }
    }

    public static void connectGridViewAdapter(Context context, GridView gv, ProgressBar pb){
        BackgroundRefreshTask backgroundRefreshTask = new BackgroundRefreshTask(context, gv, pb);
        backgroundRefreshTask.connectAdapter("popular");
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

    public static boolean movieMissingExtraData(String data, long id, Context context){
        boolean missing;
        String uri_string = "";

        switch (data) {
            case "trailers":
                uri_string = "content://com.example.frosario.popularmovies/trailers/";
                break;
            case "reviews":
                uri_string = "content://com.example.frosario.popularmovies/reviews/";
                break;
        }

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

    public static SharedPreferences getSharedPrefs(Context context) {
        String file = context.getString(R.string.shared_preferences);
        return context.getSharedPreferences(file, Context.MODE_PRIVATE);
    }

    public static List<String> loadFavorites(Context context) {
        Set<String> sp = null;
        List<String> list = new ArrayList<>();
        SharedPreferences sharedPrefs = getSharedPrefs(context);
        sp = sharedPrefs.getStringSet("favorites", new HashSet<String>());
        list = new ArrayList<>(sp);
        return list;
    }

    public static void saveFavorites(Context context, List favorites) {
        Set<String> favoriteSet = new HashSet<>(favorites);
        SharedPreferences sharedPrefs = getSharedPrefs(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putStringSet("favorites",favoriteSet);
        editor.apply();
    }

}
