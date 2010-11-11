package eu.spyropoulos.android.oldweather;

import static eu.spyropoulos.android.oldweather.OldWeatherApp.TAG;

import android.content.Context;
import android.os.AsyncTask;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class Connection {
    private static final String LOGIN_URL = "http://www.oldweather.org/classify?vessel_id=4caf8377cadfd34197000005";

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
                        msg = Toast.makeText(ctx, "Login Successful", Toast.LENGTH_SHORT);
                    } else {
                        Log.i(TAG, "login unsuccessful");
                        msg = Toast.makeText(ctx, "Unable to Login", Toast.LENGTH_SHORT);
                    }
                    msg.show();
                }
            }
        }
        private boolean doInBackgroundLogin(String login, String password) {
            try {
                // First get the login webpage, so we can extract the hidden input lt code and the login URL (the original is redirected)
                HttpGet httpGet = new HttpGet(LOGIN_URL);
                HttpContext context = new BasicHttpContext();
                HttpResponse response = mHttpClient.execute(httpGet, context);
                HttpEntity resp_entity = response.getEntity();

                if (resp_entity != null) {
                    // Find the redirected URL, as this is where the form is submitted (with the suffix added)
                    HttpHost currentHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
                    Log.i(TAG, "host: " + currentHost.toURI());
                    HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
                    Log.i(TAG, "uri: " + currentReq.getURI());

                    String login_page_html = EntityUtils.toString(resp_entity);
                    Log.i(TAG, login_page_html);
                    // Locate and extract the contents of the form tag
                    Pattern formPattern = Pattern.compile("<form action=\"([^\"]+)\" method=\"post\" [^>]*id=\"login-form\">(.*?)</form>");
                    Matcher m = formPattern.matcher(login_page_html);
					String login_url = currentHost.toURI();
					String form_contents = null;
                    if (m.find() && m.groupCount() == 2) {
                        login_url += m.group(1); // This is the suffix to the redirected URL where the form is submitted
                        form_contents = m.group(2); // This contains the form inputs and needs further parsing
                        Log.i(TAG, form_contents);
                    }

                    // Examine header
                    for (Header h : response.getAllHeaders()) {
                        Log.i(TAG, "key: " + h.getName() + ", value: " + h.getValue());
                    }

                    // Examine cookies
                    CookieStore cs = mHttpClient.getCookieStore();
                    for (Cookie c : cs.getCookies()) {
                        Log.i(TAG, "Cookie: path: " + c.getPath() + " name: " + c.getName() + " value: " + c.getValue());
                    }

					// Extract lt value and service name
					String lt_code = null;
					String service = null;
					Pattern ltPattern = Pattern.compile("<input type=\"hidden\" name=\"lt\" value=\"([^\"]*)\" id=\"lt\"/>");
					m = ltPattern.matcher(form_contents);
					if (m.find() && m.groupCount() == 1) {
						lt_code = m.group(1);
					}
					Pattern servicePattern = Pattern.compile(
							"<input type=\"hidden\" name=\"service\" value=\"([^\"]*)\" id=\"service\"/>");
					m = servicePattern.matcher(form_contents);
					if (m.find() && m.groupCount() == 1) {
						service = m.group(1);
					}

                    // HTTP Post
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(login_url);
                    
                    // Insert Userdata into form
                    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                    nvps.add(new BasicNameValuePair("username", login));
                    nvps.add(new BasicNameValuePair("password", password));
                    nvps.add(new BasicNameValuePair("lt", lt_code));
                    nvps.add(new BasicNameValuePair("service", service));
                    httppost.setEntity(new UrlEncodedFormEntity(nvps));
                    
                    // Execute Http Post request
                   	HttpResponse post_response = httpclient.execute(httppost);
                   	Log.i(TAG, "Login response: " + EntityUtils.toString(post_response.getEntity()));

                    // Examine cookies
                    cs = mHttpClient.getCookieStore();
                    for (Cookie c : cs.getCookies()) {
                        Log.i(TAG, "Cookie: path: " + c.getPath() + " name: " + c.getName() + " value: " + c.getValue());
                    }

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


