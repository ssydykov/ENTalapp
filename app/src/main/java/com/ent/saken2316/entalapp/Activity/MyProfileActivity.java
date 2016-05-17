package com.ent.saken2316.entalapp.Activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ent.saken2316.entalapp.Adapter.RankingListAdapter;
import com.ent.saken2316.entalapp.Adapter.ResultsListAdapter;
import com.ent.saken2316.entalapp.Model.MyApplication;
import com.ent.saken2316.entalapp.Model.Person;
import com.ent.saken2316.entalapp.Server.ServiceHandler;
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

public class MyProfileActivity extends ActionBarActivity {

    // GCM Values:
    private GoogleCloudMessaging gcmObject;
    private String regId = "";
    public static final String REG_ID = "regId";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // Urls
    private String urlGlobal;
    private static String url =  "mainapp/getmyprofile/";
    private static String url2 = "mainapp/getranking/";
    private static String url3 = "mainapp/getplayedgames/";
    private static String url4 = "mainapp/logout/";
    private static String url5 = "mainapp/whochallengeme/";
    private static String url6 = "mainapp/answertochallenge/";
    private static String url7 = "mainapp/regid/";

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
    TextView textViewLevel, username, textViewGames, textView1, textView2, textViewPosition;
    ListView listViewRank, listViewResults;
    ProgressDialog pDialog;
    ProgressBar progressBar1, progressBar2;
    Boolean success = false;
    int DIALOG_CHALLENGE = 1, DIALOG_INVITE = 2;
    String token, sessionId, answer, friendId;
    Person profile;
    Person challenge;
    LinearLayout linearLayout;
    List<Person> resultsList = null;
    List<Person> rankList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        context = getApplicationContext();

        // Intent values:
        intent = getIntent();
        token = intent.getStringExtra("token");
        sessionId = intent.getStringExtra("sessionId");
        urlGlobal = ((MyApplication)this.getApplication()).getUrl();

        // View values:
        imageViewAvatar = (ImageView) findViewById(R.id.avatar);
        textViewLevel = (TextView) findViewById(R.id.textViewLevel);
        textViewGames = (TextView) findViewById(R.id.textViewGames);
        textViewPosition = (TextView) findViewById(R.id.textViewPosition);
        username = (TextView) findViewById(R.id.username);
        progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        listViewRank= (ListView) findViewById(R.id.listViewRank);
        listViewResults = (ListView) findViewById(R.id.listViewGames);
        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        updateButton1 = (Button) findViewById(R.id.updateButton1);
        updateButton2 = (Button) findViewById(R.id.updateButton2);
        linearLayout = (LinearLayout) findViewById(R.id.linear_layout);

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
        SharedPreferences sharedPreferences = getSharedPreferences("user", 0);
        if (!sharedPreferences.getString("first_name", "").equals("")){

            username.setText(sharedPreferences.getString("full_name", ""));
            Glide.with(context)
                    .load(sharedPreferences.getString("avatar", ""))
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(imageViewAvatar);
            textViewLevel.setText(getResources().getString(R.string.rank) + " " + sharedPreferences.getString("total", ""));
            textViewPosition.setText(getResources().getString(R.string.position) + " " + sharedPreferences.getString("position", ""));
        }
        new GetProfile().execute();

        // Get My Games:
        resultsList = ((MyApplication)this.getApplication()).getResultsList();
        if (resultsList == null){

            listViewResults.setVisibility(View.INVISIBLE);
            progressBar1.setVisibility(View.VISIBLE);
            textView1.setVisibility(View.VISIBLE);
        }
        else{

            setMyGames();
        }
        new GetMyGames().execute();

        // Get Rating:
        rankList = ((MyApplication)this.getApplication()).getRankList();
        if (rankList == null){

            listViewRank.setVisibility(View.INVISIBLE);
            progressBar2.setVisibility(View.VISIBLE);
            textView2.setVisibility(View.VISIBLE);
        }
        else{

            setRanking();
        }
        new GetRanking().execute();

