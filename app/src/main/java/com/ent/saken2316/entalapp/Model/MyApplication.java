package com.ent.saken2316.entalapp.Model;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        printKeyHash();
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
