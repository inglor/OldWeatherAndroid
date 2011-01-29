package eu.spyropoulos.android.oldweather;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class OldWeatherApp extends Activity {
    public static final String TAG = "OldWeather";

    private Connection conn;
    private DbAdapter mDb;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        conn = new Connection();

        mDb = new DbAdapter(this);
        mDb.open();

        // Check if first run
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("first_run", true)) {
            resetSharedPreferences(prefs);
        }

        /*
        mDb.createUser("iniju", null);

        Cursor cur = mDb.fetchUser("iniju");
        String pwd = cur.getString(1);
        if (pwd == null) {
            netStatusMessage = Toast.makeText(OldWeatherApp.this, "String was null", Toast.LENGTH_SHORT);
        } else {
            netStatusMessage = Toast.makeText(OldWeatherApp.this, "String was not null, it was " + pwd, Toast.LENGTH_SHORT);
        }
        netStatusMessage.show();
*/
        final Button button = (Button) findViewById(R.id.ok);
        button.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        EditText usernameEditbox = (EditText) findViewById(R.id.username_box);
                        EditText passwordEditbox = (EditText) findViewById(R.id.passwd_box);


                        mDb.createUser(usernameEditbox.getText().toString(), passwordEditbox.getText().toString());
                        Cursor cur = mDb.fetchUser("inglor");
                        Toast netStatusMessage = null;

                        String pwd = cur.getString(1);
                        if (pwd == null) {
                            netStatusMessage = Toast.makeText(OldWeatherApp.this, "String was null", Toast.LENGTH_SHORT);
                        } else {
                            netStatusMessage = Toast.makeText(OldWeatherApp.this, "String was " + pwd, Toast.LENGTH_SHORT);
                        }
                        netStatusMessage.show();
                        conn.login("inglor", "artanis", getBaseContext());
                    }
                }
        );


        Toast conn_check = null;
        if (Connection.isOnline(getBaseContext())) {
            conn_check = Toast.makeText(getBaseContext(), "Provide username and password", Toast.LENGTH_SHORT);
            conn_check.show();

        } else {
            conn_check = Toast.makeText(getBaseContext(), "Connection unavailble", Toast.LENGTH_SHORT);
            conn_check.show();
        }
    }

    /**
     * Initialise shared preferences
     */
    private void resetSharedPreferences(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();

        // Delete all, if any, shared preferences
        editor.clear();

        // Set default values for preferences
        editor.putBoolean("first_run", false);
        editor.putBoolean("auto_login", false);
        editor.putString("last_login", "");

        // Save preferences
        editor.commit();
    }
}