        new WhoChallengeMe().execute();
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
                        else if (position == 0)
                        {
                            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.entalapp.kz/"));
                            startActivity(intent);
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
                            Intent intent = new Intent(context, ChallengesActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Bundle b = new Bundle();
                            b.putString("token", token);
                            b.putString("sessionId", sessionId);
                            intent.putExtras(b);
                            context.startActivity(intent);
                        }
                        else if (position == 6)
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
                        else if (position == 8)
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
        }
        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            String[] arrayListResponse = sh.makeServiceCall(urlGlobal + url, ServiceHandler.GET,
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

            if (jsonStr != null){

                username.setText(profile.getFirstName() + " " + profile.getLastName());
                Glide.with(context)
                        .load(profile.getAvatar())
                        .bitmapTransform(new CropCircleTransformation(context))
                        .into(imageViewAvatar);
                textViewLevel.setText(getResources().getString(R.string.rank) + " " + profile.getTotal_points());
                textViewPosition.setText(getResources().getString(R.string.position) + " " + profile.getPosition());

                SharedPreferences sharedPreferences = getSharedPreferences("user", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("first_name", profile.getFirstName());
                editor.putString("last_name", profile.getLastName());
                editor.putString("full_name", profile.getFirstName() + " " + profile.getLastName());
                editor.putString("avatar", profile.getAvatar());
                editor.putString("total", profile.getTotal_points());
                editor.putString("position", profile.getPosition());
                editor.apply();
            }
        }

    }

