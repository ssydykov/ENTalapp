package com.ent.saken2316.entalapp.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.ent.saken2316.entalapp.Adapter.CategoriesAdapter;
import com.ent.saken2316.entalapp.Adapter.CategoriesRusAdapter;
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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends ActionBarActivity {

    TabHost tabHost;
    ListView listView1, listView2;
    Intent intent;
    Toolbar toolbar;
    String token, sessionId, language;
    ProgressBar progressBar;
    ProgressDialog pDialog;
    Button updateButton;
    TextView textView;
    List<Person> categoriesList;

    private String urlGlobal;
    private static String url = "mainapp/categories/";
    private static String url2 = "mainapp/iwanttoplaywithfriend/";
    private static String url3 = "mainapp/playwithbot/";

    private static final String TAG_MESSAGE = "message";
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

    String friend_id = "", categoryId, jsonStr;
    String opponentName, opponentPoint, gameId, opponentAvatar;
    String questions[], answer1[], answer2[], answer3[], answer4[];
    int right_answer[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_categories);

        intent = getIntent();
        token = intent.getStringExtra("token");
        sessionId = intent.getStringExtra("sessionId");
        friend_id = intent.getStringExtra("friend_id");
        urlGlobal = ((MyApplication)this.getApplication()).getUrl();

        getPref();
        toolbarBuilder();

        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabsBuilder();
        listView1 = (ListView) findViewById(R.id.listView1);
        listView2 = (ListView) findViewById(R.id.listView2);
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

        // Get Categories
        categoriesList = ((MyApplication)this.getApplication()).getCategoriesList();
        if (categoriesList == null){

            progressBar.setVisibility(View.VISIBLE);
            textView.setText(getResources().getString(R.string.wait_subjects));
            textView.setVisibility(View.VISIBLE);
            tabHost.setVisibility(View.INVISIBLE);
        }
        else{

            setCategoriesList();
        }
        new GetCategories().execute();
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


    private void toolbarBuilder(){

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
    private void tabsBuilder(){

        tabHost.setup();

        final TabWidget tabWidget = tabHost.getTabWidget();
        final FrameLayout tabContent = tabHost.getTabContentView();

        // Get the original tab textviews and remove them from the viewgroup.
        TextView[] originalTextViews = new TextView[tabWidget.getTabCount()];
        for (int index = 0; index < tabWidget.getTabCount(); index++) {
            originalTextViews[index] = (TextView) tabWidget.getChildTabViewAt(index);
        }
        tabWidget.removeAllViews();

        // Ensure that all tab content childs are not visible at startup.
        for (int index = 0; index < tabContent.getChildCount(); index++) {
            tabContent.getChildAt(index).setVisibility(View.GONE);
        }

        // Create the tabspec based on the textview childs in the xml file.
        // Or create simple tabspec instances in any other way...
        for (int index = 0; index < originalTextViews.length; index++) {
            final TextView tabWidgetTextView = originalTextViews[index];
            final View tabContentView = tabContent.getChildAt(index);
            TabHost.TabSpec tabSpec = tabHost.newTabSpec((String) tabWidgetTextView.getTag());
            tabSpec.setContent(new TabHost.TabContentFactory() {
                @Override
                public View createTabContent(String tag) {
                    return tabContentView;
                }
            });
            if (tabWidgetTextView.getBackground() == null) {
                tabSpec.setIndicator(tabWidgetTextView.getText());
            } else {
                tabSpec.setIndicator(tabWidgetTextView.getText(), tabWidgetTextView.getBackground());
            }
            tabHost.addTab(tabSpec);
        }
        changeTabs(tabWidget);
    }
    private void changeTabs(TabWidget tabWidget) {
        // Change background
        for(int i=0; i < tabWidget.getChildCount(); i++)
            tabWidget.getChildAt(i).setBackgroundResource(R.drawable.tab_indicator);
    }

    private List<Person> parseJson(String array){

        JsonElement jelement = new JsonParser().parse(array);
        JsonObject jo = jelement.getAsJsonObject();
        jo = jo.getAsJsonObject();
        JsonArray ja = jo.getAsJsonArray("message");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Person>>(){}.getType();
        List<Person> resultsModels = (List<Person>) gson.fromJson(ja, listType);
//        Log.e("My_result", resultsModels.toString());

        return resultsModels;
    }
    private class GetCategories extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("language", language));
            String[] arrayListResponse = sh.makeServiceCall(urlGlobal + url, ServiceHandler.POST,
                    params, token, sessionId);
            jsonStr = arrayListResponse[2];
//            Log.e("Response: ", "> " + jsonStr);

            if (jsonStr != null)
                categoriesList = parseJson(jsonStr);

            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.INVISIBLE);
            tabHost.setVisibility(View.VISIBLE);
            textView.setVisibility(View.INVISIBLE);

            if (jsonStr == null){

                tabHost.setVisibility(View.INVISIBLE);
                updateButton.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                textView.setText(getResources().getString(R.string.connection_error));
            }
            else if (!categoriesList.isEmpty()){

                setCategoriesList();
            }
        }
    }
    private void setCategoriesList(){

        ((MyApplication) CategoriesActivity.this.getApplication()).setCategoriesList(categoriesList);

        listView1.setAdapter(new CategoriesAdapter(CategoriesActivity.this, categoriesList));

        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (friend_id != null && !friend_id.isEmpty()) {
                    categoryId = categoriesList.get(position).getId();
                    new IWantToPlayWithFriend().execute();
                } else {
                    Object activity = ThrobberActivity.class;
                    Intent intent = new Intent(getApplicationContext(),
                            (Class<?>) activity);
                    intent.putExtra("categoryId", categoriesList.get(position).getId());
                    intent.putExtra("category_name", categoriesList.get(position).getCategory());
                    intent.putExtra("token", token);
                    intent.putExtra("sessionId", sessionId);
                    startActivity(intent);
                }
            }
        });

        listView2.setAdapter(new CategoriesRusAdapter(CategoriesActivity.this, categoriesList));

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (friend_id != null && !friend_id.isEmpty()) {
                    categoryId = categoriesList.get(position).getId();
                    new IWantToPlayWithFriend().execute();
                } else {
                    Object activity = ThrobberActivity.class;
                    Intent intent = new Intent(getApplicationContext(),
                            (Class<?>) activity);
                    intent.putExtra("categoryId", categoriesList.get(position).getId());
                    intent.putExtra("category_name", categoriesList.get(position).getCategory());
                    intent.putExtra("token", token);
                    intent.putExtra("sessionId", sessionId);
                    startActivity(intent);
                }
            }
        });
    }

    private class IWantToPlayWithFriend extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(CategoriesActivity.this);
            pDialog.setMessage(getResources().getString(R.string.wait_notification));
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("category_id", categoryId));
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
//            Log.e("Response", jsonStr);

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

            Object activity = ReadyToPlayActivity.class;
            Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("token", token);
            intent.putExtra("sessionId", sessionId);
            intent.putExtra("game_id", gameId);
            intent.putExtra("opponent_name", opponentName);
            intent.putExtra("opponent_avatar", opponentAvatar);
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
        if (id == R.id.action_refresh) {

            new GetCategories().execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
