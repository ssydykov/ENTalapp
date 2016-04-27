package com.ent.saken2316.entalapp.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ent.saken2316.entalapp.Model.MyApplication;
import com.ent.saken2316.entalapp.Server.ServiceHandler;
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

public class ThrobberActivity extends ActionBarActivity {

    ProgressBar progressBar;
    Intent intent;
    String token, sessionId, categoryId;
    Handler handler;
    JSONArray questionsJSON = null;
    JSONObject message = null;
    TextView throbberText;
    Button button1;
    Context context;

    private String urlGlobal;
    private static String url = "mainapp/addtopool/";
    private static String url2 = "mainapp/killsearch/";
    private static String url3 = "mainapp/playwithbot/";

    private static final String TAG_MESSAGE = "message";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_GAME_ID = "game_id";
    private static final String TAG_OPPONENT_AVATAR = "opponent_avatar";
    private static final String TAG_OPPONENT_NAME = "opponent_name";
    private static final String TAG_OPPONENT_POINT = "opponent_points";
    private static final String TAG_QUESTIONS = "questions";
    private static final String TAG_QUESTION = "question";
    private static final String TAG_ANSWER1 = "answer_1";
    private static final String TAG_ANSWER2 = "answer_2";
    private static final String TAG_ANSWER3 = "answer_3";
    private static final String TAG_ANSWER4 = "answer_4";
    private static final String TAG_ANSWER = "correct_answer";

