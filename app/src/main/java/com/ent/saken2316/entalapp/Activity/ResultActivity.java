package com.ent.saken2316.entalapp.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ent.saken2316.entalapp.Model.MyApplication;
import com.ent.saken2316.entalapp.Server.ServiceHandler;
import com.example.saken2316.entalapp.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class ResultActivity extends ActionBarActivity {

    private String urlGlobal;
    private static String url = "mainapp/gameresult/";
    private static String url2 = "mainapp/iwanttoplaywithfriend/";
    private static String url3 = "mainapp/playwithbot/";
    private static String url4 = "mainapp/gameend/";

    JSONObject myResult = null;
    JSONObject message = null;
    JSONArray questionsJSON = null;

    Intent intent;
//    Toolbar toolbar;
    Animation animation = null;
    ImageView imageView1, imageView2;
    Button buttonBack;
    RelativeLayout buttonShare;
    TextView textViewName1, textViewName2, textViewStatus, textViewResult,
            textViewPoint1, textViewPoint2, textViewRating, textViewSubject,
            textEntalapp;
    ProgressBar progressBar;
    ProgressDialog pDialog;
    String userName, opponentName, opponentTotal, opponentPoint, myTotal, jsonStr,
            categoryName, date, gameId, category_id, friend_id, avatar1, avatar2;
    String questions[], answer1[], answer2[], answer3[], answer4[],
            q_answer1, q_answer2, q_answer3, q_answer4, q_answer5,
            point1, point2, point3, point4, point5;
    int right_answer[], rating, queryCount = 0;
    String token, sessionId, language;
    Boolean success = false, query_success, win = false;
    Context context;
    LinearLayout linearLayout, linearLayoutRating;
    ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_result);
        context = getApplicationContext();

        // Intent values:
        intent = getIntent();
        token = intent.getStringExtra("token");
        sessionId = intent.getStringExtra("sessionId");
        urlGlobal = ((MyApplication)this.getApplication()).getUrl();
        gameId = intent.getStringExtra("gameId");
        categoryName = intent.getStringExtra("category_name");
        myTotal = intent.getStringExtra("my_total");
        opponentName = intent.getStringExtra("opponent_name");
        avatar2 = intent.getStringExtra("opponent_avatar");
        query_success = intent.getBooleanExtra("query_success", true);
        getPref();

        // Shared preferences values:
        SharedPreferences sharedPreferencesProfile = getSharedPreferences("user", 0);
        userName = sharedPreferencesProfile.getString("first_name", "");
        avatar1 = sharedPreferencesProfile.getString("avatar", "");

        // View values:
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        textViewName1 = (TextView) findViewById(R.id.textViewName1);
        textViewName2 = (TextView) findViewById(R.id.textViewName2);
        textViewPoint1 = (TextView) findViewById(R.id.textViewPoint1);
        textViewPoint2 = (TextView) findViewById(R.id.textViewPoint2);
        textViewRating = (TextView) findViewById(R.id.textViewRating);
        textViewResult = (TextView) findViewById(R.id.textViewResult);
        textViewSubject = (TextView) findViewById(R.id.textViewSubject);
        textEntalapp = (TextView) findViewById(R.id.text_entalapp);
        imageView1 = (ImageView) findViewById(R.id.avatar1);
        imageView2 = (ImageView) findViewById(R.id.avatar2);
        buttonShare = (RelativeLayout) findViewById(R.id.buttonShare);
        buttonBack = (Button) findViewById(R.id.buttonBack);
        linearLayout = (LinearLayout) findViewById(R.id.linear_layout);
        linearLayoutRating = (LinearLayout) findViewById(R.id.linearLayoutRating);

        linearLayoutRating.setVisibility(View.INVISIBLE);
        textEntalapp.setVisibility(View.INVISIBLE);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Object activity = MyProfileActivity.class;
                Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("token", token);
                intent.putExtra("sessionId", sessionId);
                startActivity(intent);
            }
        });

        // Create toolbar:
//        toolbarBuilder();

        // Create status bar:
        statusBarBuilder();

