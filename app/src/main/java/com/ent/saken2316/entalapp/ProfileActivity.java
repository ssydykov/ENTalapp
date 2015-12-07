package com.ent.saken2316.entalapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.saken2316.entalapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class ProfileActivity extends ActionBarActivity {

    // GCM Values:
    private GoogleCloudMessaging gcmObject;
    private String regId = "";
    public static final String REG_ID = "regId";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // Urls
    private static String url = "http://env-3315080.j.dnr.kz/mainapp/getmyprofile/";
    private static String url2 = "http://env-3315080.j.dnr.kz/mainapp/getranking/";
    private static String url3 = "http://env-3315080.j.dnr.kz/mainapp/getplayedgames/";
    private static String url4 = "http://env-3315080.j.dnr.kz/mainapp/logout/";
    private static String url5 = "http://env-3315080.j.dnr.kz/mainapp/whochallengeme/";
    private static String url6 = "http://env-3315080.j.dnr.kz/mainapp/answertochallenge/";
    private static String url7 = "http://env-3315080.j.dnr.kz/mainapp/regid/";

    JSONObject message = null;
    JSONArray questionsJSON = null;

    String gameID, opponentName, opponentAvatar, opponentPoint, jsonStr;
    String answer1[], answer2[], answer3[], answer4[], questions[];
    int right_answer[];

    Intent intent;
    Toolbar toolbar;
    Context context;
    ImageView imageViewAvatar;
    Button updateButton1, updateButton2;
    TextView textViewLevel, username, textViewGames, textView1, textView2;
    ListView listViewRank, listViewResults;
    ProgressDialog pDialog;
    ProgressBar progressBar1, progressBar2;
    Boolean success = false;
    int DIALOG_CHALLENGE = 1;
    String token, sessionId, answer;
    Person profile;
    Person challenge;
    List<Person> resultsList;
    List<Person> rankList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        context = getApplicationContext();

        // Intent values:
        intent = getIntent();
        token = intent.getStringExtra("token");
        sessionId = intent.getStringExtra("sessionId");

        // View values:
        imageViewAvatar = (ImageView) findViewById(R.id.avatar);
        textViewLevel = (TextView) findViewById(R.id.textViewLevel);
        textViewGames = (TextView) findViewById(R.id.textViewGames);
        username = (TextView) findViewById(R.id.username);
        progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        listViewRank= (ListView) findViewById(R.id.listViewRank);
        listViewResults = (ListView) findViewById(R.id.listViewGames);
        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        updateButton1 = (Button) findViewById(R.id.updateButton1);
        updateButton2 = (Button) findViewById(R.id.updateButton2);

        // Create user preferences - token and session id:
        userPreferencesBuilder();

        // Create GCM registration id:
        createRegId();

        // Create tool bar:
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create status bar:
        toolbarBuilder();

        // Navigation Drawer:
        drawerBuilder();

        // Tabs - My Games and Rating
        tabsBuilder();

        // Update buttons:
        // 1)
        updateButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetMyGames().execute();
                updateButton1.setVisibility(View.INVISIBLE);
            }
        });
        // 2)
        updateButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetRanking().execute();
                updateButton2.setVisibility(View.INVISIBLE);
            }
        });

        // Get Profile:
        new GetProfile().execute();

        // Get My Games:
        new GetMyGames().execute();

        // Get Rating:
        new GetRanking().execute();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void userPreferencesBuilder(){

        SharedPreferences mPrefs = getSharedPreferences("user", 0);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putString("token", token).commit();
        mEditor.putString("sessionId", sessionId).commit();
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
    private void tabsBuilder(){

        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
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
    private void drawerBuilder(){
        new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withHeader(R.layout.drawer_header)
                .withSelectedItem(0)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_user).withIcon(FontAwesome.Icon.faw_user),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_raiting).withIcon(FontAwesome.Icon.faw_list),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_games).withIcon(FontAwesome.Icon.faw_gamepad).withIdentifier(1),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_friends).withIcon(FontAwesome.Icon.faw_users),
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

                        if (position == 2)
                        {
                            Context context = getApplicationContext();
                            Intent intent = new Intent(context, RankingActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Bundle b = new Bundle();
                            b.putString("token", token);
                            b.putString("sessionId", sessionId);
                            intent.putExtras(b);
                            context.startActivity(intent);
                        }
                        else if (position == 3)
                        {
                            Context context = getApplicationContext();
                            Intent intent = new Intent(context, ResultsListActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Bundle b = new Bundle();
                            b.putString("token", token);
                            b.putString("sessionId", sessionId);
                            intent.putExtras(b);
                            context.startActivity(intent);
                        }
                        else if (position == 4)
                        {
                            Context context = getApplicationContext();
                            Intent intent = new Intent(context, FriendsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Bundle b = new Bundle();
                            b.putString("token", token);
                            b.putString("sessionId", sessionId);
                            intent.putExtras(b);
                            context.startActivity(intent);
                        }
                        else if (position == 5)
                        {
                            Context context = getApplicationContext();
                            Intent intent = new Intent(context, InfoActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Bundle b = new Bundle();
                            b.putString("token", token);
                            b.putString("sessionId", sessionId);
                            intent.putExtras(b);
                            context.startActivity(intent);
                        }
                        else if (position == 7)
                        {
                            new Logout().execute();
                        }
                    }
                })
                .build();
    }
    private void createRegId(){

        SharedPreferences prefs = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        if(!TextUtils.isEmpty(prefs.getString(REG_ID, ""))) {

        } else if(checkPlayServices()){

            new CreateRegId().execute();
        }
    }


    private List<Person> parseJson(String array){

        JsonElement jelement = new JsonParser().parse(array);
        JsonObject jo = jelement.getAsJsonObject();
        jo = jo.getAsJsonObject();
        JsonArray ja = jo.getAsJsonArray("message");
        Log.e("My_jo", jo.toString());
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Person>>(){}.getType();
        List<Person> resultsModels = (List<Person>) gson.fromJson(ja, listType);
        Log.e("My_result", resultsModels.toString());

        return resultsModels;
    }

    private class GetProfile extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            setRefreshActionButtonState(true);
        }
        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            String[] arrayListResponse = sh.makeServiceCall(url, ServiceHandler.GET,
                    null, token, sessionId);
            jsonStr = arrayListResponse[2];
            Log.e("Response: ", "> " + jsonStr);

            if (jsonStr != null) {

                JsonElement jsonElement = new JsonParser().parse(jsonStr);
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                jsonObject = jsonObject.getAsJsonObject("message");

                Gson gson = new Gson();
                profile = gson.fromJson(jsonObject, Person.class);
            }

            return null;
        }
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            setRefreshActionButtonState(false);

            if (jsonStr != null){

                username.setText(profile.getFirstName() + " " + profile.getLastName());
                Glide.with(context)
                        .load(profile.getAvatar())
                        .bitmapTransform(new CropCircleTransformation(context))
                        .into(imageViewAvatar);
                textViewLevel.setText(getResources().getString(R.string.rank) + " " + profile.getTotal_points());

                SharedPreferences sharedPreferences = getSharedPreferences("user", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("first_name", profile.getFirstName());
                editor.putString("last_name", profile.getLastName());
                editor.putString("avatar", profile.getAvatar());
                editor.putString("total", profile.getTotal_points());
                editor.apply();
            }
        }

    }

    private class GetMyGames extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            listViewResults.setVisibility(View.INVISIBLE);
            progressBar1.setVisibility(View.VISIBLE);
            textView1.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("size", "10"));
            String[] arrayListResponse = sh.makeServiceCall(url3, ServiceHandler.POST,
                    params, token, sessionId);
            jsonStr = arrayListResponse[2];
            Log.e("Response: ", "> " + jsonStr);

            if (jsonStr != null)
                resultsList = parseJson(jsonStr);

            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressBar1.setVisibility(View.INVISIBLE);
            if (jsonStr == null){

                updateButton1.setVisibility(View.VISIBLE);
                textView1.setVisibility(View.VISIBLE);
                textView1.setText("Bad internet connection");
            }
            else if (!resultsList.isEmpty()){

                listViewResults.setVisibility(View.VISIBLE);
                textView1.setVisibility(View.INVISIBLE);

                textViewGames.setText(getResources().getString(R.string.games) + " " + Integer.toString(resultsList.size()));

                listViewResults.setAdapter(new ResultsListAdapter(ProfileActivity.this, resultsList));
                listViewResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Object activity = ResultActivity.class;
                        Intent intent = new Intent(getApplicationContext(),
                                (Class<?>) activity);
                        intent.putExtra("gameId", resultsList.get(position).getGameId());
                        intent.putExtra("token", token);
                        intent.putExtra("sessionId", sessionId);
                        startActivity(intent);
                    }
                });
            }
            else {

                listViewResults.setVisibility(View.INVISIBLE);
                textView1.setVisibility(View.VISIBLE);
                textView1.setText("No Games");
                textViewGames.setText(getResources().getString(R.string.games) + " 0");
            }
        }
    }

    private class GetRanking extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            listViewRank.setVisibility(View.INVISIBLE);
            progressBar2.setVisibility(View.VISIBLE);
            textView2.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            String[] arrayListResponse = sh.makeServiceCall(url2, ServiceHandler.GET,
                    null, token, sessionId);
            jsonStr = arrayListResponse[2];
            Log.e("Response rating: ", "> " + jsonStr);

            if (jsonStr != null)
                rankList = parseJson(jsonStr);

            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            listViewRank.setVisibility(View.VISIBLE);
            progressBar2.setVisibility(View.INVISIBLE);
            textView2.setVisibility(View.INVISIBLE);

            if (jsonStr == null){

                updateButton2.setVisibility(View.VISIBLE);
                textView2.setVisibility(View.VISIBLE);
                textView2.setText("Bad internet connection");
            }
            else if (!rankList.isEmpty()){

                listViewRank.setAdapter(new RankingListAdapter(ProfileActivity.this, rankList));
            }
        }
    }


    private class WhoChallengeMe extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            String[] arrayListResponse = sh.makeServiceCall(url5, ServiceHandler.GET,
                    null, token, sessionId);
            String jsonStr = arrayListResponse[2];
            Log.e("Response: ", "> " + jsonStr);

            JsonElement jsonElement = new JsonParser().parse(jsonStr);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            jsonObject = jsonObject.getAsJsonObject("message");

            Gson gson = new Gson();
            challenge = gson.fromJson(jsonObject, Person.class);

            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (challenge.isSuccess()){
                showDialog(DIALOG_CHALLENGE);
            }
            else {}
        }
    }
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_CHALLENGE)
        {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle(challenge.getCategoryName());
            adb.setMessage(challenge.getFull_name() + " " + getResources().getString(R.string.wants_to_play));
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
            pDialog = new ProgressDialog(ProfileActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("answer", answer));
            params.add(new BasicNameValuePair("game_id", challenge.getGameId()));
            Log.e("Answer and Game id", answer + " " + challenge.getGameId());
            String[] arrayListResponse = sh.makeServiceCall(url6, ServiceHandler.POST,
                    params, token, sessionId);
            String jsonStr = arrayListResponse[2];
            Log.e("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    message = jsonObj.getJSONObject("message");
                    success = message.getBoolean("success");
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

            if (success){

            }

        }
    }

    private class Logout extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(ProfileActivity.this);
            pDialog.setMessage("Logout, Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            sh.makeServiceCall(url4, ServiceHandler.GET,
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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void onClickGame(View view){

        Object activity = ChooseOpponentActivity.class;
        Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
        intent.putExtra("token", token);
        intent.putExtra("sessionId", sessionId);
        startActivity(intent);
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

//            new GetProfile().execute();
            new GetMyGames().execute();
//            new GetRanking().execute();
            new WhoChallengeMe().execute();
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
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {

            }
            return false;
        } else {
            Toast.makeText( context, "This device supports Play services, App will work normally", Toast.LENGTH_LONG).show();
        }
        return true;
    }
    private class CreateRegId extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                if(gcmObject == null) {
                    gcmObject = GoogleCloudMessaging.getInstance(context);
                }
                regId = gcmObject.register(getResources().getString(R.string.gcm_defaultSenderId));
                Log.e("Reg id", regId);
            } catch (IOException e) {
                regId = "";
            }
            return "";
        }

        @Override
        protected void onPostExecute(String msg) {
            if(!TextUtils.isEmpty(regId)) {
                storeRegIdinSharedPref(context, regId);
                new RegId().execute();
                Toast.makeText(context, "Registered with GCM Server successfully.\n\n" + msg, Toast.LENGTH_SHORT).show();
            } else Toast.makeText(context, "Reg ID Creation Failed.\n\nEither you haven't enabled Internet or GCM server is busy right now. Make sure you enabled Internet and try registering again after some time." + msg, Toast.LENGTH_LONG).show();
        }

    }
    private class RegId extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("reg_id", regId));
            sh.makeServiceCall(url7, ServiceHandler.POST,
                    params, token, sessionId);

            return null;
        }
    }
    private void storeRegIdinSharedPref(Context context, String regId) {

        SharedPreferences prefs = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REG_ID, regId);
        editor.commit();
//        storeRegIdinServer();

    }

}