package beaconapp.smartschool;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.DistanceSort;
import com.kontakt.sdk.android.ble.discovery.EventType;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconAdvertisingPacket;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilter;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_PRESENCE = "key_presence";
    public static final String KEY_WINNERS = "key_winners";
    public static final String KEY_TIME = "time";
    private ProximityManager mProximityManager;
    private SharedPreferences mSharedPreferences;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new RegisterTask("http://192.166.202.83/get_highscore.php", KEY_WINNERS).execute();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mListView = (ListView) findViewById(R.id.listView);
        mListView.setAdapter(new MyAdapter(this, android.R.layout.simple_list_item_1, new String [] {"Ania Kowalska", "Miłosz Nowak", "Jan Madej", "Dariusz Kwiecień", "Halina Cep", "Małgorzata Luty", "Ania Kowalska", "Miłosz Nowak", "Jan Madej", "Dariusz Kwiecień", "Halina Cep", "Małgorzata Luty"},
                new int [] {68, 45, 34, 28, 25, 23, 19, 16, 12, 10, 5, 1}));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setView(R.layout.photo);
                    builder.setTitle("You are the winner!");
                    builder.setNegativeButton(android.R.string.cancel, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            }
        });
        final ProximityManager.ProximityListener proximityListener = new ProximityManager.ProximityListener() {
            @Override
            public void onScanStart() {
            }

            @Override
            public void onScanStop() {
            }

            @Override
            public void onEvent(BluetoothDeviceEvent bluetoothDeviceEvent) {
                AudioManager am= (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
                switch (bluetoothDeviceEvent.getEventType()) {
                    case SPACE_ENTERED:
                        Log.i("SmartSchool", "Space entered");
                        am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                        if (mSharedPreferences.getLong(KEY_TIME, 0) < System.currentTimeMillis() - 500000){
                            new RegisterTask("http://192.166.202.83/check_presence.php?studentid=" + mSharedPreferences.getString(RegisterActivity.INDEX_KEY, "")
                                    + "&lectureid=3" + "&presence=1" + "&exit=0", KEY_PRESENCE).execute();
                            mSharedPreferences.edit().putLong(KEY_TIME, System.currentTimeMillis()).apply();
                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(MainActivity.this)
                                            .setSmallIcon(R.mipmap.ic_launcher)
                                            .setContentTitle("BLP")
                                            .setContentText("You are asigned to the lecture attendance list!")
                                    .setAutoCancel(true);

// Creates an explicit intent for an Activity in your app
                            Intent resultIntent = new Intent(MainActivity.this, MainActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(MainActivity.this);
// Adds the back stack for the Intent (but not the Intent itself)
                            stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
                            stackBuilder.addNextIntent(resultIntent);
                            PendingIntent resultPendingIntent =
                                    stackBuilder.getPendingIntent(
                                            0,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                    );
                            mBuilder.setContentIntent(resultPendingIntent);
                            NotificationManager mNotificationManager =
                                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
                            mNotificationManager.notify(1, mBuilder.build());

                        }

                        break;

                    case DEVICE_DISCOVERED:
                        Log.i("SmartSchool", "device discovered");

                        break;

                    case SPACE_ABANDONED:
                        Log.i("SmartSchool", "space abandoned");
                        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        break;

                    default:
                        throw new IllegalStateException("This event should never occur because it is not specified in ScanContext: " + bluetoothDeviceEvent.getEventType().name());
                }
            }
        };

        final ScanContext scanContext = new ScanContext.Builder()
                .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                .setScanPeriod(new ScanPeriod(3000, 0))
                .setIBeaconScanContext(new IBeaconScanContext.Builder()
                                .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
                                .setEventTypes(EnumSet.of(EventType.SPACE_ENTERED, EventType.SPACE_ABANDONED))
                                .setDevicesUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(2))
                                .setDistanceSort(DistanceSort.DESC)
                                .setIBeaconFilters(Collections.singleton(new IBeaconFilter() {
                                    @Override
                                    public boolean apply(IBeaconAdvertisingPacket iBeaconAdvertisingPacket) {
                                        final UUID proximityUUID = iBeaconAdvertisingPacket.getProximityUUID();
                                        final double distance = iBeaconAdvertisingPacket.getDistance();

                                        return proximityUUID.equals(UUID.fromString("482e827a-93bd-48ef-b8e9-2a42b69c8a74")) && distance <= 1.5;
                                    }
                                }))
                                .build()
                )
                .build();
        mProximityManager = new ProximityManager(this);
        mProximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                mProximityManager.initializeScan(scanContext);
                mProximityManager.attachListener(proximityListener);
            }

            @Override
            public void onConnectionFailure() {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class RegisterTask extends AsyncTask<Object, Void, String[][]> {

        protected String mUrl;
        protected String mKey;

        public RegisterTask(String url, String key) {
            mUrl = url;
            mKey = key;
        }
        @Override
        protected String[][] doInBackground(Object... params) {
            int responseCode = -1;
            String responseData = "";
            try {
                Log.i("ghfkjb.jn", mUrl);
                URL blogFeedUrl = new URL(mUrl);
                HttpURLConnection connection = (HttpURLConnection) blogFeedUrl
                        .openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader;
                    StringBuffer buffer = new StringBuffer();

                    if (inputStream == null) {
                        // Nothing to do.
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line =  reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line);
                    }

//                    if (buffer.length() == 0) {
//                        // Stream was empty.  No point in parsing.
//                        return null;
//                    }
 //                   responseData = buffer.toString();
//                    JSONArray arrayMain = new JSONArray(responseData);
//                    JSONArray array = arrayMain.getJSONArray(0);
//                    String name = (String) array.get(0);
//                    String lastname = (String) array.get(1);
//                 int counter = (int)array.get(2);
                    responseData = "[[\"Jan\", \"Nowak\", 5], [\"Jan\", \"Nowak\",  5]]\n";
                    //responseData = responseData.toString();
                    JSONArray array = new JSONArray(responseData);
                    Log.i("Register Task", responseData + array.getJSONArray(0).get(0));

                } else {
                    Log.i("Register Task", "Unsuccesful HTTP response Code: "
                            + responseCode);
                }
                Log.i("Register Task", "Code: " + responseCode);
            } catch (MalformedURLException e) {
                logException(e);
            } catch (IOException e) {
                logException(e);
            } catch (Exception e) {
                logException(e);
            }
            return null;//responseData;
        }

        @Override
        protected void onPostExecute(String[][] result) {
            if (mKey.equals(KEY_WINNERS)){
                setListView(result);
            }
        }

        public void logException(Exception e) {
            Log.e("Register task", "Exception caught! ", e);
        }
    }

    private void setListView(String[][] result) {

    }
}
