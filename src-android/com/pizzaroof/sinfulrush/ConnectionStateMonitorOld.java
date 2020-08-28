package com.pizzaroof.sinfulrush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionStateMonitorOld extends BroadcastReceiver {

    private AndroidLauncher launcher;

    public ConnectionStateMonitorOld(AndroidLauncher launcher) {
        super();
        this.launcher = launcher;
    }

    public void enable(Context context) {
        context.registerReceiver(this, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void disable(Context context) {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null || intent.getExtras() == null)
            return;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(manager == null) return;
        NetworkInfo ni = manager.getActiveNetworkInfo();
        if(ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
            launcher.onConnectedToInternet();
        } /*else if(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
						onDisconnectedFromInternet();
        }*/
    }
}
