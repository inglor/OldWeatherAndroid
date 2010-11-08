package eu.spyropoulos.android.oldweather;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

public class Connection {

    private DefaultHttpClient mHttpClient;

    /**
     * Check to see if there is network connection.
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

    public Connection() {
        mHttpClient = new DefaultHttpClient();
    }

    /**
     * Login to server.
     *
     * @param login
     * @param password
     */
    public boolean login(String login, String password) {
        HttpPost httpPost = new HttpPost("https://login.zooniverse.org/login?service=http%3A%2F%2Fwww.oldweather.org%2Fclassify");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("username", login));
        nvps.add(new BasicNameValuePair("password", password));
        nvps.add(new BasicNameValuePair("service", "http://www.oldweather.org/classify"));
        
    }
}


