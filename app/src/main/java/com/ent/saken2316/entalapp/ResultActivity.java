package com.ent.saken2316.entalapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.saken2316.entalapp.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class ResultActivity extends ActionBarActivity {

    private static String url = "http://env-3315080.j.dnr.kz/mainapp/gameresult/";
    private static String url2 = "http://env-3315080.j.dnr.kz/mainapp/iwanttoplaywithfriend/";
    private static String url3 = "http://env-3315080.j.dnr.kz/mainapp/playwithbot/";

    JSONObject myResult = null;
    JSONObject message = null;
    JSONArray questionsJSON = null;

    Intent intent;
    Toolbar toolbar;
    ImageView imageView1, imageView2;
    TextView textViewName1, textViewName2, textViewInternetStatus,
            textViewStatus, textViewPoint1, textViewPoint2;
    ProgressBar progressBar;
    ProgressDialog pDialog;
    String userName, opponentName, opponentTotal, opponentPoint, myTotal,
            categoryName, date, gameId, category_id, friend_id, avatar1, avatar2;
    String questions[], answer1[], answer2[], answer3[], answer4[];
    int right_answer[];
    String token, sessionId;
    Boolean success = false, isInternet;
    Context context;
    ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        context = getApplicationContext();
        isInternet = isNetworkAvailable();

        // Intent values:
        intent = getIntent();
        token = intent.getStringExtra("token");
        sessionId = intent.getStringExtra("sessionId");
        gameId = intent.getStringExtra("gameId");

        // View values:
        progressBar = (ProgressBar) findViewById(R.id.progressBar3);
        progressBar.setVisibility(View.VISIBLE);
        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        textViewInternetStatus = (TextView) findViewById(R.id.textViewInternetStatus);
        textViewName1 = (TextView) findViewById(R.id.textViewName1);
        textViewName2 = (TextView) findViewById(R.id.textViewName2);
        textViewPoint1 = (TextView) findViewById(R.id.textViewPoint1);
        textViewPoint2 = (TextView) findViewById(R.id.textViewPoint2);
        imageView1 = (ImageView) findViewById(R.id.avatar1);
        imageView2 = (ImageView) findViewById(R.id.avatar2);

        // Shared preferences values:
        SharedPreferences sharedPreferencesProfile = getSharedPreferences("user", 0);
        userName = sharedPreferencesProfile.getString("first_name", "");
        avatar1 = sharedPreferencesProfile.getString("avatar", "");

        // Create toolbar:
        toolbarBuilder();

        // Create status bar:
        statusBarBuilder();

        if (isInternet){
            new GetMyResult().execute();
        }
        else {
            progressBar.setVisibility(View.INVISIBLE    );
            textViewInternetStatus.setText(getResources().getString(R.string.connection_error));
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void toolbarBuilder(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object activity = ProfileActivity.class;
                Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("token", token);
                intent.putExtra("sessionId", sessionId);
                startActivity(intent);
            }
        });
    }
    private void statusBarBuilder(){

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
    }

    private class GetMyResult extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("game_id", gameId));
            String[] arrayListResponse = sh.makeServiceCall(url, ServiceHandler.POST,
                    params, token, sessionId);
            String jsonStr = arrayListResponse[2];
            Log.e("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    myResult = jsonObj.getJSONObject("message");
                    success = myResult.getBoolean("success");
                    date = myResult.getString("date");
                    opponentName = myResult.getString("opponent_name");
                    avatar2 = myResult.getString("opponent_avatar");
                    myTotal = myResult.getString("my_points");
                    categoryName = myResult.getString("category_name");
                    category_id = myResult.getString("category_id");
                    friend_id = myResult.getString("opponent_id");

                    if (success){
                        opponentTotal = myResult.getString("opponent_points");
                    }
                    else {
                        opponentTotal = "...";
                    }

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

            toolbar.setTitle(categoryName);
            textViewPoint1.setText(myTotal);
            textViewName1.setText(userName);
            textViewName2.setText(opponentName);
            textViewPoint2.setText(opponentTotal);

            Glide.with(context)
                    .load(avatar1)
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(imageView1);
            Glide.with(context)
                    .load(avatar2)
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(imageView2);

            if (success)
            {
                progressBar.setVisibility(View.INVISIBLE);
                if (Integer.parseInt(myTotal) > Integer.parseInt(opponentTotal)){
                    textViewStatus.setText(getResources().getString(R.string.win));
                }
                else if (Integer.parseInt(myTotal) < Integer.parseInt(opponentTotal)){
                    textViewStatus.setText(getResources().getString(R.string.lose));
                }
                else {
                    textViewStatus.setText(getResources().getString(R.string.dead_heat));
                }
            }
            else
            {
                textViewStatus.setTextSize(15);
                textViewStatus.setText(getResources().getString(R.string.wait));
                Runnable task = new Runnable() {
                    public void run() {
                        isInternet = isNetworkAvailable();
                        if (isInternet){
                            new GetMyResult().execute();
                        }
                        else {
                            textViewInternetStatus.setText(getResources().getString(R.string.connection_error));
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.oops),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                worker.schedule(task, 5, TimeUnit.SECONDS);
            }
        }
    }

    public void onClickAnotherGame(View view){

        Object activity = ChooseOpponentActivity.class;
        Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
        intent.putExtra("token", token);
        intent.putExtra("sessionId", sessionId);
        startActivity(intent);
    }

    public void onClickRevenge(View view){
        new IWantToPlayWithFriend().execute();
    }

    private class IWantToPlayWithFriend extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(ResultActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("category_id", category_id));
            params.add(new BasicNameValuePair("friend_id", friend_id));
            String[] arrayListResponse;
            if (friend_id.equals("1")){
                arrayListResponse = sh.makeServiceCall(url3, ServiceHandler.POST,
                        params, token, sessionId);
            }
            else {
                arrayListResponse = sh.makeServiceCall(url2, ServiceHandler.POST,
                        params, token, sessionId);
            }
            String jsonStr = arrayListResponse[2];
            Log.e("Response", arrayListResponse[2]);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    message = jsonObj.getJSONObject("message");
                    success = message.getBoolean("success");
                    if (success){
                        gameId = message.getString("game_id");
                        opponentName = message.getString("opponent_name");
                        avatar2 = message.getString("opponent_avatar");
                        opponentPoint = message.getString("opponent_points");
                        questionsJSON = message.getJSONArray("questions");

                        questions = new String[questionsJSON.length()];
                        answer1 = new String[questionsJSON.length()];
                        answer2 = new String[questionsJSON.length()];
                        answer3 = new String[questionsJSON.length()];
                        answer4 = new String[questionsJSON.length()];
                        right_answer = new int[questionsJSON.length()];

                        // looping through All Contacts
                        for (int i = 0; i < questionsJSON.length(); i++) {

                            JSONObject c = questionsJSON.getJSONObject(i);
                            questions[i] = c.getString("question");
                            answer1[i] = c.getString("answer_1");
                            answer2[i] = c.getString("answer_2");
                            answer3[i] = c.getString("answer_3");
                            answer4[i] = c.getString("answer_4");
                            right_answer[i] = c.getInt("correct_answer");
                        }
                    }
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
            pDialog.dismiss();

            Toast.makeText(getBaseContext(), getResources().getString(R.string.want_to_play),
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getApplicationContext(), ReadyToPlayActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle b = new Bundle();
            b.putString("token", token);
            b.putString("sessionId", sessionId);
            b.putString("game_id", gameId);
            b.putString("opponent_name", opponentName);
            b.putString("opponent_avatar", avatar2);
            b.putString("opponent_point", opponentPoint);
            b.putStringArray("questions", questions);
            b.putStringArray("answer1", answer1);
            b.putStringArray("answer2", answer2);
            b.putStringArray("answer3", answer3);
            b.putStringArray("answer4", answer4);
            b.putIntArray("answer", right_answer);
            intent.putExtras(b);
            getApplicationContext().startActivity(intent);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            worker.shutdownNow();
            worker.shutdown();
            Object activity = ProfileActivity.class;
            Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
            intent.putExtra("token", token);
            intent.putExtra("sessionId", sessionId);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        isInternet = isNetworkAvailable();
        if (id == R.id.action_refresh && isInternet) {

            progressBar.setVisibility(View.VISIBLE);
            new GetMyResult().execute();
            return true;
        } else {
            textViewStatus.setText(getResources().getString(R.string.connection_error));
            Toast.makeText(getBaseContext(), getResources().getString(R.string.oops),
                    Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
