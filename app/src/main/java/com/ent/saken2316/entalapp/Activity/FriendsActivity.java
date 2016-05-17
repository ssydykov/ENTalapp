package com.ent.saken2316.entalapp.Activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ent.saken2316.entalapp.Adapter.FriendsListAdapter;
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

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;

public class FriendsActivity extends AppCompatActivity {

    private String urlGlobal;
    private static String url = "mainapp/getfriends/";
    private static String url2 = "mainapp/logout/";
    private static String url3 = "mainapp/search/";

    private boolean isSearchOpened = false;
    String token, sessionId, searchValue, friendId, jsonStr;
    int DIALOG_INVITE = 1;

    Intent intent;
    Context context;
    Toolbar toolbar;
    TextView textView;
    Button updateButton;
    MenuItem searchAction;
    MenuItem refreshAction;
    EditText editSearch;
    ProgressDialog pDialog;
    ProgressBar progressBar;
    ListView listViewFriends;
    List<Person> friendsList;
    List<Person> searchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        context = getApplicationContext();

        intent = getIntent();
        token = intent.getStringExtra("token");
        sessionId = intent.getStringExtra("sessionId");
        urlGlobal = ((MyApplication)this.getApplication()).getUrl();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textView = (TextView) findViewById(R.id.textView);
        listViewFriends = (ListView) findViewById(R.id.listViewFriends);

        updateButton = (Button) findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetMyFriends().execute();
                updateButton.setVisibility(View.INVISIBLE);
            }
        });

        // Create tool bar:
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get Pref:
        getPref();

        // Create status bar:
        toolbarBuilder();

        // Navigation Drawer:
        drawerBuilder();

        // Get Friends:
        friendsList = ((MyApplication)this.getApplication()).getFriendsList();
        if (friendsList == null){

            progressBar.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            listViewFriends.setVisibility(View.INVISIBLE);
            setRefreshActionButtonState(true);
        }
        else{

            setMyFriends();
        }
        new GetMyFriends().execute();
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
                .withSelectedItem(3)
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
                        } else if (position == 5) {
                            Context context = getApplicationContext();
                            Intent intent = new Intent(context, ChallengesActivity.class);
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
        jo = jo.getAsJsonObject();
        JsonArray ja = jo.getAsJsonArray("message");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Person>>(){}.getType();
        List<Person> resultsModels = (List<Person>) gson.fromJson(ja, listType);
//        Log.e("My_result", resultsModels.toString());

        return resultsModels;
    }
    private class GetMyFriends extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            String[] arrayListResponse = sh.makeServiceCall(urlGlobal + url, ServiceHandler.GET,
                    null, token, sessionId);
            jsonStr = arrayListResponse[2];
