package com.example.frosario.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class Utility {
    public static void networkNotAvailableToast(Context context){
        String message = "Network not available, please try again later";
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();

        if (activeNetworkInfo != null) {
            return activeNetworkInfo.isConnected();
        } else {
            return false;
        }
    }

    public static void checkForEmptyDB(Context context, GridView gv, ProgressBar pb) {
        CheckEmptyDbTask checkEmptyDbTask = new CheckEmptyDbTask(context, gv, pb);

        if (isNetworkAvailable(context)) {
            Object[] params = {};
            checkEmptyDbTask.execute(params);
        } else {
            networkNotAvailableToast(context);
            if (!checkEmptyDbTask.hasEmptyDB()) {
                checkEmptyDbTask.connectAdapter(gv);
                pb.setVisibility(View.INVISIBLE);
            }
        }
    }

    public static void checkForEmptyApiKey(Context c,SharedPreferences sp) {
        String apiKey = sp.getString("API_Key", "");
        if (apiKey.equals("")) { startApiKeyActivity(c); }
    }

    public static void startApiKeyActivity(Context context) {
        Intent intent = new Intent(context,com.example.frosario.popularmovies.ApiKeyActivity.class);
        context.startActivity(intent);
    }
}
