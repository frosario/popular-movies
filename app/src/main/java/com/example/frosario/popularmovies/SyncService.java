package com.example.frosario.popularmovies;

import android.accounts.NetworkErrorException;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class SyncService extends IntentService {
    public final String TAG = "SyncService";
    private SharedPreferences sharedPrefs;

    public SyncService() {
        super("SyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Performing sync");
        String file = this.getString(R.string.shared_preferences);
        sharedPrefs = getSharedPreferences(file,Context.MODE_PRIVATE);
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

            this.getContentResolver().insert(resolverUri, contentValues);
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

        try {
            HttpURLConnection apiConnection = (HttpURLConnection) u.openConnection();
            apiConnection.setRequestMethod("GET");
            apiConnection.connect();

            switch (apiConnection.getResponseCode()) {
                case -1:
                    throw new NetworkErrorException("API call failed");
                case 401:
                    throw new NetworkErrorException("Invalid API key: You must be granted a valid key.");
            }

            apiConnection.getContentLength();
            InputStream inputStream = apiConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line = "";
            String output_string = "";
            while ((line = bufferedReader.readLine()) != null)
                output_string += line;

            json = new JSONObject(output_string);

            //clean up
            inputStream.close();
            inputStreamReader.close();
            bufferedReader.close();
            apiConnection.disconnect();

        } catch (java.io.IOException | org.json.JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return json;
    }
}
