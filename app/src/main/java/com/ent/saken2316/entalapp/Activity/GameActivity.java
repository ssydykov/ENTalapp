package com.ent.saken2316.entalapp.Activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ent.saken2316.entalapp.Model.MyApplication;
import com.ent.saken2316.entalapp.Server.ServiceHandler;
import com.example.saken2316.entalapp.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class GameActivity extends ActionBarActivity {

    Intent intent;
    ProgressBar progressBar;
    ProgressDialog pDialog;
    Integer r = 0, g = 0, b = 256;
    TextView textView, textViewPoint, textViewTime, textViewNumber;
    LinearLayout linearLayout;
    Button btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4;
    ImageView imageView1, imageView2, imageViewTip;
//    Drawable drawable;
    String token, sessionId;
    Handler handler = new Handler();
    final int MENU_ALPHA_ID = 1, MENU_SCALE_ID = 2;
    Animation animation = null;
    Vibrator vibrator;
    Context context;
    AudioManager audio;

    String gameID, opponentName, opponentCity, opponentPoint, categoryName, avatar1, avatar2;
    String questions[], answer1[], answer2[], answer3[], answer4[], jsonStr;
    int progressStatus = 200, counter = 0, point = 0, answer[], userAnswer[], userPoint[];
    int DIALOG_FINISH = 1;
    boolean isProgress = true, buttonClicked = false, isInternet, isSetQuestion = true;

    private String urlGlobal;
    private static String url = "mainapp/gameend/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_game);
        isInternet = isNetworkAvailable();
        context = getApplicationContext();
        vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
        urlGlobal = ((MyApplication)this.getApplication()).getUrl();
        audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

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
        token = intent.getStringExtra("token");
        sessionId = intent.getStringExtra("sessionId");
        gameID = intent.getStringExtra("game_id");
        opponentName = intent.getStringExtra("opponent_name");
//        opponentCity = intent.getStringExtra("opponent_city");
        avatar2 = intent.getStringExtra("opponent_avatar");
        opponentPoint = intent.getStringExtra("opponent_point");
        questions = intent.getStringArrayExtra("questions");
        answer1 = intent.getStringArrayExtra("answer1");
        answer2 = intent.getStringArrayExtra("answer2");
        answer3 = intent.getStringArrayExtra("answer3");
        answer4 = intent.getStringArrayExtra("answer4");
        answer = intent.getIntArrayExtra("answer");
        categoryName = intent.getStringExtra("category_name");

        // Shared preferences values:
        SharedPreferences sharedPreferencesProfile = getSharedPreferences("user", 0);
        avatar1 = sharedPreferencesProfile.getString("avatar", "");
        getPref();

        userAnswer = new int[5];
        userPoint = new int[5];

        linearLayout = (LinearLayout) findViewById(R.id.linear_layout);
        textView = (TextView) findViewById(R.id.textView);
        textViewPoint = (TextView) findViewById(R.id.textViewPoint);
        textViewTime = (TextView) findViewById(R.id.textViewTime);
        textViewNumber = (TextView) findViewById(R.id.textViewNumber);
        btnAnswer1 = (Button) findViewById(R.id.answer1);
        btnAnswer2 = (Button) findViewById(R.id.answer2);
        btnAnswer3 = (Button) findViewById(R.id.answer3);
        btnAnswer4 = (Button) findViewById(R.id.answer4);
        imageView1 = (ImageView) findViewById(R.id.avatar1);
        imageView2 = (ImageView) findViewById(R.id.avatar2);
        imageViewTip = (ImageView) findViewById(R.id.imageTip);

        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar.setMax(200);
//        drawable = progressBar.getProgressDrawable();

        Glide.with(context)
                .load(avatar1)
                .bitmapTransform(new CropCircleTransformation(context))
                .into(imageView1);
        Glide.with(context)
                .load(avatar2)
                .bitmapTransform(new CropCircleTransformation(context))
                .into(imageView2);

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences mPrefs = getSharedPreferences("user", 0);
                SharedPreferences.Editor editor = mPrefs.edit();
                if (mPrefs.getBoolean("new", true)){
                    imageViewTip.setVisibility(View.VISIBLE);
                }
                editor.putBoolean("new", false);
                imageViewTip.setVisibility(View.GONE);
                if (counter != 4 && buttonClicked && !isSetQuestion) {
                    counter++;
                    r = 0;
                    b = 256;
                    progressStatus = 200;
                    isProgress = true;
                    buttonClicked = false;
                    setQuestion();
                    linearLayout.setVisibility(View.GONE);
                } else if (isInternet && buttonClicked && !isSetQuestion) {
                    new GameEnd().execute();
                }
            }
        });

        setQuestion();
    }

    private void getPref(){

        SharedPreferences sharedPreferencesSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String language = sharedPreferencesSettings.getString("language", "rus");
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
    public void setRightAnswer() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                switch (answer[counter]) {
                    case 1:
                        btnAnswer1.setBackground(getResources().getDrawable(R.drawable.button_game_green));
                        break;
                    case 2:
                        btnAnswer2.setBackground(getResources().getDrawable(R.drawable.button_game_green));
                        break;
                    case 3:
                        btnAnswer3.setBackground(getResources().getDrawable(R.drawable.button_game_green));
                        break;
                    case 4:
                        btnAnswer4.setBackground(getResources().getDrawable(R.drawable.button_game_green));
                        break;
                }
                textViewPoint.setText(Integer.toString(point));
                linearLayout.setVisibility(View.VISIBLE);
            }
        }, 10);
    }
    public void setQuestion(){

//        drawable.setColorFilter(new LightingColorFilter(0xFF000000, Color.rgb(r, g, b)));

        btnAnswer1.setBackground(getResources().getDrawable(R.drawable.button_game));
        btnAnswer2.setBackground(getResources().getDrawable(R.drawable.button_game));
        btnAnswer3.setBackground(getResources().getDrawable(R.drawable.button_game));
        btnAnswer4.setBackground(getResources().getDrawable(R.drawable.button_game));

        buttonClicked = true;
        isSetQuestion = true;
//        animation = AnimationUtils.loadAnimation(this, R.anim.alpha);
        textView.setText(Html.fromHtml(questions[counter]));
//        textView.startAnimation(animation);
        progressBar.setProgress(progressStatus);
//        drawable.setColorFilter(new LightingColorFilter(0xFF000000, Color.rgb(r, g, b)));
        textViewTime.setText(Double.toString(progressStatus / 10));
        textViewPoint.setText(Integer.toString(point));
        textViewNumber.setText(Integer.toString(counter + 1) + " из 5");
        btnAnswer1.setVisibility(View.INVISIBLE);
        btnAnswer2.setVisibility(View.INVISIBLE);
        btnAnswer3.setVisibility(View.INVISIBLE);
        btnAnswer4.setVisibility(View.INVISIBLE);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

//                animation = AnimationUtils.loadAnimation(GameActivity.this, R.anim.alpha);

                btnAnswer1.setText(answer1[counter]);
                btnAnswer2.setText(answer2[counter]);
                btnAnswer3.setText(answer3[counter]);
                btnAnswer4.setText(answer4[counter]);

                btnAnswer1.setVisibility(View.VISIBLE);
                btnAnswer2.setVisibility(View.VISIBLE);
                btnAnswer3.setVisibility(View.VISIBLE);
                btnAnswer4.setVisibility(View.VISIBLE);

//                btnAnswer1.startAnimation(animation);
//                btnAnswer2.startAnimation(animation);
//                btnAnswer3.startAnimation(animation);
//                btnAnswer4.startAnimation(animation);

            }
        }, 1000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                buttonClicked = false;
                isSetQuestion = false;

                new Thread(new Runnable() {
                    public void run() {
                        while (progressStatus > 0 && isProgress) {
                            progressStatus -= 1;
                            // Update the progress bar and display the
                            //current value in the text view
                            handler.post(new Runnable() {
                                public void run() {
//                                    r += 256 / 100;
//                                    b -= 256 / 100;
                                    progressBar.setProgress(progressStatus);
//                                    drawable.setColorFilter(new LightingColorFilter(0xFF000000, Color.rgb(r, g, b)));
                                    if (progressStatus != 0){
                                        textViewTime.setText(Double.toString(progressStatus / 10 + 1));
                                    }
                                    else{
                                        textViewTime.setText("0");
                                    }
                                }
                            });
                            try {
                                // Sleep for 100 milliseconds.
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        buttonClicked = true;
                        setRightAnswer();
                    }
                }).start();
            }
        }, 3000);
    }

    public void onClickAnswer1(View view){

        if (!buttonClicked) {
            isProgress = false;
            userAnswer[counter] = 1;
            if (answer[counter] != 1) {
                switch (audio.getRingerMode())
                {
                    case AudioManager.RINGER_MODE_NORMAL:
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.wrong);
                        mp.start();
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        // Device is on Silent mode.
                        // you should not play sound now.
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        // Device is on Vibrate/Meeting mode.
                        // you should not play sound but you can make vibrate device (if you want).
                        break;
                }
                btnAnswer1.setBackground(getResources().getDrawable(R.drawable.button_game_red));
                vibrator.vibrate(250);
                userPoint[counter] = 0;
            } else {
                switch (audio.getRingerMode())
                {
                    case AudioManager.RINGER_MODE_NORMAL:
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.correct);
                        mp.start();
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        // Device is on Silent mode.
                        // you should not play sound now.
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        // Device is on Vibrate/Meeting mode.
                        // you should not play sound but you can make vibrate device (if you want).
                        break;
                }
                point += progressStatus / 10 + 1;
                userPoint[counter] = progressStatus / 10 + 1;
            }
        }
        buttonClicked = true;
