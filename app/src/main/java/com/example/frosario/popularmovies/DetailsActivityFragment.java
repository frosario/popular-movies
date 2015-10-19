package com.example.frosario.popularmovies;

import android.content.Context;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DetailsActivityFragment extends Fragment {
    private String TAG = "DetailsActivityFragment";

    public DetailsActivityFragment() {
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Get all the relevant views
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        RelativeLayout trailerLayout = (RelativeLayout) view.findViewById(R.id.relativeLayoutTrailers);
        RelativeLayout reviewLayout = (RelativeLayout) view.findViewById(R.id.relativeLayoutReviews);
        ProgressBar progressbarTrailers = (ProgressBar) view.findViewById(R.id.trailerSpinner);
        ProgressBar progressbarReviews = (ProgressBar) view.findViewById(R.id.reviewsSpinner);

        //Get contextual info
        Bundle extras = getActivity().getIntent().getExtras();
        Long id = extras.getLong("id");
        Context context = this.getContext();

        //Get new data if we need it
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

        return view;
    }
}
