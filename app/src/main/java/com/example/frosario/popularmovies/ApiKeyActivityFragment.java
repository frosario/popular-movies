package com.example.frosario.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class ApiKeyActivityFragment extends Fragment {
    private SharedPreferences sharedPrefs;

    public ApiKeyActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPrefs = Utility.getSharedPrefs(getContext());
        return inflater.inflate(R.layout.fragment_api_key, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String apiKey = sharedPrefs.getString("API_Key","");
        if (!apiKey.equals("")){
            EditText t = (EditText) view.findViewById(R.id.textApiKeyString);
            t.setText(apiKey);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        TextView textView = (TextView) getActivity().findViewById(R.id.api_help_text);
        textView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String url = "https://www.themoviedb.org/faq/api";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

    }
}
