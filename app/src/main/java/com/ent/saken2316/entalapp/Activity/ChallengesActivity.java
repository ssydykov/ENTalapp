package com.ent.saken2316.entalapp.Activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ent.saken2316.entalapp.Adapter.ChallengesAdapter;
import com.ent.saken2316.entalapp.Model.MyApplication;
import com.ent.saken2316.entalapp.Model.Person;
import com.ent.saken2316.entalapp.Server.ServiceHandler;
import com.example.saken2316.entalapp.R;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ChallengesActivity extends AppCompatActivity {

    ListView listViewChallenges;
    Intent intent;
    Toolbar toolbar;
    List<Person> challengesList;
    ProgressBar progressBar;
    ProgressDialog pDialog;
    TextView textView;
    Button updateButton;

    int item;
    String token, sessionId, jsonStr, answer;

    private String urlGlobal;
    private static String url = "mainapp/whochallengemeall/";
    private static String url2 = "mainapp/logout/";
    private static String url3 = "mainapp/answertochallenge/";

    JSONObject message = null;
    JSONArray questionsJSON = null;

    String gameID, opponentName, opponentAvatar, opponentPoint;
    String answer1[], answer2[], answer3[], answer4[], questions[];
    int right_answer[];
    boolean success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);

        intent = getIntent();
        token = intent.getStringExtra("token");
        sessionId = intent.getStringExtra("sessionId");
        urlGlobal = ((MyApplication)this.getApplication()).getUrl();
        getPref();

        // Create tool bar:
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create toolbar:
        toolbarBuilder();

        // Create drawer:
        drawerBuilder();

        listViewChallenges = (ListView) findViewById(R.id.listView);

        textView = (TextView) findViewById(R.id.textView);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        updateButton = (Button) findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetChallenges().execute();
                updateButton.setVisibility(View.INVISIBLE);
            }
        });

        // Get My Games:
        challengesList = ((MyApplication)this.getApplication()).getChallengesList();
        if (challengesList == null){

            progressBar.setVisibility(View.VISIBLE);
            textView.setText(getResources().getString(R.string.connection_to_games));
            textView.setVisibility(View.VISIBLE);
            listViewChallenges.setVisibility(View.INVISIBLE);
        }
        else{

            setChallenges();
        }
        new GetChallenges().execute();
    }


    private class GetChallenges extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(ChallengesActivity.this);
            pDialog.setMessage("Загрузка вызовов");
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            String[] arrayListResponse = sh.makeServiceCall(urlGlobal + url, ServiceHandler.GET,
                    null, token, sessionId);
            jsonStr = arrayListResponse[2];
            Log.e("Response: ", "> " + jsonStr);

            if (jsonStr != null)
                challengesList = parseJson(jsonStr);

            return null;
        }

        protected void onPostExecute(final String result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.INVISIBLE);
            if (pDialog.isShowing())
                pDialog.dismiss();


            if (jsonStr == null){

                listViewChallenges.setVisibility(View.INVISIBLE);
                updateButton.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                textView.setText(getResources().getString(R.string.connection_error));
            }
            else if (!challengesList.isEmpty()){

                setChallenges();
            }
            else {

                listViewChallenges.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.VISIBLE);
                textView.setText(getResources().getString(R.string.no_challenges));
            }
        }
    }
    private void setChallenges(){

        ((MyApplication) ChallengesActivity.this.getApplication()).setChallengesList(challengesList);

        listViewChallenges.setVisibility(View.VISIBLE);
        textView.setVisibility(View.INVISIBLE);

        listViewChallenges.setAdapter(new ChallengesAdapter(ChallengesActivity.this, challengesList));
        listViewChallenges.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                item = position;
                showDialog(1);
            }
        });
    }
    protected Dialog onCreateDialog(int id) {
        if (id == 1)
        {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle(challengesList.get(item).getCategoryName());
            adb.setMessage(getResources().getString(R.string.accept_challenge) + " " + challengesList.get(item).getFull_name());
            adb.setIcon(android.R.drawable.ic_dialog_info);
            adb.setPositiveButton(R.string.accept, myClickListener);
            adb.setNegativeButton(R.string.refuse, myClickListener);
            adb.setNeutralButton(R.string.cancel, myClickListener);
            return adb.create();
        }
        return super.onCreateDialog(id);
    }

    // Dialog myClickListener
    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {

                case Dialog.BUTTON_POSITIVE:
                    answer = "2";
                    new AnswerToChallenge().execute();
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    answer = "1";
                    new AnswerToChallenge().execute();
                    break;
                case Dialog.BUTTON_NEUTRAL:
                    break;
            }
        }
    };
    private class AnswerToChallenge extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(ChallengesActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("answer", answer));
            params.add(new BasicNameValuePair("game_id", challengesList.get(item).getGameId()));
            String[] arrayListResponse = sh.makeServiceCall(urlGlobal + url3, ServiceHandler.POST,
                    params, token, sessionId);
            jsonStr = arrayListResponse[2];
            Log.e("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    message = jsonObj.getJSONObject("message");
                    success = message.getBoolean("success");
                    Log.e("Success", Boolean.toString(success));
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
            if (pDialog.isShowing())
                pDialog.dismiss();

            Log.e("Success", Boolean.toString(success));
            if (success && jsonStr != null){

                Object activity = ReadyToPlayActivity.class;
                Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("token", token);
                intent.putExtra("sessionId", sessionId);
                intent.putExtra("game_id", gameID);
                intent.putExtra("opponent_name", opponentName);
                intent.putExtra("opponent_avatar", opponentAvatar);
                intent.putExtra("opponent_point", opponentPoint);
                intent.putExtra("questions", questions);
                intent.putExtra("answer1", answer1);
                intent.putExtra("answer2", answer2);
                intent.putExtra("answer3", answer3);
                intent.putExtra("answer4", answer4);
                intent.putExtra("answer", right_answer);
                intent.putExtra("category_name", challengesList.get(item).getCategoryName());
                startActivity(intent);
            }
            else {

                new GetChallenges().execute();
            }

        }
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

    private void toolbarBuilder(){

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

    private void drawerBuilder(){

        new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withHeader(R.layout.drawer_header)
                .withSelectedItem(4)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_user).withIcon(FontAwesome.Icon.faw_user),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_rating).withIcon(FontAwesome.Icon.faw_list),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_games).withIcon(FontAwesome.Icon.faw_gamepad).withIdentifier(1),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_friends).withIcon(FontAwesome.Icon.faw_users),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_challenges).withIcon(FontAwesome.Icon.faw_bell),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_about).withIcon(FontAwesome.Icon.faw_info),