//        Log.e("Counter", Integer.toString(counter));
//        Log.e("Point", Integer.toString(userPoint[counter]));
//        Log.e("Answer", Integer.toString(userAnswer[counter]));
    }
    public void onClickAnswer2(View view){

        if (!buttonClicked) {
            isProgress = false;
            userAnswer[counter] = 2;
            if (answer[counter] != 2) {
                switch (audio.getRingerMode())
                {
                    case AudioManager.RINGER_MODE_NORMAL:
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.wrong);
                        mp.start();
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        // Device is on Silent mode.
                        // you should not play sound now.
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        // Device is on Vibrate/Meeting mode.
                        // you should not play sound but you can make vibrate device (if you want).
                        break;
                }
                btnAnswer2.setBackground(getResources().getDrawable(R.drawable.button_game_red));
                vibrator.vibrate(250);
                userPoint[counter] = 0;
            } else {
                switch (audio.getRingerMode())
                {
                    case AudioManager.RINGER_MODE_NORMAL:
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.correct);
                        mp.start();
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        // Device is on Silent mode.
                        // you should not play sound now.
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        // Device is on Vibrate/Meeting mode.
                        // you should not play sound but you can make vibrate device (if you want).
                        break;
                }
                point += progressStatus / 10 + 1;
                userPoint[counter] = progressStatus / 10 + 1;
            }
        }
        buttonClicked = true;;