//        toolbar.setTitle(categoryName);
        textViewPoint1.setText(myTotal);
        textViewName1.setText(userName);
        textViewName2.setText(opponentName);
        textViewSubject.setText(categoryName);
        textViewPoint2.setText("...");

        Glide.with(context)
                .load(avatar1)
                .bitmapTransform(new CropCircleTransformation(context))
                .into(imageView1);
        Glide.with(context)
                .load(avatar2)
                .bitmapTransform(new CropCircleTransformation(context))
                .into(imageView2);

        if (isNetworkAvailable() && query_success){

            new GetMyResult().execute();
        }
        else if (isNetworkAvailable()){

            q_answer1 = intent.getStringExtra("answer1");
            q_answer2 = intent.getStringExtra("answer2");
            q_answer3 = intent.getStringExtra("answer3");
            q_answer4 = intent.getStringExtra("answer4");
            q_answer5 = intent.getStringExtra("answer5");
            point1 = intent.getStringExtra("point1");
            point2 = intent.getStringExtra("point2");
            point3 = intent.getStringExtra("point3");
            point4 = intent.getStringExtra("point4");
            point5 = intent.getStringExtra("point5");

            new GameEnd().execute();
        }
        else {

            progressBar.setVisibility(View.INVISIBLE    );
            textViewStatus.setText(getResources().getString(R.string.connection_error));
        }
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

    private class GameEnd extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            progressBar.setVisibility(View.VISIBLE);
            textViewStatus.setText(getResources().getString(R.string.wait));
        }

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("game_id", gameId));
            params.add(new BasicNameValuePair("answer1", q_answer1));
            params.add(new BasicNameValuePair("answer2", q_answer2));
            params.add(new BasicNameValuePair("answer3", q_answer3));
            params.add(new BasicNameValuePair("answer4", q_answer4));
            params.add(new BasicNameValuePair("answer5", q_answer5));
            params.add(new BasicNameValuePair("point1", point1));
            params.add(new BasicNameValuePair("point2", point2));
            params.add(new BasicNameValuePair("point3", point3));
            params.add(new BasicNameValuePair("point4", point4));
            params.add(new BasicNameValuePair("point5", point5));
            params.add(new BasicNameValuePair("total", myTotal));

            String[] arrayListResponse = sh.makeServiceCall(urlGlobal + url4, ServiceHandler.POST, params, token, sessionId);
            jsonStr = arrayListResponse[2];

            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            progressBar.setVisibility(View.INVISIBLE);

            Runnable task = new Runnable() {
                public void run() {

                    if (jsonStr == null && queryCount < 5){

                        queryCount++;
                        new GameEnd().execute();
                    }
                    else {

                        new GetMyResult().execute();
                    }
                }
            };
            worker.schedule(task, 5, TimeUnit.SECONDS);
        }
    }
    private class GetMyResult extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("game_id", gameId));
            String[] arrayListResponse = sh.makeServiceCall(urlGlobal + url, ServiceHandler.POST,
                    params, token, sessionId);
            jsonStr = arrayListResponse[2];
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
                    rating = myResult.getInt("my_pts");

                    Log.e("Success", Boolean.toString(success));
                    if (success){
                        opponentTotal = myResult.getString("opponent_points");
                        Log.e("Opponent total", opponentTotal);
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

//            toolbar.setTitle(categoryName);
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

            if (jsonStr == null) {

                progressBar.setVisibility(View.INVISIBLE);
                textViewStatus.setText(getResources().getString(R.string.connection_error));
                textViewStatus.setTextSize(14);
            }
            else if (success && queryCount < 5){

                progressBar.setVisibility(View.INVISIBLE);
                textViewStatus.setVisibility(View.INVISIBLE);
                animation = AnimationUtils.loadAnimation(ResultActivity.this, R.anim.alpha);

                textViewRating.setText(Integer.toString(rating));
                Log.e("My point", myTotal);
                Log.e("Opponent point", opponentTotal);
                if (Integer.parseInt(myTotal) > Integer.parseInt(opponentTotal)){

                    //WIN
                    linearLayout.setBackground(getResources().getDrawable(R.drawable.card_head_green_shape));
                    textViewResult.setText(getResources().getString(R.string.win));
                    textViewPoint1.setTextColor(getResources().getColor(R.color.game_green));
                    textViewPoint2.setTextColor(getResources().getColor(R.color.game_red));
                    win = true;
                }
                else if (Integer.parseInt(myTotal) < Integer.parseInt(opponentTotal)){

                    //LOSE
                    linearLayout.setBackground(getResources().getDrawable(R.drawable.card_head_red_shape));
                    textViewResult.setText(getResources().getString(R.string.lose));
                    textViewPoint1.setTextColor(getResources().getColor(R.color.game_red));
                    textViewPoint2.setTextColor(getResources().getColor(R.color.game_green));
                }
                else {

                    linearLayout.setBackground(getResources().getDrawable(R.drawable.card_head_shape));
                    textViewResult.setText(getResources().getString(R.string.draw));
                    textViewPoint1.setTextColor(getResources().getColor(R.color.game_blue));
                    textViewPoint2.setTextColor(getResources().getColor(R.color.game_blue));
                }
                if (rating > 0){
                    textViewRating.setTextColor(getResources().getColor(R.color.game_green));
                    textViewRating.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_upward_black_18dp, 0);
                }
                else if(rating < 0){

                    textViewRating.setTextColor(getResources().getColor(R.color.game_red));
                    textViewRating.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_downward_black_18dp, 0);
                }
                else {

                    textViewRating.setTextColor(getResources().getColor(R.color.primary_text));
                    textViewRating.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }

                linearLayoutRating.setVisibility(View.VISIBLE);
                linearLayoutRating.startAnimation(animation);
                linearLayout.startAnimation(animation);
            }
            else {

                progressBar.setVisibility(View.VISIBLE);
                textViewStatus.setText(getResources().getString(R.string.wait));
                Runnable task = new Runnable() {
                    public void run() {

                        if (isNetworkAvailable() && queryCount < 5){

                            queryCount++;
                            new GetMyResult().execute();
                        }
                    }
                };
                worker.schedule(task, 5, TimeUnit.SECONDS);

                if (isNetworkAvailable() && queryCount == 5){

                    progressBar.setVisibility(View.INVISIBLE);
                    textViewStatus.setText(getResources().getString(R.string.done));
                    textViewStatus.setTextSize(15);
                }
                else if (!isNetworkAvailable()){

                    worker.shutdownNow();
                    progressBar.setVisibility(View.INVISIBLE);
                    textViewStatus.setText(getResources().getString(R.string.connection_error));
                    textViewStatus.setTextSize(15);
                }
            }

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            worker.shutdownNow();
            worker.shutdown();
            Object activity = MyProfileActivity.class;
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

        if (id == R.id.action_refresh && isNetworkAvailable()) {

            progressBar.setVisibility(View.VISIBLE);
            new GetMyResult().execute();
            return true;
        } else {
            textViewStatus.setText(getResources().getString(R.string.connection_error));
            textViewStatus.setTextSize(15);
        }

        return super.onOptionsItemSelected(item);
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
            pDialog.setMessage(getResources().getString(R.string.wait_notification));
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("category_id", category_id));
            params.add(new BasicNameValuePair("friend_id", friend_id));
            params.add(new BasicNameValuePair("language", language));
            String[] arrayListResponse;
            if (friend_id.equals("1") || friend_id.equals("2") || friend_id.equals("3") || friend_id.equals("4") ||
                    friend_id.equals("5") || friend_id.equals("6")){
                arrayListResponse = sh.makeServiceCall(urlGlobal + url3, ServiceHandler.POST,
                        params, token, sessionId);
            }
            else {
                arrayListResponse = sh.makeServiceCall(urlGlobal + url2, ServiceHandler.POST,
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

            if (jsonStr != null) {

                Object activity = ReadyToPlayActivity.class;
                Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("token", token);
                intent.putExtra("sessionId", sessionId);
                intent.putExtra("game_id", gameId);
                intent.putExtra("opponent_name", opponentName);
                intent.putExtra("opponent_avatar", avatar2);
                intent.putExtra("opponent_point", opponentPoint);
                intent.putExtra("questions", questions);
                intent.putExtra("answer1", answer1);
                intent.putExtra("answer2", answer2);
                intent.putExtra("answer3", answer3);
                intent.putExtra("answer4", answer4);
                intent.putExtra("answer", right_answer);
                startActivity(intent);
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


    public void onClickShare(View view){

        textEntalapp.setVisibility(View.VISIBLE);

        String text = "";
        if (win)
        {
            text = "Я выиграл в приложении ENTalapp. Попробуй и ты! " +
                    "Ссылка play market: https://play.google.com/store/apps/details?id=com.ent.saken2316.entalapp&hl=ru";
        }
        else
        {
            text = "Я нашел крутое приложение по подготовке к ЕНТ. Попробуй и ты! " +
                    "Ссылка play market: https://play.google.com/store/apps/details?id=com.ent.saken2316.entalapp&hl=ru";
        }
        takeScreenshot(text);
    }
    private void takeScreenshot(String text) {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {

            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/*");
            Uri uri = Uri.fromFile(imageFile);
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(share, "Share to"));

            textEntalapp.setVisibility(View.GONE);

        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

}
