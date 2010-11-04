package eu.spyropoulos.android.oldweather;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

public class Connection {

    /**
     * Check to see if we there is network connection
     *
     * @param context The base context passed from an activity
     * @return True is connected or connecting, false otherwise
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm.getActiveNetworkInfo() != null) {
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } else {
            return false;
        }
    }
}


