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
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ListView;
import android.widget.Toast;

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

import java.lang.reflect.Type;
import java.util.List;

public class RankingActivity extends AppCompatActivity {

    private static String url = "http://env-3315080.j.dnr.kz/mainapp/getranking/";
    private static String url2 = "http://env-3315080.j.dnr.kz/mainapp/logout/";

    String token, sessionId;
    List<Person> rankList;

    ListView listViewRank;
    Toolbar toolbar;
    Intent intent;
    Context context;
    Boolean isInternet;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        isInternet = isNetworkAvailable();
        context = getApplicationContext();

        // Values
        listViewRank = (ListView) findViewById(R.id.listViewRank);
        intent = getIntent();
        token = intent.getStringExtra("token");
        sessionId = intent.getStringExtra("sessionId");
        Log.e("token", token);

        // Create tool bar:
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create status bar:
        toolbarBuilder();

        // Navigation Drawer:
        drawerBuilder();

        // Get rating:
        new GetRanking().execute();
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
    private void drawerBuilder(){

        new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withHeader(R.layout.drawer_header)
                .withSelectedItem(1)
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

                        if (position == 1)
                        {
                            Context context = getApplicationContext();
                            Intent intent = new Intent(context, ProfileActivity.class);
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
                        }  else if (position == 3) {
                            Context context = getApplicationContext();
                            Intent intent = new Intent(context, ResultsListActivity.class);
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
                        } else if (position == 7){
                            new Logout().execute();
                        }
                    }
                })
                .build();
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

    private class GetRanking extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setRefreshActionButtonState(true);
        }
        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            String[] arrayListResponse = sh.makeServiceCall(url, ServiceHandler.GET,
                    null, token, sessionId);
            String jsonStr = arrayListResponse[2];
            Log.e("Response: ", "> " + jsonStr);

            rankList = parseJson(jsonStr);

            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            setRefreshActionButtonState(false);

            if (!rankList.isEmpty()){

                listViewRank.setAdapter(new RankingListAdapter(RankingActivity.this, rankList));
            }
        }
    }

    private class Logout extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(RankingActivity.this);
            pDialog.setMessage("Logout, Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            String[] arrayListResponse = sh.makeServiceCall(url2, ServiceHandler.GET,
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
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_ranking, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            Object activity = ProfileActivity.class;
            Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("token", token);
            intent.putExtra("sessionId", sessionId);
            startActivity(intent);

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private Menu optionsMenu;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh && isInternet) {

            new GetRanking().execute();
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
