package com.ent.saken2316.entalapp.Model;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.util.Base64;
import android.util.Log;

import com.ent.saken2316.entalapp.Server.AnalyticsTrackers;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

public class MyApplication extends Application {


    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        printKeyHash();
        mInstance = this;

        AnalyticsTrackers.initialize(this);
        AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
    }

    public void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.ent.saken2316.entalapp", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("My Key", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public synchronized Tracker getGoogleAnalyticsTracker() {
        AnalyticsTrackers analyticsTrackers = AnalyticsTrackers.getInstance();
        return analyticsTrackers.get(AnalyticsTrackers.Target.APP);
    }

    public void trackScreenView(String screenName) {
        Tracker t = getGoogleAnalyticsTracker();

        // Set screen name.
        t.setScreenName(screenName);

        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());

        GoogleAnalytics.getInstance(this).dispatchLocalHits();
    }

    public void trackException(Exception e) {
        if (e != null) {
            Tracker t = getGoogleAnalyticsTracker();

            t.send(new HitBuilders.ExceptionBuilder()
                            .setDescription(
                                    new StandardExceptionParser(this, null)
                                            .getDescription(Thread.currentThread().getName(), e))
                            .setFatal(false)
                            .build()
            );
        }
    }

    public void trackEvent(String category, String action, String label) {
        Tracker t = getGoogleAnalyticsTracker();

        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
    }

    public static void setLocaleKk(Context context) {
        Locale locale = new Locale("kk");
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        context.getApplicationContext().getResources().updateConfiguration(configuration, null);
    }

    public static void setLocaleRu(Context context) {
        Locale locale = new Locale("ru");
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        context.getApplicationContext().getResources().updateConfiguration(configuration, null);
    }

    private List resultsListFull = null;
    public List getResultsListFull() {
        return resultsListFull;
    }
    public void setResultsListFull(List resultsListFull) {
        this.resultsListFull = resultsListFull;
    }

    private List friendsList = null;
    public List getFriendsList() {
        return friendsList;
    }
    public void setFriendsList(List friendsList) {
        this.friendsList = friendsList;
    }

    private List resultsList = null;
    public List getResultsList() {
        return resultsList;
    }
    public void setResultsList(List resultsList) {
        this.resultsList = resultsList;
    }

    private List challengesList = null;
    public List getChallengesList() {
        return challengesList;
    }
    public void setChallengesList(List challengesList) {
        this.challengesList = challengesList;
    }

    private List rankList = null;
    public List getRankList() {
        return rankList;
    }
    public void setRankList(List rankList) {
        this.rankList = rankList;
    }

    private List categoriesList = null;
    public List getCategoriesList() {
        return categoriesList;
    }
    public void setCategoriesList(List categoriesList) {
        this.categoriesList = categoriesList;
    }

    private String url = "http://185.22.67.17/";
//    private String url = "http://env-3315080.j.dnr.kz/";
    public String getUrl() { return url; }
}
