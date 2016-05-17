package com.ent.saken2316.entalapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ent.saken2316.entalapp.Model.Person;
import com.example.saken2316.entalapp.R;

import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class ChallengesAdapter extends ArrayAdapter<Person> {

    List<Person> person;
    Context context;

    public ChallengesAdapter(Context context, List<Person> person) {
        super(context, R.layout.result_item, person);

        this.person = person;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
        convertView = vi.inflate(R.layout.result_item, parent, false);

        Person person = getItem(position);

        TextView userName = (TextView) convertView.findViewById(R.id.textViewOpponentName);
        userName.setText(person.getFull_name());

        TextView category = (TextView) convertView.findViewById(R.id.textViewCategoryName);
        category.setText(person.getCategoryName());

//        TextView date = (TextView) convertView.findViewById(R.id.date);
//        date.setText(person.getDate());

        ImageView img = (ImageView)convertView.findViewById(R.id.imageViewOpponentAvatar);
        Glide.with(context)
                .load(person.getOpponentAvatar())
                .bitmapTransform(new CropCircleTransformation(context))
                .into(img);

        return convertView;
    }
}