//            Log.e("Response: ", "> " + jsonStr);

            if (jsonStr != null)
                friendsList = parseJson(jsonStr);

            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);
            listViewFriends.setVisibility(View.VISIBLE);
            setRefreshActionButtonState(false);

            if (jsonStr == null){

                listViewFriends.setVisibility(View.INVISIBLE);
                updateButton.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                textView.setText(getResources().getString(R.string.connection_error));
            }
            else if (!friendsList.isEmpty()) {

                setMyFriends();
            }
            else {

                listViewFriends.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.VISIBLE);
                textView.setText(getResources().getString(R.string.no_friends));
            }
        }
    }
    private void setMyFriends(){

        ((MyApplication) FriendsActivity.this.getApplication()).setFriendsList(friendsList);

        listViewFriends.setAdapter(new FriendsListAdapter(FriendsActivity.this, friendsList));

        listViewFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                friendId = friendsList.get(position).getId();
                showDialog(DIALOG_INVITE);
            }
        });
    }

    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_INVITE)
        {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle(R.string.invite);
//            adb.setMessage(R.string.finish_message);
            adb.setIcon(android.R.drawable.ic_dialog_info);
            adb.setPositiveButton(R.string.yes, myClickListener);
            adb.setNegativeButton(R.string.cancel, myClickListener);
            return adb.create();
        }
        return super.onCreateDialog(id);
    }
    // Dialog Box:
    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
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

    private class Logout extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(FriendsActivity.this);
            pDialog.setMessage(getResources().getString(R.string.wait_logout));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            sh.makeServiceCall(urlGlobal + url2, ServiceHandler.GET,
                    null, token, sessionId);

            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            listViewFriends.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);

            SharedPreferences mPrefs = getSharedPreferences("user", 0);
            SharedPreferences.Editor mEditor = mPrefs.edit();
            mEditor.clear().commit();

            Object activity = MainActivity.class;
            Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        searchAction = menu.findItem(R.id.action_search);
        refreshAction = menu.findItem(R.id.action_refresh);
        return super.onPrepareOptionsMenu(menu);
    }
    private Menu optionsMenu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_friends, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            Object activity = MyProfileActivity.class;
            Intent intent = new Intent(getApplicationContext(), (Class<?>) activity);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("token", token);
            intent.putExtra("sessionId", sessionId);
            startActivity(intent);

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {

            takeScreenshot();

            return true;
        } else if (id == R.id.action_search){

            handleMenuSearch();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {

            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";
            String text = "Сразись со мной в приложении по подготовке к ЕНТ! " +
                    "Ссылка play market: https://play.google.com/store/apps/details?id=com.ent.saken2316.entalapp&hl=ru";

            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_promo);

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

        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
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
    protected void handleMenuSearch(){
        ActionBar action = getSupportActionBar(); //get the actionbar

        textView.setVisibility(View.INVISIBLE);
        if(isSearchOpened){ //test if the search is open

            action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
            action.setDisplayShowTitleEnabled(true); //show the title in the action bar

            //hides the keyboard
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            //add the search icon in the action bar
            searchAction.setIcon(getResources().getDrawable(R.drawable.ic_search_white_24dp));
            refreshAction.setVisible(true);

            listViewFriends.setVisibility(View.VISIBLE);

            isSearchOpened = false;
        } else { //open the search entry

            action.setDisplayShowCustomEnabled(true); //enable it to display a
            // custom view in the action bar.
            action.setCustomView(R.layout.bar_search);//add the custom view
            action.setDisplayShowTitleEnabled(false); //hide the title

            editSearch = (EditText)action.getCustomView().findViewById(R.id.edtSearch); //the text editor

            //this is a listener to do a search when the user clicks on search button
            editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        searchValue = v.getText().toString();
                        doSearch();
                        return true;
                    }
                    return false;
                }
            });


            editSearch.requestFocus();

            //open the keyboard focused in the edtSearch
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editSearch, InputMethodManager.SHOW_IMPLICIT);


            //add the close icon
            searchAction.setIcon(getResources().getDrawable(R.drawable.ic_close_white_24dp));
            refreshAction.setVisible(false);

            listViewFriends.setVisibility(View.INVISIBLE);

            isSearchOpened = true;
        }
    }
    private void doSearch() {

        handleMenuSearch();
        if (!searchValue.isEmpty() && isNetworkAvailable()){

            listViewFriends.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            new GetMyFriendsSearch().execute();
        }
        else {

        }
    }
    private class GetMyFriendsSearch extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... args) {

            ServiceHandler sh = new ServiceHandler();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("string", searchValue));
            String[] arrayListResponse = sh.makeServiceCall(urlGlobal + url3, ServiceHandler.POST,
                    params, token, sessionId);
            String jsonStr = arrayListResponse[2];
//            Log.e("Response: ", "> " + jsonStr);
            if (jsonStr != null) {

                JsonElement jsonElement = new JsonParser().parse(jsonStr);
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                jsonObject = jsonObject.getAsJsonObject("message");

                Gson gson = new Gson();
                JsonArray jsonArray = jsonObject.getAsJsonArray("users");
                Type listType = new TypeToken<List<Person>>() {
                }.getType();
                searchList = (List<Person>) gson.fromJson(jsonArray, listType);
            }

            return null;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (jsonStr == null){

                listViewFriends.setVisibility(View.INVISIBLE);
                updateButton.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                textView.setText(getResources().getString(R.string.connection_error));
            }
            else if (searchList != null) {

                listViewFriends.setAdapter(new FriendsListAdapter(FriendsActivity.this, searchList));
                progressBar.setVisibility(View.INVISIBLE);
                listViewFriends.setVisibility(View.VISIBLE);

                listViewFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        friendId = searchList.get(position).getUserId();
                        showDialog(DIALOG_INVITE);
                    }
                });
            }
            else {

                progressBar.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.VISIBLE);
                textView.setText("No Results");
            }
        }
    }

}
