package beaconapp.smartschool;

import android.app.Application;
import android.util.Log;

import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.log.LogLevel;

/**
 * Created by SzymonN on 2015-11-21.
 */
public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        KontaktSDK.initialize(this)
                .setDebugLoggingEnabled(BuildConfig.DEBUG)
                .setLogLevelEnabled(LogLevel.DEBUG, true);
    }
}