//        Log.e("Counter", Integer.toString(counter));
//        Log.e("Point", Integer.toString(userPoint[counter]));
//        Log.e("Answer", Integer.toString(userAnswer[counter]));
    }
    public void onClickAnswer3(View view){


        if (!buttonClicked) {
            isProgress = false;
            userAnswer[counter] = 3;
            if (answer[counter] != 3) {
                switch (audio.getRingerMode())
                {
                    case AudioManager.RINGER_MODE_NORMAL:
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.wrong);
                        mp.start();
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        // Device is on Silent mode.
                        // you should not play sound now.
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        // Device is on Vibrate/Meeting mode.
                        // you should not play sound but you can make vibrate device (if you want).
                        break;
                }
                btnAnswer3.setBackground(getResources().getDrawable(R.drawable.button_game_red));
                vibrator.vibrate(250);
                userPoint[counter] = 0;
            } else {
                switch (audio.getRingerMode())
                {
                    case AudioManager.RINGER_MODE_NORMAL:
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.correct);
                        mp.start();
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        // Device is on Silent mode.
                        // you should not play sound now.
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        // Device is on Vibrate/Meeting mode.
                        // you should not play sound but you can make vibrate device (if you want).
                        break;
                }
                point += progressStatus / 10 + 1;
                userPoint[counter] = progressStatus / 10 + 1;
            }
        }
        buttonClicked = true;;
