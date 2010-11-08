package eu.spyropoulos.android.oldweather;

import android.app.Activity;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.widget.Toast;



public class OldWeatherApp extends Activity {
    private static final String TAG = "OldWeather";

    private DbAdapter mDb;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mDb = new DbAdapter(this);
        mDb.open();

        // Check if first run
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("first_run", true)) {
            resetSharedPreferences(prefs);
        }

        mDb.createUser("iniju", null);

        Toast netStatusMessage = null;
        Cursor cur = mDb.fetchUser("iniju");
        String pwd = cur.getString(1);
        if (pwd == null) {
            netStatusMessage = Toast.makeText(OldWeatherApp.this, "String was null", Toast.LENGTH_SHORT);
        } else {
            netStatusMessage = Toast.makeText(OldWeatherApp.this, "String was not null, it was " + pwd, Toast.LENGTH_SHORT);
        }
        netStatusMessage.show();


        netStatusMessage = null;
        if (Connection.isOnline(getBaseContext())) {
            netStatusMessage = Toast.makeText(OldWeatherApp.this, "Network is on", Toast.LENGTH_SHORT);
        } else {
            netStatusMessage = Toast.makeText(OldWeatherApp.this, "Network is off", Toast.LENGTH_SHORT);
        }
        netStatusMessage.show();
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

