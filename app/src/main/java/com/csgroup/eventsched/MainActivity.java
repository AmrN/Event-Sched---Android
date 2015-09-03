package com.csgroup.eventsched;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;


public class MainActivity extends ActionBarActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private CheckBox chkRemember;
    private boolean rememberLogin = false;

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final boolean CLEAR_PREFERENCES = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // store views references
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        chkRemember = (CheckBox) findViewById(R.id.chkRemember);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                LoginTask loginTask = new LoginTask();
                loginTask.execute(new String[]{email, password});
            }
        });

        chkRemember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rememberLogin = isChecked;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (CLEAR_PREFERENCES) {
            PreferencesManager prefManager = new PreferencesManager(this, null);
            prefManager.clearAll();
        }

        // if API_KEY exists, then the user has logged in previously
        // and we should redirect him to HomeActivity
        if (!loginRequired()) {
            this.rememberLogin = true;
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PreferencesManager preferencesManager = new PreferencesManager(this, null);
        preferencesManager.setRememberLogin(rememberLogin);
    }

    private boolean loginRequired() {
        PreferencesManager prefManager = new PreferencesManager(this, null);
        String apiKey = prefManager.getApiKey();

        if (apiKey == null) {
            Log.v(LOG_TAG, "Couldn't find API_KEY, login required");
            return true;
        }

        Log.v(LOG_TAG, "Found API_KEY");

        boolean rememberLogin = prefManager.getRememberLogin();
        if (rememberLogin) {
            Log.v(LOG_TAG, "Login is remembered, no need to login");
            return false;
        }
        else {
            Log.v(LOG_TAG, "Login is not remembered, login required");
            return true;
        }

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

    private class LoginTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = LoginTask.class.getSimpleName();
        private final String ROUTE = "login";

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> payload = new HashMap<>();
            payload.put("email", params[0]);
            payload.put("password", params[1]);

            HTTPManager httpManager = new HTTPManager();
            String jsonResponse = httpManager.post(ROUTE, null, payload);

            Log.v(LOG_TAG, "Login jsonResponse: " + jsonResponse);

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            if (jsonResponse != null) {
                try {
                    User user = JsonParser.parseLogin(jsonResponse);
                    // if parsing was successful
                    if (user != null) {
                        Toast.makeText(MainActivity.this, "Logged in Successfully!", Toast.LENGTH_LONG)
                                .show();
                        Log.v(LOG_TAG, "Parsed Login json: " + user.toString());

                        // set API_KEY in preferences
                        PreferencesManager prefManager = new PreferencesManager(MainActivity.this, null);
                        prefManager.setApiKey(user.getApiKey());
                        prefManager.setUserId(Integer.toString(user.getId()));

                        // redirect to HomeActivity
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish();
                    }
                    else {
                        // parsing failed

                    }
                } catch (JsonParser.JsonParserException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }


}
