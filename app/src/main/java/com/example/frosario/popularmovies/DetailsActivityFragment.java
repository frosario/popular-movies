package com.example.frosario.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DetailsActivityFragment extends Fragment {
    private String TAG = "DetailsActivityFragment";
    private Long id;
    private Context context;
    private String outputTitle = "";
    private String outputPlot = "";
    private File imageFile;
    private View view;
    private RelativeLayout trailerLayout;
    private RelativeLayout reviewLayout;
    private ProgressBar progressbarTrailers;
    private ProgressBar progressbarReviews;
    private Button favoritesButton;
    private List<String> favorites;
    private Boolean isFavorite;

    public DetailsActivityFragment() { setHasOptionsMenu(false); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Get all the relevant views
        view = inflater.inflate(R.layout.fragment_details, container, false);
        trailerLayout = (RelativeLayout) view.findViewById(R.id.relativeLayoutTrailers);
        reviewLayout = (RelativeLayout) view.findViewById(R.id.relativeLayoutReviews);
        progressbarTrailers = (ProgressBar) view.findViewById(R.id.trailerSpinner);
        progressbarReviews = (ProgressBar) view.findViewById(R.id.reviewsSpinner);
        favoritesButton = (Button) view.findViewById(R.id.buttonFavorites);

        //Get contextual info
        Bundle extras = getActivity().getIntent().getExtras();
        id = extras.getLong("id");
        context = this.getContext();

        updateDataIfNecessary();
        getMovieDetails(id);
        setMovieThumbnail();
        updateViews();
        setFavoritesButtonListener();

        return view;
    }

    private void updateDataIfNecessary(){
        if (Utility.hasEmptyTable(context, "trailers") || Utility.hasEmptyTable(context, "reviews")) {
            if (Utility.isNetworkAvailable(context)) {
                //Refresh trailers first
                BackgroundRefreshTask trailersRefreshTask = new BackgroundRefreshTask(context, trailerLayout, reviewLayout, progressbarTrailers, progressbarReviews);
                List trailersParamsList = new ArrayList();
                trailersParamsList.add("trailers");
                if (id != null) { trailersParamsList.add(id); }
                Object[] trailerParams = trailersParamsList.toArray();
                trailersRefreshTask.execute(trailerParams);

                //Refresh reviews next
                BackgroundRefreshTask reviewsRefreshTask = new BackgroundRefreshTask(context, trailerLayout, reviewLayout, progressbarTrailers, progressbarReviews);
                List reviewsParamsList = new ArrayList();
                reviewsParamsList.add("reviews");
                if (id != null) { reviewsParamsList.add(id); }
                Object[] reviewsParams = reviewsParamsList.toArray();
                reviewsRefreshTask.execute(reviewsParams);

            } else {
                Utility.networkNotAvailableToast(context);
            }

        } else {
            BackgroundRefreshTask backgroundRefreshTask = new BackgroundRefreshTask(context, trailerLayout, reviewLayout, progressbarTrailers, progressbarReviews);
            backgroundRefreshTask.displayTrailerLinks(id);
            backgroundRefreshTask.displayReviewLinks(id);
        }
    }

    private void getMovieDetails(long movieID) {
        //Setup URI for MovieProvider
        String uri_string = "content://com.example.frosario.popularmovies/movie/" + String.valueOf(movieID);
        Uri uri = Uri.parse(uri_string);

        //Query for movie details
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
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

        //Find poster thumbnail
        imageFile = new File(context.getFilesDir(), cursor.getString(column_poster_path));

        cursor.close();
        favorites = Utility.loadFavorites(context);
        String strID = String.valueOf(id);
        isFavorite = favorites.contains(strID);
    }

    private void setMovieThumbnail() {
        ImageView imageView = (ImageView) view.findViewById(R.id.thumbnail);

        if (imageFile.exists()) {
            Uri fileUri = Uri.parse("file://" + imageFile.toString());
            imageView.setImageURI(fileUri);
        } else {
            Log.d(TAG,"Poster image not found: " + imageFile.toString());
            imageView.setImageResource(R.drawable.no_poster);
        }

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    private void updateViews(){
        TextView titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(outputTitle);
        TextView plotView = (TextView) view.findViewById(R.id.plot);
        plotView.setText(outputPlot);
        if (isFavorite) { favoritesButton.setText(R.string.remove_from_favorites); }
    }

    private void setFavoritesButtonListener(){
        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stringID = String.valueOf(id);

                if (isFavorite) {
                    favorites.remove(stringID);
                    favoritesButton.setText(R.string.add_to_favorites);
                    isFavorite = false;

                } else {
                    favorites.add(stringID);
                    favoritesButton.setText(R.string.remove_from_favorites);
                    isFavorite = true;
                }

                Utility.saveFavorites(context,favorites);
            }
        });
    }
}
