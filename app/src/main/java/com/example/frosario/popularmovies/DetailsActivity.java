package com.example.frosario.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;

public class DetailsActivity extends AppCompatActivity {
    private String TAG = "DetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String outputTitle = "";
        String outputPlot = "";
        Long id = getIntent().getExtras().getLong("id");
        String uri_string = "content://com.example.frosario.popularmovies/movie/" + id.toString();
        Uri uri = Uri.parse(uri_string);

        //Query for movie details
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        Integer column_title = cursor.getColumnIndexOrThrow("title");
        Integer column_poster_path = cursor.getColumnIndexOrThrow("poster_path");
        Integer column_overview = cursor.getColumnIndexOrThrow("overview");
        Integer column_vote_average = cursor.getColumnIndexOrThrow("vote_average");
        Integer column_release_date = cursor.getColumnIndexOrThrow("release_date");

        //Construct details text
        cursor.moveToFirst();
        outputTitle += "Title:\n" + cursor.getString(column_title) + "\n\n";
        outputTitle += "Release Date:\n" + cursor.getString(column_release_date) + "\n\n";
        outputTitle += "User Rating:\n" + cursor.getString(column_vote_average) + "\n\n";
        outputPlot += "Plot:\n" + cursor.getString(column_overview) + "\n";


        //Update the text
        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(outputTitle);
        TextView plotView = (TextView) findViewById(R.id.plot);
        plotView.setText(outputPlot);

        //Set poster thumbnail
        ImageView imageView = (ImageView) findViewById(R.id.thumbnail);
        File file = new File(getFilesDir(), cursor.getString(column_poster_path));

        if (file.exists()) {
            Uri fileUri = Uri.parse("file://" + file.toString());
            imageView.setImageURI(fileUri);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            Log.d(TAG,"Poster image not found: " + file.toString());
        }

        cursor.close();
    }
}
