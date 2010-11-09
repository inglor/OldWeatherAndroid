package eu.spyropoulos.android.oldweather;

import static eu.spyropoulos.android.oldweather.OldWeatherApp.TAG;

import android.content.Context;
import android.os.AsyncTask;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

public class Connection {
    private static final String LOGIN_URL = "http://www.oldweather.org/classify";

    private DefaultHttpClient mHttpClient;

    /**
     * Constructor.
     * Initialises the client.
     */
    public Connection() {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        //HttpProtocolParams.setUseExpectContinue(params, true);
        HttpProtocolParams.setUserAgent(params, "OldWeatherAndroid App");
        
        mHttpClient = new DefaultHttpClient(params);
    }

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

    /**
     * Login to server.
     *
     * @param login
     * @param password
     */
    public void login(String login, String password, Context ctx) {
        LoginTask loginTask = new LoginTask(ctx);
        loginTask.execute(login, password);
    }

    /** Async Tasks */
    private class LoginTask extends AsyncTask<String, Void, Boolean> {
        private final WeakReference<Context> mCtxRef;

        public LoginTask(Context ctx) {
            mCtxRef = new WeakReference<Context>(ctx);
        }

        @Override
        protected Boolean doInBackground(String... credentials) {
            if (credentials.length == 2) {
                return Boolean.valueOf(doInBackgroundLogin(credentials[0], credentials[1]));
            }
            return Boolean.valueOf(false);
        }
        @Override
        protected void onPostExecute(Boolean result) {
            boolean success = false;
            if (isCancelled()) {
                success = false;
            } else {
                success = result.booleanValue();
            }

            if (mCtxRef != null) {
                Context ctx = mCtxRef.get();
                if (ctx != null) {
                    Toast msg = null;
                    if (success) {
                        Log.i(TAG, "login successful");
                        msg = Toast.makeText(ctx, "YES!!!!!", Toast.LENGTH_SHORT);
                    } else {
                        Log.i(TAG, "login unsuccessful");
                        msg = Toast.makeText(ctx, "NO :(", Toast.LENGTH_SHORT);
                    }
                    msg.show();
                }
            }
        }
        private boolean doInBackgroundLogin(String login, String password) {
            try {
                HttpGet httpGet = new HttpGet(LOGIN_URL);
                HttpResponse response = mHttpClient.execute(httpGet);
                HttpEntity resp_entity = response.getEntity();

                if (resp_entity != null) {
                    String login_page = EntityUtils.toString(resp_entity);
                    Log.i(TAG, login_page);
                }
            } catch (ClientProtocolException e) {
                Log.e(TAG, "login: ClientProtocolException: " + e.getMessage());
                return false;
            } catch (IOException e) {
                Log.e(TAG, "login: IOException: " + e.getMessage());
                return false;
            }
            return true;

            /*
               HttpPost httpPost = new HttpPost("https://login.zooniverse.org/login?service=http%3A%2F%2Fwww.oldweather.org%2Fclassify");

               List<NameValuePair> nvps = new ArrayList<NameValuePair>();
               nvps.add(new BasicNameValuePair("username", login));
               nvps.add(new BasicNameValuePair("password", password));
               nvps.add(new BasicNameValuePair("service", "http://www.oldweather.org/classify"));
               */
        }

    }
}


