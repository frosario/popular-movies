package com.example.frosario.popularmovies;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.io.File;

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
        long id = db.insert("movies",null,values);
        if (id != -1) Log.e(TAG,"DB insert failed: " + values.toString());
        //TODO: Download movies posters and save to filesystem
        return uri;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        db = context.openOrCreateDatabase("movies.db", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS movies (title VARCHAR, release_date DATETIME, " +
                "poster_path VARCHAR, vote_average FLOAT, overview VARCHAR)");
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
}
