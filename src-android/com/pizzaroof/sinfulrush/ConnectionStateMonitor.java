package com.pizzaroof.sinfulrush;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import androidx.annotation.RequiresApi;

@RequiresApi(21)
public class ConnectionStateMonitor extends ConnectivityManager.NetworkCallback {

    private AndroidLauncher launcher;
    private final NetworkRequest networkRequest;

    public ConnectionStateMonitor(AndroidLauncher launcher) {
        networkRequest = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        this.launcher = launcher;
    }

    public void enable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null)
            connectivityManager.registerNetworkCallback(networkRequest , this);
    }

    public void disable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null)
            connectivityManager.unregisterNetworkCallback(this);
    }

    @Override
    public void onAvailable(Network network) {
        launcher.onConnectedToInternet();
    }

    /*@Override
    public void onLost(Network network) {
        launcher.onDisconnectedFromInternet();
    }*/
}
