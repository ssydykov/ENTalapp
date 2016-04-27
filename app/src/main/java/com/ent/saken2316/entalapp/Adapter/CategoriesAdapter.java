package com.ent.saken2316.entalapp.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ent.saken2316.entalapp.Model.MyApplication;
import com.ent.saken2316.entalapp.Model.Person;
import com.example.saken2316.entalapp.R;

import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class CategoriesAdapter extends ArrayAdapter<Person> {

    List<Person> person;
    Context context;
    String language;

    public CategoriesAdapter(Context context, List<Person> person) {
        super(context, R.layout.categories_item, person);

        this.person = person;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SharedPreferences sharedPreferencesSettings = PreferenceManager.getDefaultSharedPreferences(context);
        language = sharedPreferencesSettings.getString("language", "rus");

        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
        Person person = getItem(position);

        if (person.getLanguage().equals("kaz")){

            convertView = vi.inflate(R.layout.categories_item, parent, false);

            TextView categoryName = (TextView) convertView.findViewById(R.id.categoryName);
            categoryName.setText(person.getCategory());

            // show image
            ImageView avatar = (ImageView) convertView.findViewById(R.id.icon);
            String id = person.getId();
            if (id.equals("1") || id.equals("9")) {

                Glide.with(context)
                        .load(R.drawable.history_kz)
                        .into(avatar);
            } else if (id.equals("2") || id.equals("10")) {

                Glide.with(context)
                        .load(R.drawable.geography)
                        .into(avatar);
            } else if (id.equals("3") || id.equals("11")) {

                Glide.with(context)
                        .load(R.drawable.biology)
                        .into(avatar);
            } else if (id.equals("4") || id.equals("12")) {

                Glide.with(context)
                        .load(R.drawable.english)
                        .into(avatar);
            } else if (id.equals("5") || id.equals("13")) {

                Glide.with(context)
                        .load(R.drawable.history)
                        .into(avatar);
            } else if (id.equals("6") || id.equals("14")) {

                Glide.with(context)
                        .load(R.drawable.russian)
                        .into(avatar);
            } else if (id.equals("7") || id.equals("15")) {

                Glide.with(context)
                        .load(R.drawable.kazakh)
                        .into(avatar);
            } else if (id.equals("8")) {

                Glide.with(context)
                        .load(R.drawable.literature)
                        .into(avatar);
            } else if (id.equals("16")) {

                Glide.with(context)
                        .load(R.drawable.qazaqadebiet)
                        .into(avatar);
            } else {

                Glide.with(context)
                        .load(R.drawable.book)
                        .into(avatar);
            }
        }
        else {

            convertView = vi.inflate(R.layout.categories_null_item, parent, false);
        }


        return convertView;
    }

}