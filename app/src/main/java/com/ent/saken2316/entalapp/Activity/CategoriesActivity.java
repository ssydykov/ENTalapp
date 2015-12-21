package com.ent.saken2316.entalapp.Activity;

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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ent.saken2316.entalapp.Server.ServiceHandler;
import com.example.saken2316.entalapp.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriesActivity extends ActionBarActivity {

    SimpleAdapter adapter;
    ListView listView;
    Intent intent;
    Toolbar toolbar;
    String token, sessionId;
    ProgressBar progressBar;
    Button updateButton;
    TextView textView;

    private static String url = "http://env-3315080.j.dnr.kz/mainapp/categories/";
    private static String url2 = "http://env-3315080.j.dnr.kz/mainapp/iwanttoplaywithfriend/";

    private static final String TAG_MESSAGE = "message";
    private static final String TAG_ID = "id";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_OPPONENT_NAME = "opponent_name";
    private static final String TAG_OPPONENT_AVATAR = "opponent_avatar";
    private static final String TAG_OPPONENT_POINT = "opponent_points";
    private static final String TAG_QUESTIONS = "questions";
    private static final String TAG_QUESTION = "question";
    private static final String TAG_ANSWER1 = "answer_1";
    private static final String TAG_ANSWER2 = "answer_2";
    private static final String TAG_ANSWER3 = "answer_3";
    private static final String TAG_ANSWER4 = "answer_4";
    private static final String TAG_ANSWER = "correct_answer";
    private static final String TAG_GAME_ID = "game_id";
    JSONObject message = null;
    JSONArray questionsJSON = null;
    JSONArray categories = null;

    final String ATTRIBUTE_NAME_CATEGORY = "category";
    String categoriesName[], friend_id = "", categoryId, jsonStr;
    String opponentName, opponentPoint, gameId, opponentAvatar;
    String questions[], answer1[], answer2[], answer3[], answer4[];
    int right_answer[], categoriesId[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        intent = getIntent();
        token = intent.getStringExtra("token");
        sessionId = intent.getStringExtra("sessionId");
        friend_id = intent.getStringExtra("friend_id");

        toolbarBuilder();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object activity = ChooseOpponentActivity.class;
                Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("token", token);
                intent.putExtra("sessionId", sessionId);
                startActivity(intent);
            }
        });

        listView = (ListView) findViewById(R.id.listView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textView = (TextView) findViewById(R.id.textView);
        updateButton = (Button) findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new GetCategories().execute();
                updateButton.setVisibility(View.INVISIBLE);
            }
        });

        new GetCategories().execute();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

    private class GetCategories extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setRefreshActionButtonState(true);
            progressBar.setVisibility(View.VISIBLE);
            textView.setText(getResources().getString(R.string.getting_categories));
            textView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            String[] arrayListResponse = sh.makeServiceCall(url, ServiceHandler.GET,
                    null, token, sessionId);
            jsonStr = arrayListResponse[2];
            Log.e("Response: ", "> " + jsonStr);


            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    categories = jsonObj.getJSONArray(TAG_MESSAGE);
                    categoriesName = new String[categories.length()];
                    categoriesId = new int[categories.length()];

                    // looping through All Contacts
                    for (int i = 0; i < categories.length(); i++) {
                        JSONObject c = categories.getJSONObject(i);

                        categoriesId[i] = c.getInt(TAG_ID);
                        categoriesName[i] = c.getString(TAG_CATEGORY);
                    }
                    SharedPreferences sharedPreferences = getSharedPreferences("categories", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("data", jsonStr);
                    editor.commit();

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
            setRefreshActionButtonState(false);
            progressBar.setVisibility(View.INVISIBLE);

            if (jsonStr == null){

                updateButton.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                textView.setText("Bad internet connection");
            }
            else if (categories != null){

                listView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.INVISIBLE);

                ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(
                        categoriesName.length);
                Map<String, Object> m;

                for (int i = 0; i < categoriesName.length; i++) {
                    m = new HashMap<String, Object>();
                    m.put("id", Integer.toString(categoriesId[i]));
                    m.put(ATTRIBUTE_NAME_CATEGORY, categoriesName[i]);
                    data.add(m);
                }

                String[] from = { ATTRIBUTE_NAME_CATEGORY };
                int[] to = { R.id.categoryName };

                adapter = new SimpleAdapter(CategoriesActivity.this, data,
                        R.layout.categories_item, from, to);
                listView.setAdapter(adapter);

                // Item selected:

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (friend_id != null && !friend_id.isEmpty()) {
                            categoryId = Integer.toString(categoriesId[position]);
                            new IWantToPlayWithFriend().execute();
                        } else {
                            Object activity = ThrobberActivity.class;
                            Intent intent = new Intent(getApplicationContext(),
                                    (Class<?>) activity);
                            intent.putExtra("categoryId", categoriesId[position]);
                            intent.putExtra("token", token);
                            intent.putExtra("sessionId", sessionId);
                            startActivity(intent);
                        }
                    }
                });
            }
        }
    }
    private class IWantToPlayWithFriend extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("category_id", categoryId));
            params.add(new BasicNameValuePair("friend_id", friend_id));
            String[] arrayListResponse = sh.makeServiceCall(url2, ServiceHandler.POST,
                    params, token, sessionId);
            String jsonStr = arrayListResponse[2];
            Log.e("Response", arrayListResponse[2]);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    message = jsonObj.getJSONObject(TAG_MESSAGE);
                    gameId = message.getString(TAG_GAME_ID);
                    opponentName = message.getString(TAG_OPPONENT_NAME);
                    opponentAvatar = message.getString(TAG_OPPONENT_AVATAR);
                    opponentPoint = message.getString(TAG_OPPONENT_POINT);
                    questionsJSON = message.getJSONArray(TAG_QUESTIONS);

                    questions = new String[questionsJSON.length()];
                    answer1 = new String[questionsJSON.length()];
                    answer2 = new String[questionsJSON.length()];
                    answer3 = new String[questionsJSON.length()];
                    answer4 = new String[questionsJSON.length()];
                    right_answer = new int[questionsJSON.length()];

                    // looping through All Contacts
                    for (int i = 0; i < questionsJSON.length(); i++) {

                        JSONObject c = questionsJSON.getJSONObject(i);
                        questions[i] = c.getString(TAG_QUESTION);
                        answer1[i] = c.getString(TAG_ANSWER1);
                        answer2[i] = c.getString(TAG_ANSWER2);
                        answer3[i] = c.getString(TAG_ANSWER3);
                        answer4[i] = c.getString(TAG_ANSWER4);
                        right_answer[i] = c.getInt(TAG_ANSWER);
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

            friend_id = "";
            Toast.makeText(getBaseContext(), getResources().getString(R.string.want_to_play),
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getApplicationContext(), ReadyToPlayActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle b = new Bundle();
            b.putString("token", token);
            b.putString("sessionId", sessionId);
            b.putString("game_id", gameId);
            b.putString("opponent_name", opponentName);
            b.putString("opponent_avatar", opponentAvatar);
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

        new GetCategories().isCancelled();
        return super.onKeyDown(keyCode, event);
    }
    private Menu optionsMenu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_categories, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh && isNetworkAvailable()) {

            new GetCategories().execute();
            return true;
        } else {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.oops),
                    Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
    public void setRefreshActionButtonState(final boolean refreshing) {
        if (optionsMenu != null) {
            final MenuItem refreshItem = optionsMenu
                    .findItem(R.id.action_refresh);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }
}
