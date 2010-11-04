package eu.spyropoulos.android.oldweather;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class OldWeatherApp extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Toast netStatusMessage = null;
        if (Connection.isOnline(getBaseContext())) {
            netStatusMessage = Toast.makeText(OldWeatherApp.this, "Network is on", Toast.LENGTH_SHORT);
        } else {
            netStatusMessage = Toast.makeText(OldWeatherApp.this, "Network is off", Toast.LENGTH_SHORT);
        }
        netStatusMessage.show();
    }
}

