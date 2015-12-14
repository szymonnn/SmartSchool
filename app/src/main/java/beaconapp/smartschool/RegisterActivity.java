package beaconapp.smartschool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RegisterActivity extends AppCompatActivity {
    public static final String NAME_KEY = "name";

    public static final String INDEX_KEY = "index";

    public static final String LASTNAME_KEY = "lastname";

    public static final String PASSWORD_KEY = "password";

    public static final String REGISTER_KEY = "register";

    public static final String LOGIN_KEY = "login";

    public static final String IS_LOGGED = "is_logged";

    public static final String SECURE_ID = "secure_id";

    private String mName;

    private String mLastname;

    private String mIndex;

    private String mPassword;

    private SharedPreferences mSharedPreferences;

    private EditText mIndexField;

    private EditText mNameField;

    private EditText mLastnameField;

    private EditText mPasswordField;

    private Button mRegisterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.i("ghcgjhb.jnk", "" + mSharedPreferences.getBoolean(IS_LOGGED, false));

        if (mSharedPreferences.getBoolean(IS_LOGGED, false)){
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            mIndexField = (EditText) findViewById(R.id.indexField);
            mNameField = (EditText) findViewById(R.id.nameField);
            mLastnameField = (EditText) findViewById(R.id.lastnameField);
            mRegisterButton = (Button) findViewById(R.id.registerButton);
            mPasswordField = (EditText)findViewById(R.id.passwordField);

            mRegisterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String mName = mNameField.getText().toString().trim();
                    String mLastname = mLastnameField.getText().toString().trim();
                    String mIndex = mIndexField.getText().toString().trim();
                    String mPassword = mPasswordField.getText().toString().trim();

                    if (mName.equals("") || mLastname.equals("") || mIndex.equals("") || mPassword.equals("")) {
                        Toast.makeText(RegisterActivity.this, "Please enter the data", Toast.LENGTH_SHORT).show();
                    } else {
                        new RegisterTask("http://192.166.202.83/register.php?imie=" + mName
                                + "&nazwisko=" + mLastname
                                + "&index=" + mIndex
                                + "&haslo=" + mPassword, REGISTER_KEY).execute();
                        mSharedPreferences.edit()
                                .putString(NAME_KEY, mName)
                                .putString(LASTNAME_KEY, mLastname)
                                .putString(INDEX_KEY, mIndex)
                                .putString(PASSWORD_KEY, mPassword)
                                .apply();
                    }
                }
            });
        }
    }

    class RegisterTask extends AsyncTask<Object, Void, String> {

        protected String mUrl;

        private String mKey;

        public RegisterTask(String url, String key) {
            mUrl = url;
            mKey = key;
        }
        @Override
        protected String doInBackground(Object... params) {
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
                        buffer.append(line + "\n");
                    }

//                    if (buffer.length() == 0) {
//                        // Stream was empty.  No point in parsing.
//                        return null;
//                    }
                    responseData = buffer.toString();
                    mSharedPreferences.edit().putString(SECURE_ID, responseData).apply();
                    Log.i("Register Task", responseData);

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
            return responseData;
        }

        @Override
        protected void onPostExecute(String result) {
            if (mKey.equals(RegisterActivity.REGISTER_KEY)){
                new RegisterTask("http://192.166.202.83/login.php?index="
                        + mSharedPreferences.getString(INDEX_KEY, "") + "&haslo=" + mSharedPreferences.getString(PASSWORD_KEY, ""), LOGIN_KEY).execute();
            } else if (mKey.equals(RegisterActivity.LOGIN_KEY)){
                Log.i("LOGEED", "");
                mSharedPreferences.edit().putBoolean(IS_LOGGED, true).apply();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }

        public void logException(Exception e) {
            Log.e("Register task", "Exception caught! ", e);
        }
    }
}



