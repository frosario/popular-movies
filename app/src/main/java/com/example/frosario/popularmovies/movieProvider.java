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
import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * DB columns: title, release_date, poster_path, vote_average, overview
 * Movie posters saved to filesystem
 */

public class MovieProvider extends ContentProvider {
    private SQLiteDatabase db;
    private static final String TAG = "movieProvider";

    public MovieProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //TODO: Make more efficient by keeping posters we can reuse
        int postersDeleted = 0;
        Cursor cursor = db.rawQuery("select poster_path from movies",null);

        //Delete movie posters from filesystem
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String poster_path = cursor.getString(cursor.getColumnIndex("poster_path"));
                    File image_file = new File(poster_path.substring(1));
                    if (image_file.exists()){
                        if (image_file.delete()) {
                            postersDeleted += 1;
                        } else {
                            Log.e(TAG,"Error deleting image poster");
                        }
                    }
                }while (cursor.moveToNext());
            }
        }

        //Empty table after all posters are removed
        db.delete("movies",null,null);

        return postersDeleted;
    }

    @Override
    public String getType(Uri uri) {
        UriMatcher uriMatcher = buildUriMatcher();
        int match = uriMatcher.match(uri);

        switch (match) {
            case 1:
                return "application/json";
            case 2:
                return "application/json";
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
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

        //TODO: Download movies posters and save to filesystem
        return uri;
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
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = db.query("movies", projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int rowsUpdated = db.update("movies", values, selection, selectionArgs);
        return rowsUpdated;
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
