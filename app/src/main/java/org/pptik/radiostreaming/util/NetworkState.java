package org.pptik.radiostreaming.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by Hafid on 4/27/2017.
 */

public class NetworkState {
    public static boolean isOnline(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean connect = netInfo != null && netInfo.isConnectedOrConnecting();
        if (!connect) Toast.makeText(ctx, "Layanan internet tidak tersedia!", Toast.LENGTH_LONG).show();
        return connect;
    }
}
