package com.example.frosario.popularmovies;

import android.accounts.NetworkErrorException;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SyncService extends IntentService {
    public final String TAG = "SyncService";
    private SharedPreferences sharedPrefs;

    public SyncService() {
        super("SyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPrefs = Utility.getSharedPrefs(this);
        URL apiUrl = null;
        String resolverString = "content://com.example.frosario.popularmovies";
        String data = "";
        long movie_id = -1;

        Bundle extras = intent.getExtras();
        if (extras != null) {
            data = extras.getString("data");

            switch (data){
                case "movies":
                    String sortPref = extras.getString("sortPreference");
                    apiUrl = buildMovieApiUrl(sortPref);
                    resolverString += "/movies";
                    break;

                case "trailers":
                    movie_id = extras.getLong("movie_id");
                    apiUrl = buildTrailerApiURL(movie_id);
                    resolverString += "/trailers";
                    break;

                case "reviews":
                    movie_id = extras.getLong("movie_id");
                    apiUrl = buildReviewApiURL(movie_id);
                    resolverString += "/reviews";
                    break;

                default:
                    throw new UnsupportedOperationException("Unsupported data sync");
            }

        } else {
            apiUrl = buildMovieApiUrl(null);
        }

        Uri resolverUri = Uri.parse(resolverString);

        try {
            JSONObject apiJson = queryApi(apiUrl);
            Log.d(TAG, "Performed API call to: " + apiUrl.toString());

            if (data.equals("trailers") || data.equals("reviews")) { movie_id = apiJson.getLong("id"); }
            JSONArray resultsJsonArray = (JSONArray) apiJson.get("results");
            if (data.equals("movies")) { downloadMoviePosters(resultsJsonArray); }
            String resultsString = apiJson.getString("results");

            ContentValues contentValues = new ContentValues();
            if (data.equals("trailers") || data.equals("reviews")) { contentValues.put("movie_id",movie_id); }
            contentValues.put("results",resultsString);
            this.getContentResolver().insert(resolverUri, contentValues);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private URL buildMovieApiUrl(String sortBy) {
        URL url = null;
        String SORT_BY_POPULARITY_PARAM = null;
        String api_key = sharedPrefs.getString("API_Key",null);

        //Build the url string for the API call
        String API_URL = "http://api.themoviedb.org/3/discover/movie";

        try {
            if (sortBy.equals("ratings")) {
                SORT_BY_POPULARITY_PARAM = "sort_by=vote_average.desc";
            } else {
                SORT_BY_POPULARITY_PARAM = "sort_by=popularity.desc";
            }
        } catch (NullPointerException e) {
            Log.d(TAG,"sortBy was null");
            SORT_BY_POPULARITY_PARAM = "sort_by=popularity.desc";
        }

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

    private URL buildTrailerApiURL(long movie_id_number) {
        Log.d(TAG,"Building trailer api");
        URL url = null;
        String api_key = sharedPrefs.getString("API_Key",null);
        String url_string = "http://api.themoviedb.org/3/movie/" + String.valueOf(movie_id_number) +
                            "/videos?api_key=" + api_key;

        try {
            url = new URL(url_string);
        } catch (java.net.MalformedURLException e) {
            Log.e(TAG, "Malformed URL: " + url_string);
        }

        return url;
    }

    private URL buildReviewApiURL(long movie_id_number) {
        Log.d(TAG,"Building review api");
        URL url = null;
        String api_key = sharedPrefs.getString("API_Key",null);
        String url_string = "http://api.themoviedb.org/3/movie/" + String.valueOf(movie_id_number) +
                "/reviews?api_key=" + api_key;

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

            if (!poster_path.equals("null")) {
                String urlString = "http://image.tmdb.org/t/p/w185" + poster_path;
                File file = new File(this.getFilesDir(), poster_path);

                if (!file.exists()) {
                    URL url = new URL(urlString);
                    FileUtils.copyURLToFile(url, file);
                }
            }

        }
    }
}
