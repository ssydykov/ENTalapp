package com.ent.saken2316.entalapp.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ent.saken2316.entalapp.Server.ServiceHandler;
import com.example.saken2316.entalapp.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class ReadyToPlayActivity extends AppCompatActivity {

    private static String url = "http://env-3315080.j.dnr.kz/mainapp/answertochallenge/";

    JSONObject message = null;
    JSONArray questionsJSON = null;

    Intent intent;
    ImageView avatar1, avatar2;
    ProgressBar progressBar;
    TextView username, usernameop;
    Button button;
    Context context;

    String token, sessionId;
    String gameID, opponentName, opponentAvatar, opponentCity, opponentPoint, categoryName;
    String questions[], answer1[], answer2[], answer3[], answer4[];
    Boolean isNotification;
    int answer[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready_to_play);

        context = getApplicationContext();

        // Intent values:
        intent = getIntent();
        isNotification = intent.getBooleanExtra("isNotification", false);
        gameID = intent.getStringExtra("game_id");
        categoryName = intent.getStringExtra("category_name");

        avatar1 = (ImageView) findViewById(R.id.avatar1);
        avatar2 = (ImageView) findViewById(R.id.avatar2);
        username = (TextView) findViewById(R.id.username);
        usernameop = (TextView) findViewById(R.id.usernameop);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        button = (Button) findViewById(R.id.button);

        if (isNotification){

            SharedPreferences mPrefs = getSharedPreferences("user", 0);
            token = mPrefs.getString("token", "");
            sessionId = mPrefs.getString("sessionId", "");
            Log.e("Hello", "Notification");
            Log.e("TOKEN", token);
            Log.e("Game Id", gameID);

            new AnswerToChallenge().execute();

        } else {

            token = intent.getStringExtra("token");
            sessionId = intent.getStringExtra("sessionId");
            opponentName = intent.getStringExtra("opponent_name");
//        opponentCity = intent.getStringExtra("opponent_city");
            opponentAvatar = intent.getStringExtra("opponent_avatar");
            opponentPoint = intent.getStringExtra("opponent_point");
            questions = intent.getStringArrayExtra("questions");
            answer1 = intent.getStringArrayExtra("answer1");
            answer2 = intent.getStringArrayExtra("answer2");
            answer3 = intent.getStringArrayExtra("answer3");
            answer4 = intent.getStringArrayExtra("answer4");
            answer = intent.getIntArrayExtra("answer");

            new GetInfo().execute();
        }
    }

    private class GetInfo extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {

            return null;
        }

        protected void onPostExecute(String result) {

            progressBar.setVisibility(View.INVISIBLE);
            button.setVisibility(View.VISIBLE);

            SharedPreferences sharedPreferences = getSharedPreferences("user", 0);

            username.setText(sharedPreferences.getString("first_name", ""));
            usernameop.setText(opponentName);

            Glide.with(context)
                    .load(sharedPreferences.getString("avatar", ""))
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(avatar1);
            Glide.with(context)
                    .load(opponentAvatar)
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(avatar2);
        }
    }
    private class AnswerToChallenge extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("answer", "2"));
            params.add(new BasicNameValuePair("game_id", gameID));
            Log.e("TOKEN", token);
            String[] arrayListResponse = sh.makeServiceCall(url, ServiceHandler.POST,
                    params, token, sessionId);
            String jsonStr = arrayListResponse[2];
            Log.e("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    message = jsonObj.getJSONObject("message");
                    Boolean success = message.getBoolean("success");
                    if (success){
                        gameID = message.getString("game_id");
                        opponentName = message.getString("opponent_name");
                        opponentAvatar = message.getString("opponent_avatar");
                        opponentPoint = message.getString("opponent_points");
                        questionsJSON = message.getJSONArray("questions");

                        questions = new String[questionsJSON.length()];
                        answer1 = new String[questionsJSON.length()];
                        answer2 = new String[questionsJSON.length()];
                        answer3 = new String[questionsJSON.length()];
                        answer4 = new String[questionsJSON.length()];
                        answer = new int[questionsJSON.length()];

                        // looping through All Contacts
                        for (int i = 0; i < questionsJSON.length(); i++) {

                            JSONObject c = questionsJSON.getJSONObject(i);
                            questions[i] = c.getString("question");
                            answer1[i] = c.getString("answer_1");
                            answer2[i] = c.getString("answer_2");
                            answer3[i] = c.getString("answer_3");
                            answer4[i] = c.getString("answer_4");
                            answer[i] = c.getInt("correct_answer");
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

            progressBar.setVisibility(View.INVISIBLE);
            button.setVisibility(View.VISIBLE);

            SharedPreferences sharedPreferences = getSharedPreferences("user", 0);
            username.setText(sharedPreferences.getString("first_name", ""));
            usernameop.setText(opponentName);

            Glide.with(context)
                    .load(sharedPreferences.getString("avatar", ""))
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(avatar1);
            Glide.with(context)
                    .load(opponentAvatar)
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(avatar2);
        }
    }

    public void onClickGame(View view){

        Intent intent = new Intent(context, GameActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle b = new Bundle();
        b.putString("token", token);
        b.putString("sessionId", sessionId);
        b.putString("game_id", gameID);
        b.putString("opponent_name", opponentName);
        b.putString("opponent_avatar", opponentAvatar);
//        b.putString("opponent_city", opponentCity);
        b.putString("opponent_point", opponentPoint);
        b.putStringArray("questions", questions);
        b.putStringArray("answer1", answer1);
        b.putStringArray("answer2", answer2);
        b.putStringArray("answer3", answer3);
        b.putStringArray("answer4", answer4);
        b.putIntArray("answer", answer);
        b.putString("category_name", categoryName);
        intent.putExtras(b);
        context.startActivity(intent);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ready_to_play, menu);
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
