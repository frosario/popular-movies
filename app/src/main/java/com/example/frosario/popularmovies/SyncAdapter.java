package com.example.frosario.popularmovies;

import android.accounts.Account;
import android.accounts.NetworkErrorException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public final String TAG = "SyncAdaptor";
    private SharedPreferences sharedPrefs;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        sharedPrefs = context.getSharedPreferences("",Context.MODE_PRIVATE);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "Performing sync");
        URL apiUrl = buildApiUrl();
        Uri resolverUri = Uri.parse("content://com.example.frosario.popularmovies.provider/");

        try {
            JSONObject moviesJson = queryApi(apiUrl);
            ContentValues contentValues = new ContentValues();

            for (Iterator<String> keys = moviesJson.keys(); keys.hasNext();) {
                String k = keys.next();
                String v = moviesJson.getString(k);
                contentValues.put(k,v);
            }

            getContext().getContentResolver().insert(resolverUri, contentValues);
            //TODO: Download movies posters and save to filesystem

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private URL buildApiUrl() {
        URL url = null;
        String api_key = sharedPrefs.getString("API_Key",null);

        //Build the url string for the API call
        String API_URL = "http://api.themoviedb.org/3/discover/movie";
        String SORT_BY_POPULARITY_PARAM = "sort_by=popularity.desc";
        String API_PARAM = "api_key=" + api_key;
        String url_string = API_URL + "?" + SORT_BY_POPULARITY_PARAM + "&" + API_PARAM;

        //Parse url string to url object
        try {
            url = new URL(url_string);
        } catch (java.net.MalformedURLException e) {
            Log.e(TAG, "Malformed URL: " + url_string);
        }

        return url;
    }

    private JSONObject queryApi(URL u) throws NetworkErrorException {
        JSONObject json = null;
        HttpURLConnection apiConnection = null;

        try {
            apiConnection = (HttpURLConnection) u.openConnection();
            apiConnection.setRequestMethod("GET");
            apiConnection.connect();

            InputStream inputStream = apiConnection.getInputStream();
            if (inputStream == null) {throw new NetworkErrorException();}

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String data = bufferedReader.toString();
            json = new JSONObject(data);

            //clean up
            inputStream.close();
            bufferedReader.close();
            apiConnection.disconnect();

        } catch (java.io.IOException | org.json.JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return json;
    }
}