//                        new SectionDrawerItem().withName(R.string.drawer_item_settings),
//                        new SecondaryDrawerItem().withName(R.string.drawer_item_help).withIcon(FontAwesome.Icon.faw_cog),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_logout).withIcon(FontAwesome.Icon.faw_sign_out)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    //  ????????? ?????
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {

                        if (position == 1) {
                            Context context = getApplicationContext();
                            Intent intent = new Intent(context, MyProfileActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Bundle b = new Bundle();
                            b.putString("token", token);
                            b.putString("sessionId", sessionId);
                            intent.putExtras(b);
                            context.startActivity(intent);
                        }
                        else if (position == 0)
                        {
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.entalapp.kz/"));
                            startActivity(intent);
                        } else if (position == 2) {
                            Context context = getApplicationContext();
                            Intent intent = new Intent(context, RankingActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Bundle b = new Bundle();
                            b.putString("token", token);
                            b.putString("sessionId", sessionId);
                            intent.putExtras(b);
                            context.startActivity(intent);
                        } else if (position == 3) {
                            Context context = getApplicationContext();
                            Intent intent = new Intent(context, ResultsListActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Bundle b = new Bundle();
                            b.putString("token", token);
                            b.putString("sessionId", sessionId);
                            intent.putExtras(b);
                            context.startActivity(intent);
                        } else if (position == 4) {
                            Context context = getApplicationContext();
                            Intent intent = new Intent(context, FriendsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Bundle b = new Bundle();
                            b.putString("token", token);
                            b.putString("sessionId", sessionId);
                            intent.putExtras(b);
                            context.startActivity(intent);
                        } else if (position == 6) {
                            Context context = getApplicationContext();
                            Intent intent = new Intent(context, InfoActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Bundle b = new Bundle();
                            b.putString("token", token);
                            b.putString("sessionId", sessionId);
                            intent.putExtras(b);
                            context.startActivity(intent);
                        } else if (position == 8) {
                            new Logout().execute();
                        }
                    }
                })
                .build();
    }

    private List<Person> parseJson(String array){

        JsonElement jelement = new JsonParser().parse(array);
        JsonObject jo = jelement.getAsJsonObject();
        jo = jo.getAsJsonObject("message");
        JsonArray ja = jo.getAsJsonArray("result");
        Log.e("My_jo", jo.toString());
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Person>>(){}.getType();
        List<Person> resultsModels = (List<Person>) gson.fromJson(ja, listType);
        Log.e("My_result", resultsModels.toString());

        return resultsModels;
    }

    private class Logout extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(ChallengesActivity.this);
            pDialog.setMessage(getResources().getString(R.string.wait_logout));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            String[] arrayListResponse = sh.makeServiceCall(urlGlobal + url2, ServiceHandler.GET,
                    null, token, sessionId);

            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            SharedPreferences mPrefs = getSharedPreferences("user", 0);
            SharedPreferences.Editor mEditor = mPrefs.edit();
            mEditor.clear().commit();

            SharedPreferences sharedPreferencesCategories = getSharedPreferences("categories", 0);
            SharedPreferences.Editor editorCategories = sharedPreferencesCategories.edit();
            editorCategories.clear().commit();

            Object activity = MainActivity.class;
            Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
