package com.example.frosario.popularmovies;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.Iterator;

public class MovieProvider extends ContentProvider {
    private SQLiteDatabase db;
    private static final String TAG = "movieProvider";

    public MovieProvider() {
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        db = openDB(context);
        String COLUMN_DEFINITIONS = "popularity VARCHAR," + "vote_average FLOAT," + "original_title VARCHAR," +
                "adult BOOLEAN," + "video VARCHAR," + "original_language VARCHAR," + "overview VARCHAR," + "title VARCHAR," +
                "backdrop_path VARCHAR," + "id LONG," + "release_date VARCHAR," + "poster_path VARCHAR," + "vote_count LONG," +
                "genre_ids VARCHAR";

        db.execSQL("CREATE TABLE IF NOT EXISTS movies (" + COLUMN_DEFINITIONS + ")");
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        try {
            JSONArray results = new JSONArray(values.getAsString("results"));
            JSONObject json;
            ContentValues cv = new ContentValues();
            db = openDB(getContext());
            db.delete("movies",null,null); //Empty out table
            db.beginTransaction();

            for (int i = 0; i < results.length(); i++) {
                json = (JSONObject) results.get(i);
                Iterator iterator = json.keys();

                while (iterator.hasNext()){
                    String key = (String) iterator.next();
                    String value = json.getString(key);
                    cv.put(key, value);
                }

                db.insertOrThrow("movies",null,cv);
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
        String[] columns = {"id", "poster_path", "popularity", "vote_average"};
        return db.query("movies", columns, null, null, null, null, null, "20");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    private UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = "com.example.frosario.popularmovies.provider";
        matcher.addURI(authority,"/movies",1);
        matcher.addURI(authority,"/movies/#",2);
        return matcher;
    }

    private SQLiteDatabase openDB(Context c) {
        return c.openOrCreateDatabase("movies.db", Context.MODE_PRIVATE, null);
    }
}
