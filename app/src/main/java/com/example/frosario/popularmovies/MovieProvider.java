package com.example.frosario.popularmovies;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MovieProvider extends ContentProvider {
    private SQLiteDatabase db;
    private static final String TAG = "MovieProvider";

    public MovieProvider() {
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        db = openDB(context);
        String MOVIE_COLUMN_DEFINITIONS = "popularity VARCHAR," + "vote_average FLOAT," + "original_title VARCHAR," +
                "adult BOOLEAN," + "video VARCHAR," + "original_language VARCHAR," + "overview VARCHAR," + "title VARCHAR," +
                "backdrop_path VARCHAR," + "id LONG," + "release_date VARCHAR," + "poster_path VARCHAR," + "vote_count LONG," +
                "genre_ids VARCHAR";

        String TRAILER_COLUMN_DEFINITIONS = "movie_id LONG, key VARCHAR, name VARCHAR, site VARCHAR, size INT, type VARCHAR, " +
                "id LONG, iso_639_1 CHAR(2)";

        String REVIEW_COLUMN_DEFINITIONS = "movie_id LONG, author VARCHAR, content VARCHAR, id LONG, url VARCHAR";

        db.execSQL("CREATE TABLE IF NOT EXISTS movies (" + MOVIE_COLUMN_DEFINITIONS + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS trailers (" + TRAILER_COLUMN_DEFINITIONS + ")");
        db.execSQL("CREATE TABLE IF NOT EXISTS reviews (" + REVIEW_COLUMN_DEFINITIONS + ")");
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //Empty out tables
        db.delete("movies",null,null);
        db.delete("trailers", null, null);
        db.delete("reviews", null, null);
        Log.d(TAG, "All values in database deleted");
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long movie_id = -1;
        JSONObject json;
        ContentValues cv = new ContentValues();
        String table = "";

        switch (uri.getPath()) {
            case "/movies": table = "movies"; break;
            case "/trailers": table = "trailers"; break;
            case "/reviews": table = "reviews"; break;
            default: throw new UnsupportedOperationException();
        }

        try {
            if (table.equals("trailers") || table.equals("reviews")) { movie_id = values.getAsLong("movie_id"); }
            JSONArray results = new JSONArray(values.getAsString("results"));
            if (table.equals("movies")) { db.delete(table, null, null); }
            db.beginTransaction();

            for (int i = 0; i < results.length(); i++) {
                if (table.equals("trailers") || table.equals("reviews")) { cv.put("movie_id", movie_id); }
                json = (JSONObject) results.get(i);
                Iterator iterator = json.keys();

                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    String value = json.getString(key);
                    cv.put(key, value);
                }

                db.insertOrThrow(table, null, cv);
            }

            db.setTransactionSuccessful();
            db.endTransaction();

        } catch (org.json.JSONException e) {
            e.getStackTrace();
        }


        return uri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (uri.getPath().equals("/movies")) {
            String[] columns = {"id", "poster_path", "popularity", "vote_average"};
            return db.query("movies", columns, null, null, null, null, null, "20");

        } else if (uri.getPath().equals("/trailers")) {
            String[] columns = {"movie_id", "key", "name"};
            return db.query("trailers", columns, null, null, null, null, null);

        } else if (uri.getPath().equals("/reviews")) {
            String[] columns = {"movie_id", "author", "url"};
            return db.query("reviews", columns, null, null, null, null, null);

        } else {
            Pattern moviePattern = Pattern.compile("/movie/\\d+");
            Matcher movieMatcher = moviePattern.matcher(uri.getPath());
            Pattern trailersPattern = Pattern.compile("/trailers/\\d+");
            Matcher trailersMatcher = trailersPattern.matcher(uri.getPath());
            Pattern reviewPattern = Pattern.compile("/reviews/\\d+");
            Matcher reviewMatcher = reviewPattern.matcher(uri.getPath());

            if (movieMatcher.find()) {
                String id = uri.getPath().split("/")[2];
                String[] columns = {"title", "poster_path", "overview", "vote_average", "release_date"};
                String selectionID = "id=" + id;
                return db.query("movies", columns, selectionID, null, null, null, null, "1");
            }

            if (trailersMatcher.find()) {
                String movie_id = uri.getPath().split("/")[2];
                String[] columns = {"key", "name"};
                String selectionID = "movie_id=" + movie_id;
                return db.query("trailers", columns, selectionID, null, null, null, null);
            }

            if (reviewMatcher.find()){
                String movie_id = uri.getPath().split("/")[2];
                String[] columns = {"author", "url"};
                String selectionID = "movie_id=" + movie_id;
                return db.query("reviews", columns, selectionID, null, null, null, null);
            }
        }

        throw new UnsupportedOperationException("Call to unsupported path: " + uri.toString());
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    private SQLiteDatabase openDB(Context c) {
        return c.openOrCreateDatabase("movies.db", Context.MODE_PRIVATE, null);
    }
}
