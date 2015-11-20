package com.ent.saken2316.entalapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.example.saken2316.entalapp.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.util.VKUtil;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    Context context;
    Button btnRegistration, btnLogin, btnLoginVK, btnLoginFacebook;
    VKRequest request, request1;
    CallbackManager callbackManager;
    ProgressDialog pDialog;

    private static String url = "http://env-3315080.j.dnr.kz/mainapp/login_sn/";

    // JSON Node names
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_TEXT = "text";

    // URL to get contacts JSON
    String message_text = "";
    JSONObject messages = null;
    JSONArray message = null;

    String first_name, last_name, avatar, city = "Kazakhstan", id_vk, id_fb, token, sessionId, friends;
    boolean success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this.getApplicationContext();

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
        SharedPreferences mPrefs = getSharedPreferences("user", 0);
        String token = mPrefs.getString("token", "");
        String sessionId = mPrefs.getString("sessionId", "");
        if (token != null && !token.isEmpty())
        {
            Log.e("Hello", "token");
            Log.e("token", token);
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle b = new Bundle();
            b.putString("token", token);
            b.putString("sessionId", sessionId);
            intent.putExtras(b);
            context.startActivity(intent);
        }

        VKSdk.initialize(context);
        FacebookSdk.sdkInitialize(getApplicationContext());
        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        Log.e("Certificate", fingerprints[0]);
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                final GraphRequest request = GraphRequest.newMeRequest(accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {

                                id_vk = "0";
                                if (jsonObject != null) {
                                    try {

                                        JSONObject picture = jsonObject.getJSONObject("picture");
                                        JSONObject picture_data = picture.getJSONObject("data");
                                        avatar = picture_data.getString("url");
                                        id_fb = jsonObject.getString("id");
                                        first_name = jsonObject.getString("first_name");
                                        last_name = jsonObject.getString("last_name");

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Log.e("ServiceHandler", "Couldn't get any data from the url");
                                }
                                if (friends != null){
                                    executionDone();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, picture");
                request.setParameters(parameters);
                request.executeAsync();
                GraphRequest request1 = GraphRequest.newMyFriendsRequest(accessToken,
                        new GraphRequest.GraphJSONArrayCallback() {
                            @Override
                            public void onCompleted(JSONArray jsonArray, GraphResponse graphResponse) {

                                JSONObject jsonObject = graphResponse.getJSONObject();
                                if (jsonObject != null) {
                                    try {
                                        JSONArray response = jsonObject.getJSONArray("data");
                                        friends = "{\"friends\": [";
                                        Log.e("Length", Integer.toString(response.length()));
                                        if (response.length() != 0){
                                            for (int i = 0; i < response.length() - 1; i++){
                                                JSONObject c = response.getJSONObject(i);
                                                String id = c.getString("id");
                                                friends += id + ", ";
                                            }
                                            JSONObject c = response.getJSONObject(response.length() - 1);
                                            String id = c.getString("id");
                                            friends += id + "]}";
                                        }
                                        else {
                                            friends += "]}";
                                        }
                                        Log.e("Friends", friends);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Log.e("ServiceHandler", "Couldn't get any data from the url");
                                }
                                if (id_fb != null){
                                    executionDone();
                                }
                            }
                        });
                Bundle parameters1 = new Bundle();
                parameters1.putString("fields", "id, first_name, last_name, picture");
                request1.setParameters(parameters1);
                request1.executeAsync();
            }
            @Override
            public void onCancel() { }
            @Override
            public void onError(FacebookException e) {
                if (e instanceof FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut();
                    }
                }
            }
        });

        btnRegistration = (Button) findViewById(R.id.btnRegistration);
        btnRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object activity = RegistrationActivity.class;
                Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
                startActivity(intent);
            }
        });
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object activity = LoginActivity.class;
                Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
                startActivity(intent);
            }
        });
        btnLoginVK = (Button) findViewById(R.id.btnLoginVk);
        btnLoginVK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VKSdk.login(MainActivity.this, "scope=friends");
            }
        });
        btnLoginFacebook = (Button) findViewById(R.id.btnLoginFacebook);
        btnLoginFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Button", "clicked");
                //LoginManager.getInstance().logInWithReadPermissions(MainActivity.this,
                //      Arrays.asList("user_friends"));
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this,
                        Arrays.asList("user_friends", "public_profile"));

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Facebook Result:
        callbackManager.onActivityResult(requestCode, resultCode, data);

        // VK Result:
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {

                request = new VKRequest("users.get", VKParameters.from(VKApiConst.FIELDS, "photo_100,sex,city"));
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {

                        String jsonStr = response.responseString;
                        id_fb = "0";

                        if (jsonStr != null) {
                            try {
                                JSONObject jsonObj = new JSONObject(jsonStr);

                                // Getting JSON Array node
                                message = jsonObj.getJSONArray("response");
                                JSONObject a = message.getJSONObject(0);

                                first_name = a.getString("first_name");
                                last_name = a.getString("last_name");
                                avatar = a.getString("photo_100");
                                id_vk = a.getString("id");
                                JSONObject city_title = a.getJSONObject("city");
                                city = city_title.getString("title");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.e("ServiceHandler", "Couldn't get any data from the url");
                        }
                        if (friends != null){
                            executionDone();
                        }
                    }
                });
                request1 = new VKRequest("friends.get", VKParameters.from(VKApiConst.FIELDS, "photo_100,sex,city"));
                request1.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {

                        String jsonStr = response.responseString;
                        Log.e("Response", jsonStr);
                        if (jsonStr != null) {
                            try {
                                JSONObject jsonObj = new JSONObject(jsonStr);
                                JSONObject response1 = jsonObj.getJSONObject("response");
                                JSONArray items = response1.getJSONArray("items");
                                friends = "{\"friends\": [";
                                for (int i = 0; i < items.length() - 1; i++){
                                    JSONObject c = items.getJSONObject(i);
                                    String id = c.getString("id");
                                    friends += id + ", ";
                                }
                                JSONObject c = items.getJSONObject(items.length() - 1);
                                String id = c.getString("id");
                                friends += id + "]}";
                                Log.e("Friends", friends);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.e("ServiceHandler", "Couldn't get any data from the url");
                        }
                        if (id_vk != null){
                            executionDone();
                        }
                    }
                });
            }

            @Override
            public void onError(VKError error) {
                Log.e("Sau bol", "User");
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void executionDone(){

        Log.e("Error", "catch1");
        new VKFB(context).execute();
    }

    private class VKFB extends AsyncTask<String, String, String> {

        Context context;
        private VKFB(Context _context){ this.context = _context; }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();

            // Building Parameters
            Log.e("Error", "catch2");
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("first_name", first_name));
            params.add(new BasicNameValuePair("last_name", last_name));
            params.add(new BasicNameValuePair("friends", friends));
            params.add(new BasicNameValuePair("avatar", avatar));
            params.add(new BasicNameValuePair("city", city));
            params.add(new BasicNameValuePair("id_vk", id_vk));
            params.add(new BasicNameValuePair("id_fb", id_fb));
            Log.e("Error", "catch3");

            Log.e("Vk id", id_vk);
            Log.e("Fb id", id_fb);
            Log.e("First name", first_name);
            Log.e("Last name", last_name);
            Log.e("Avatar", avatar);
//            Log.e("City", city);
            Log.e("Friends", friends);

            Log.e("Error", "catch4");
            String[] arrayListResponse = sh.makeServiceCall(url, ServiceHandler.SIGNUP,
                    params, null, null);
            Log.e("Error", "catch5");

            token = arrayListResponse[0];
            sessionId = arrayListResponse[1];
            Log.e("Error", "catch6");
            Log.e("token", token);
            Log.e("session id", sessionId);
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

            Log.e("Error", "catch7");
            if (!success) {
                Log.e("Success", "false");
                Log.e("sss", Boolean.toString(success));
                Toast.makeText(getBaseContext(), message_text,
                        Toast.LENGTH_SHORT).show();
            } else {
                Log.e("Success", "true");
                Log.e("sss", Boolean.toString(success));
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle b = new Bundle();
                b.putString("token", token);
                b.putString("sessionId", sessionId);
                intent.putExtras(b);
                context.startActivity(intent);
            }
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
}