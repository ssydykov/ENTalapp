package com.ent.saken2316.entalapp.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
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

public class ResultsListAdapter extends ArrayAdapter<Person> {

    List<Person> person;
    Context context;

    public ResultsListAdapter(Context context, List<Person> person) {
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
        userName.setText(person.getOpponentName());

        TextView category = (TextView) convertView.findViewById(R.id.textViewCategoryName);
        category.setText(person.getCategoryName());
        // Set red, green or yellow text color:
        if (person.getState().equals("1")){
            category.setTextColor(Color.rgb(76, 175, 80));
        }
        else if (person.getState().equals("-1")){
            category.setTextColor(Color.rgb(244, 67, 54));
        }

        TextView date = (TextView) convertView.findViewById(R.id.date);
        date.setText(person.getDate());

        ImageView img = (ImageView)convertView.findViewById(R.id.imageViewOpponentAvatar);
        Glide.with(context)
                .load(person.getAvatar())
                .bitmapTransform(new CropCircleTransformation(context))
                .into(img);

        return convertView;
    }
}