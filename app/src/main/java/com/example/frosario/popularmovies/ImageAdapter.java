package com.example.frosario.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

public class ImageAdapter extends BaseAdapter {
    private String TAG = "ImageAdapter";
    private LinkedHashMap movies = new LinkedHashMap();
    ArrayList<String> imageUrlArray = new ArrayList<>();
    private Context mContext;


    public ImageAdapter(Context context) {
        super();
        mContext = context;

        //Query MovieProvider
        String uri_string = "content://com.example.frosario.popularmovies/movies";
        Uri uri = Uri.parse(uri_string);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        Integer column_id = cursor.getColumnIndexOrThrow("id");
        Integer column_poster_path = cursor.getColumnIndexOrThrow("poster_path");
        Integer column_popularity = cursor.getColumnIndexOrThrow("popularity");
        Integer column_vote_average = cursor.getColumnIndexOrThrow("vote_average");

        //Iterate over database records
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

            //Build movies hashmap
            HashMap hashMap = new HashMap();
            Long id = cursor.getLong(column_id);
            hashMap.put("movie_id", id);
            hashMap.put("poster_path", cursor.getString(column_poster_path));
            hashMap.put("popularity", cursor.getFloat(column_popularity));
            hashMap.put("vote_average", cursor.getFloat(column_vote_average));
            movies.put(id, hashMap);

            //This will be used by Picasso later to load images
            imageUrlArray.add(cursor.getString(column_poster_path));
        }
        cursor.close();
    }

    @Override
    public int getCount() {
        return movies.size();
    }

    @Override
    public Object getItem(int position) {
        return getMovieByPosition(position);
    }

    @Override
    public long getItemId(int position) {
        HashMap movie = (HashMap) getMovieByPosition(position);
        return (Long) movie.get("movie_id");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PosterView posterView;

        if (convertView == null) {
            posterView = new PosterView(mContext);
        } else {
            posterView = (PosterView) convertView;
        }

        String poster_path = imageUrlArray.get(position);
        File file = new File(mContext.getFilesDir(),poster_path);
        if (file.exists()) {
            Uri uri = Uri.parse("file://" + file.toString());
            posterView.setImageURI(uri);
        } else {
            Log.d(TAG,file.toString() + " has not been downloaded yet");
            posterView.setImageResource(R.drawable.no_poster);
        }

        return posterView;
    }

    private Object getMovieByPosition(int p) {
        Set keySet = movies.keySet();
        Object[] keysArray = keySet.toArray();
        Long id = (Long) keysArray[p];
        return movies.get(id);
    }
}
