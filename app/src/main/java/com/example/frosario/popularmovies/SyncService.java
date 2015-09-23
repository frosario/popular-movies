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

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
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
        Uri resolverUri = Uri.parse("content://com.example.frosario.popularmovies/");

        try {
            JSONObject moviesJson = queryApi(apiUrl);
            JSONArray results = (JSONArray) moviesJson.get("results");
            downloadMoviePosters(results);
            String stringResults = moviesJson.getString("results");
            ContentValues contentValues = new ContentValues();
            contentValues.put("results",stringResults);
            this.getContentResolver().insert(resolverUri, contentValues);

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

            InputStream inputStream = apiConnection.getInputStream();
            String outputString = inputToString(inputStream);
            json = new JSONObject(outputString);

            //clean up
            inputStream.close();
            apiConnection.disconnect();

        } catch (java.io.IOException | org.json.JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return json;
    }

    private String inputToString(InputStream is) {
        String tmpString = "";

        try {
            InputStreamReader inputStreamReader = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;

            while ((line = bufferedReader.readLine()) != null)
                tmpString += line;

            inputStreamReader.close();
            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return tmpString;
    }

    private void downloadMoviePosters(JSONArray jsonArray) throws org.json.JSONException,java.io.IOException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject movie = (JSONObject) jsonArray.get(i);
            String poster_path = movie.getString("poster_path");
            String urlString = "http://image.tmdb.org/t/p/w185" + poster_path;
            File file = new File(this.getFilesDir(), poster_path);

            if (!file.exists()) {
                URL url = new URL(urlString);
                FileUtils.copyURLToFile(url, file);
            }

        }
    }
}