    private class GetMyGames extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("size", "10"));
            String[] arrayListResponse = sh.makeServiceCall(urlGlobal + url3, ServiceHandler.POST,
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

                listViewResults.setVisibility(View.INVISIBLE);
                updateButton1.setVisibility(View.VISIBLE);
                textView1.setVisibility(View.VISIBLE);
                textView1.setText(getResources().getString(R.string.connection_error));
            }
            else if (resultsList != null){

                setMyGames();
            }
            else {

                listViewResults.setVisibility(View.INVISIBLE);
                textView1.setVisibility(View.VISIBLE);
                textView1.setText(getResources().getString(R.string.no_games));
                textViewGames.setText(getResources().getString(R.string.games) + " 0");
            }
        }
    }
    private void setMyGames (){

        ((MyApplication) MyProfileActivity.this.getApplication()).setResultsList(resultsList);

        listViewResults.setVisibility(View.VISIBLE);
        textView1.setVisibility(View.INVISIBLE);

        textViewGames.setText(getResources().getString(R.string.games) + " " + Integer.toString(resultsList.size()));

        listViewResults.setAdapter(new ResultsListAdapter(MyProfileActivity.this, resultsList));
        listViewResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object activity = ResultActivity.class;
                Intent intent = new Intent(getApplicationContext(),
                        (Class<?>) activity);
                intent.putExtra("token", token);
                intent.putExtra("sessionId", sessionId);
                intent.putExtra("gameId", resultsList.get(position).getGameId());
                intent.putExtra("category_name", resultsList.get(position).getCategoryName());
                intent.putExtra("my_total", resultsList.get(position).getTotal_points());
                intent.putExtra("opponent_name", resultsList.get(position).getOpponentName());
                intent.putExtra("opponent_avatar", resultsList.get(position).getOpponentAvatar());
                intent.putExtra("query_success", true);
                startActivity(intent);
            }
        });

        listViewResults.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }

            int mPosition = 0;
            int mOffset = 0;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int position = listViewResults.getFirstVisiblePosition();
                View v = listViewResults.getChildAt(0);
                int offset = (v == null) ? 0 : v.getTop();

                if (mPosition < position || (mPosition == position && mOffset < offset)) {
                    // Scrolled up
                    //search_layout.setVisibility(View.GONE);
                    //toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
//                    linearLayout.animate().translationY(-linearLayout.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
//                            linearLayout.setVisibility(View.GONE);

                } else {
                    // Scrolled down
                    //search_layout.setVisibility(View.VISIBLE);
                    //toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
//                    linearLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
//                            linearLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private class GetRanking extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            String[] arrayListResponse = sh.makeServiceCall(urlGlobal + url2, ServiceHandler.GET,
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

                listViewRank.setVisibility(View.INVISIBLE);
                updateButton2.setVisibility(View.VISIBLE);
                textView2.setVisibility(View.VISIBLE);
                textView2.setText(getResources().getString(R.string.connection_error));
            }
            else if (rankList != null){

                setRanking();
            }
        }
    }
    private void setRanking (){

        ((MyApplication) MyProfileActivity.this.getApplication()).setRankList(rankList);

        listViewRank.setAdapter(new RankingListAdapter(MyProfileActivity.this, rankList));
        listViewRank.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!rankList.get(position).isYou()) {

                    friendId = rankList.get(position).getId();
                    showDialog(DIALOG_INVITE);
                }
            }
        });
    }

    private class WhoChallengeMe extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            String[] arrayListResponse = sh.makeServiceCall(urlGlobal + url5, ServiceHandler.GET,
                    null, token, sessionId);
            jsonStr = arrayListResponse[2];
            Log.e("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                JsonElement jsonElement = new JsonParser().parse(jsonStr);
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                jsonObject = jsonObject.getAsJsonObject("message");

                Gson gson = new Gson();
                challenge = gson.fromJson(jsonObject, Person.class);

            }
            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (jsonStr == null){
            }
            else if (challenge.isSuccess()){
                showDialog(DIALOG_CHALLENGE);
            }
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
        else if (id == DIALOG_INVITE)
        {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle(getResources().getString(R.string.invite));
//            adb.setMessage(R.string.finish_message);
            adb.setIcon(android.R.drawable.ic_dialog_info);
            adb.setPositiveButton(R.string.yes, myClickListener1);
            adb.setNegativeButton(R.string.no, myClickListener1);
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
    DialogInterface.OnClickListener myClickListener1 = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {

                case Dialog.BUTTON_POSITIVE:

                    Object activity = CategoriesActivity.class;
                    Intent intent = new Intent(getApplicationContext(),
                            (Class<?>) activity);
                    intent.putExtra("friend_id", friendId);
                    intent.putExtra("token", token);
                    intent.putExtra("sessionId", sessionId);
                    startActivity(intent);
                    break;

                case Dialog.BUTTON_NEGATIVE:

                    break;
            }
        }
    };
    private class AnswerToChallenge extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MyProfileActivity.this);
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
            String[] arrayListResponse = sh.makeServiceCall(urlGlobal + url6, ServiceHandler.POST,
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
                intent.putExtra("category_name", challenge.getCategoryName());
                startActivity(intent);
            }

        }
    }

    private class Logout extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MyProfileActivity.this);
            pDialog.setMessage(getResources().getString(R.string.wait_logout));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            sh.makeServiceCall(urlGlobal + url4, ServiceHandler.GET,
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
        inflater.inflate(R.menu.menu_my_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        SharedPreferences sharedPreferencesSettings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferencesSettings.edit();

        if (id == R.id.action_kazakh) {

            MyApplication.setLocaleKk(context);
            editor.putString("language", "kaz");
            editor.commit();
            finish();
            startActivity(getIntent());

            return true;
        }
        else if (id == R.id.action_russian){

            MyApplication.setLocaleRu(context);
            editor.putString("language", "rus");
            editor.commit();
            finish();
            startActivity(getIntent());

            return true;
        }

        return super.onOptionsItemSelected(item);
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
            Log.e("Notification", "This device supports Play services, App will work normally");
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
                Log.e("Notification", "Registered with GCM Server successfully.\n\n" + msg);
            } else Log.e("Notification", "Reg ID Creation Failed.\n\n" +
                    "Either you haven't enabled Internet or GCM server is " +
                    "busy right now. Make sure you enabled Internet and try registering " +
                    "again after some time." + msg);
        }

    }
    private class RegId extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("reg_id", regId));
            sh.makeServiceCall(urlGlobal + url7, ServiceHandler.POST,
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