    Boolean success = false, isThrobber = true, isQuery = false, isBot = false;
    String gameID, opponentName, opponentAvatar, opponentPoint, categoryName, language;
    String questions[], answer1[], answer2[], answer3[], answer4[];
    int answer[], queryCounter = 0;
    ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_throbber);

        context = getApplicationContext();

        handler = new Handler();

        intent = getIntent();
        token = intent.getStringExtra("token");
        sessionId = intent.getStringExtra("sessionId");
        categoryId = intent.getStringExtra("categoryId");
        urlGlobal = ((MyApplication)this.getApplication()).getUrl();
        categoryName = intent.getStringExtra("category_name");
        getPref();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        throbberText = (TextView) findViewById(R.id.throbberText);
        button1 = (Button) findViewById(R.id.btnStop);
        button1.setVisibility(View.INVISIBLE);

        createStatusBar();

        new GetQuestions(context, url).execute();
    }

    private void getPref(){

        SharedPreferences sharedPreferencesSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        language = sharedPreferencesSettings.getString("language", "rus");
        if (language.equals("rus")){
            MyApplication.setLocaleRu(getApplicationContext());
        }
        else {
            MyApplication.setLocaleKk(getApplicationContext());
        }
    }

    private void createStatusBar(){
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

    private class GetQuestions extends AsyncTask<String, String, String> {

        Context context;
        String url;
        public GetQuestions(Context _context, String _url){
            this.context = _context;
            this.url = _url;
        }

        @Override
        protected void onCancelled() {

            Log.i("Http Response:", "Aborted");
        }

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("category_id", categoryId));
            params.add(new BasicNameValuePair("language", language));
            if (isBot){
                params.add(new BasicNameValuePair("friend_id", "0"));
                Log.e("Friend id", "0");
                Log.e("Category id", categoryId);
            }
            String[] arrayListResponse = sh.makeServiceCall(urlGlobal + url, ServiceHandler.POST,
                    params, token, sessionId);
            String jsonStr = arrayListResponse[2];
            Log.e("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    message = jsonObj.getJSONObject(TAG_MESSAGE);
                    success = message.getBoolean(TAG_SUCCESS);
                    gameID = message.getString(TAG_GAME_ID);
                    opponentName = message.getString(TAG_OPPONENT_NAME);
                    opponentAvatar = message.getString(TAG_OPPONENT_AVATAR);
//                            opponentCity = message.getString(TAG_OPPONENT_CITY);
                    opponentPoint = message.getString(TAG_OPPONENT_POINT);
                    questionsJSON = message.getJSONArray(TAG_QUESTIONS);

                    questions = new String[questionsJSON.length()];
                    answer1 = new String[questionsJSON.length()];
                    answer2 = new String[questionsJSON.length()];
                    answer3 = new String[questionsJSON.length()];
                    answer4 = new String[questionsJSON.length()];
                    answer = new int[questionsJSON.length()];

                    // looping through All Contacts
                    for (int i = 0; i < questionsJSON.length(); i++) {

                        JSONObject c = questionsJSON.getJSONObject(i);
                        questions[i] = c.getString(TAG_QUESTION);
                        answer1[i] = c.getString(TAG_ANSWER1);
                        answer2[i] = c.getString(TAG_ANSWER2);
                        answer3[i] = c.getString(TAG_ANSWER3);
                        answer4[i] = c.getString(TAG_ANSWER4);
                        answer[i] = c.getInt(TAG_ANSWER);
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

            isQuery = true;
            button1.setVisibility(View.VISIBLE);
            throbberText.setText(getResources().getString(R.string.search_opponent));
            if (!success && isThrobber && queryCounter < 1)
            {
                queryCounter++;
                Runnable task = new Runnable() {
                    public void run() {
                        new GetQuestions(context, url).execute();
                    }
                };
                worker.schedule(task, 5, TimeUnit.SECONDS);
            }
            else if (success && isThrobber)
            {
                isThrobber = false;
                Intent intent = new Intent(context, ReadyToPlayActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle b = new Bundle();
                b.putString("token", token);
                b.putString("sessionId", sessionId);
                b.putString("game_id", gameID);
                b.putString("opponent_name", opponentName);
                b.putString("opponent_avatar", opponentAvatar);
//                b.putString("opponent_city", opponentCity);
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
            else {

                onBot();
                queryCounter = 0;
            }
        }
    }
    public void onClickStop(View view){

        if (isQuery){
            worker.shutdownNow();
            new KillSearch(context).execute();
        }
        else {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.waiting),
                    Toast.LENGTH_SHORT).show();
        }
    }
    public void onBot(){

        isBot = true;
        worker.shutdownNow();
        new KillSearch(context).execute();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (isQuery){
                worker.shutdownNow();
                new KillSearch(context).execute();
                isQuery = false;
            }
            else {
                new GetQuestions(context, url).onCancelled();
                Toast.makeText(getBaseContext(), getResources().getString(R.string.waiting),
                        Toast.LENGTH_SHORT).show();
                Object activity = CategoriesActivity.class;
                Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("token", token);
                intent.putExtra("sessionId", sessionId);
                startActivity(intent);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class KillSearch extends AsyncTask<String, String, String> {

        Context context;
        public KillSearch(Context _context){
            this.context = _context;
        }

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("category_id", categoryId));
            String[] arrayListResponse2 = sh.makeServiceCall(urlGlobal + url2, ServiceHandler.POST,
                    params, token, sessionId);
            String jsonStr = arrayListResponse2[2];
            Log.e("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    message = jsonObj.getJSONObject(TAG_MESSAGE);
                    success = message.getBoolean(TAG_SUCCESS);

                    if (success) {
                        gameID = message.getString(TAG_GAME_ID);
                        opponentName = message.getString(TAG_OPPONENT_NAME);
                        opponentAvatar = message.getString(TAG_OPPONENT_AVATAR);
                        //                            opponentCity = message.getString(TAG_OPPONENT_CITY);
                        opponentPoint = message.getString(TAG_OPPONENT_POINT);
                        questionsJSON = message.getJSONArray(TAG_QUESTIONS);

                        questions = new String[questionsJSON.length()];
                        answer1 = new String[questionsJSON.length()];
                        answer2 = new String[questionsJSON.length()];
                        answer3 = new String[questionsJSON.length()];
                        answer4 = new String[questionsJSON.length()];
                        answer = new int[questionsJSON.length()];

                        // looping through All Contacts
                        for (int i = 0; i < questionsJSON.length(); i++) {

                            JSONObject c = questionsJSON.getJSONObject(i);
                            questions[i] = c.getString(TAG_QUESTION);
                            answer1[i] = c.getString(TAG_ANSWER1);
                            answer2[i] = c.getString(TAG_ANSWER2);
                            answer3[i] = c.getString(TAG_ANSWER3);
                            answer4[i] = c.getString(TAG_ANSWER4);
                            answer[i] = c.getInt(TAG_ANSWER);
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

            isQuery = false;
            if (!success && isThrobber && !isBot)
            {
                finish();
                isThrobber = false;
                Object activity = CategoriesActivity.class;
                Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("token", token);
                intent.putExtra("sessionId", sessionId);
                startActivity(intent);
            }
            else if (success && isThrobber)
            {
                isThrobber = false;
                Intent intent = new Intent(context, ReadyToPlayActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle b = new Bundle();
                b.putBoolean("isNotification", false);
                b.putString("token", token);
                b.putString("sessionId", sessionId);
                b.putString("game_id", gameID);
                b.putString("opponent_name", opponentName);
                b.putString("opponent_avatar", opponentAvatar);
//                b.putString("opponent_city", opponentCity);
                b.putString("opponent_point", opponentPoint);
                b.putStringArray("questions", questions);
                b.putStringArray("answer1", answer1);
                b.putStringArray("answer2", answer2);
                b.putStringArray("answer3", answer3);
                b.putStringArray("answer4", answer4);
                b.putIntArray("answer", answer);
                intent.putExtras(b);
                context.startActivity(intent);
            }
            else {
                new GetQuestions(context, url3).execute();
            }

        }
    }
}