//        Log.e("Counter", Integer.toString(counter));
//        Log.e("Point", Integer.toString(userPoint[counter]));
//        Log.e("Answer", Integer.toString(userAnswer[counter]));
    }
    public void onClickAnswer4(View view){

        if (!buttonClicked) {
            isProgress = false;
            userAnswer[counter] = 4;
            if (answer[counter] != 4) {
                switch (audio.getRingerMode())
                {
                    case AudioManager.RINGER_MODE_NORMAL:
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.wrong);
                        mp.start();
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        // Device is on Silent mode.
                        // you should not play sound now.
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        // Device is on Vibrate/Meeting mode.
                        // you should not play sound but you can make vibrate device (if you want).
                        break;
                }
                btnAnswer4.setBackground(getResources().getDrawable(R.drawable.button_game_red));
                vibrator.vibrate(250);
                userPoint[counter] = 0;
            } else {
                switch (audio.getRingerMode())
                {
                    case AudioManager.RINGER_MODE_NORMAL:
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.correct);
                        mp.start();
                        break;
                    case AudioManager.RINGER_MODE_SILENT:
                        // Device is on Silent mode.
                        // you should not play sound now.
                        break;
                    case AudioManager.RINGER_MODE_VIBRATE:
                        // Device is on Vibrate/Meeting mode.
                        // you should not play sound but you can make vibrate device (if you want).
                        break;
                }
                point += progressStatus / 10 + 1;
                userPoint[counter] = progressStatus / 10 + 1;
            }
        }
        buttonClicked = true;
//        Log.e("Counter", Integer.toString(counter));
//        Log.e("Point", Integer.toString(userPoint[counter]));
//        Log.e("Answer", Integer.toString(userAnswer[counter]));
    }
    public void showTips(){

        if (counter == 0){

            Toast.makeText(getBaseContext(), getResources().getString(R.string.tip),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class GameEnd extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(GameActivity.this);
            pDialog.setMessage(getResources().getString(R.string.wait_result));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("game_id", gameID));
            params.add(new BasicNameValuePair("answer1", Integer.toString(userAnswer[0])));
            params.add(new BasicNameValuePair("answer2", Integer.toString(userAnswer[1])));
            params.add(new BasicNameValuePair("answer3", Integer.toString(userAnswer[2])));
            params.add(new BasicNameValuePair("answer4", Integer.toString(userAnswer[3])));
            params.add(new BasicNameValuePair("answer5", Integer.toString(userAnswer[4])));
            params.add(new BasicNameValuePair("point1", Integer.toString(userPoint[0])));
            params.add(new BasicNameValuePair("point2", Integer.toString(userPoint[1])));
            params.add(new BasicNameValuePair("point3", Integer.toString(userPoint[2])));
            params.add(new BasicNameValuePair("point4", Integer.toString(userPoint[3])));
            params.add(new BasicNameValuePair("point5", Integer.toString(userPoint[4])));
            params.add(new BasicNameValuePair("total", Integer.toString(point)));

            String[] arrayListResponse = sh.makeServiceCall(urlGlobal + url, ServiceHandler.POST, params, token, sessionId);
            jsonStr = arrayListResponse[2];

            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            Object activity = ResultActivity.class;
            Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("token", token);
            intent.putExtra("sessionId", sessionId);
            intent.putExtra("gameId", gameID);
            intent.putExtra("category_name", categoryName);
            intent.putExtra("my_total", Integer.toString(point));
            intent.putExtra("opponent_name", opponentName);
            intent.putExtra("opponent_avatar", avatar2);

            if (jsonStr != null){

                intent.putExtra("query_success", true);
            }
            else {

                intent.putExtra("query_success", false);
                intent.putExtra("answer1", Integer.toString(userAnswer[0]));
                intent.putExtra("answer2", Integer.toString(userAnswer[1]));
                intent.putExtra("answer3", Integer.toString(userAnswer[2]));
                intent.putExtra("answer4", Integer.toString(userAnswer[3]));
                intent.putExtra("answer5", Integer.toString(userAnswer[4]));
                intent.putExtra("point1", Integer.toString(userPoint[0]));
                intent.putExtra("point2", Integer.toString(userPoint[1]));
                intent.putExtra("point3", Integer.toString(userPoint[2]));
                intent.putExtra("point4", Integer.toString(userPoint[3]));
                intent.putExtra("point5", Integer.toString(userPoint[4]));
            }

            startActivity(intent);

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //do whatever you need for the hardware 'back' button
            showDialog(DIALOG_FINISH);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_FINISH)
        {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle(R.string.finish);
            adb.setMessage(R.string.finish_message);
            adb.setIcon(android.R.drawable.ic_dialog_info);
            adb.setPositiveButton(R.string.yes, myClickListener);
            adb.setNegativeButton(R.string.cancel, myClickListener);
            return adb.create();
        }
        return super.onCreateDialog(id);
    }
    // Dialog onClick listener:
    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {

                case Dialog.BUTTON_POSITIVE:

                    point = 0;
                    new GameEnd().execute();
                    break;
                case Dialog.BUTTON_NEGATIVE:

                    break;
            }
        }
    };
}



