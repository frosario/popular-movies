package com.example.frosario.popularmovies;

import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailsActivityFragment extends Fragment {

    public DetailsActivityFragment() {
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        TextView textView = (TextView) view.findViewById(R.id.trailers);
        Bundle extras = getActivity().getIntent().getExtras();
        long id = extras.getLong("id");
        String TAG = "DetailsActivityFragment";

        if (Utility.hasEmptyTable(this.getContext(), "trailers")) {
            Utility.refreshTrailers(this.getContext(), textView, id);
        } else {
            Utility.displayTrailerLinks(id,this.getContext(),textView);
        }
        
        return view;
    }
}
