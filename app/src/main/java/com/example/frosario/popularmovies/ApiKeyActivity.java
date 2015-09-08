package com.example.frosario.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ApiKeyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_key);
    }

    public void save(View view) {
        EditText t = (EditText) findViewById(R.id.textApiKeyString);
        String apiKey = t.getText().toString();

        if (String.valueOf(R.id.textApiKeyString).length() > 0) {
            SharedPreferences sharedPrefs = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("API_Key", apiKey);
            editor.apply();

            Toast toast = Toast.makeText(this,"API Key saved.",Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
