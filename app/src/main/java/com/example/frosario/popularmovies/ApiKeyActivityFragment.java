package com.example.frosario.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String file = this.getString(R.string.shared_preferences);
        sharedPrefs = getActivity().getSharedPreferences(file,Context.MODE_PRIVATE);
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
}
