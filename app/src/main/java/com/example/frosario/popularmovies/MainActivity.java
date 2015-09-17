package com.example.frosario.popularmovies;

import android.accounts.Account;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                intent = new Intent(this,com.example.frosario.popularmovies.ApiKeyActivity.class);
                startActivity(intent);
                break;
            case R.id.sync:
                intent = new Intent(this,com.example.frosario.popularmovies.SyncService.class);
                startService(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
