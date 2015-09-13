package com.example.frosario.popularmovies;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SyncService extends Service {
    private String TAG = "SyncService";
    private static SyncAdapter syncAdapter = null;
    private static final Object syncAdapterLock = new Object();

    public SyncService() {
    }

    @Override
    public void onCreate() {
        //Made this class a singleton since we do not want concurrent syncs
        synchronized (syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"Returning binder");
        return syncAdapter.getSyncAdapterBinder();
    }
}
