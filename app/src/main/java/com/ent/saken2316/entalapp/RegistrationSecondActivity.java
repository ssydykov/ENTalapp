package com.ent.saken2316.entalapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.saken2316.entalapp.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegistrationSecondActivity extends ActionBarActivity {

    String email, password, firstName, lastName, token, sessionId;
    EditText editTextFirstName, editTextLastName;
    Button btnSignUp;
    Intent intent;
    boolean success;

    private ProgressDialog pDialog;

    // URL to get contacts JSON
    private static String url = "http://env-3315080.j.dnr.kz/mainapp/registration/";

    // JSON Node names
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_TEXT = "text";

    // URL to get contacts JSON
    String message_text = "";
    JSONObject messages = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_second);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // finally change the color
            window.setStatusBarColor(this.getResources().getColor(R.color.dark_primary));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            Window window = this.getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        intent = getIntent();
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");
        Log.e("email", email);
        Log.e("password", password);

        btnSignUp = (Button)findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editTextFirstName = (EditText)findViewById(R.id.editTextFirstName);
                editTextLastName = (EditText)findViewById(R.id.editTextLastName);
                firstName = editTextFirstName.getText().toString();
                lastName = editTextLastName.getText().toString();

                Context context = getApplicationContext();
                new CreateNewUser(context).execute();
            }
        });
    }

    private class CreateNewUser extends AsyncTask<String, String, String> {

        Context context;
        private CreateNewUser(Context _context){ this.context = _context; }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(RegistrationSecondActivity.this);
            pDialog.setMessage(getResources().getString(R.string.wait_profile));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("first_name", firstName));
            params.add(new BasicNameValuePair("last_name", lastName));

            String[] arrayListResponse = sh.makeServiceCall(url, ServiceHandler.SIGNUP,
                    params, null, null);

            token = arrayListResponse[0];
            sessionId = arrayListResponse[1];
            String jsonStr = arrayListResponse[2];

            Log.e("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    messages = jsonObj.getJSONObject(TAG_MESSAGE);

                    message_text = messages.getString(TAG_TEXT);
                    success = messages.getBoolean(TAG_SUCCESS);
                    Log.e("Success", Boolean.toString(success));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog

            if (pDialog.isShowing())
                pDialog.dismiss();

            if (success == false) {
                Toast.makeText(getBaseContext(), message_text,
                        Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle b = new Bundle();
                b.putString("token", token);
                b.putString("sessionId", sessionId);
                b.putString("status", "APP");
                intent.putExtras(b);
                context.startActivity(intent);
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_registration, menu);
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
}


