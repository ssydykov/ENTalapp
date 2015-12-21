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

public class RankingListAdapter extends ArrayAdapter<Person> {

    List<Person> person;
    Context context;

    public RankingListAdapter(Context context, List<Person> person) {
        super(context, R.layout.rank_item, person);

        this.person = person;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
        convertView = vi.inflate(R.layout.rank_item, parent, false);

        // Product object
        Person person = getItem(position);

//        TextView stand = (TextView) convertView.findViewById(R.id.textViewNumber);
//        stand.setText(person.getPosition());

        // show image
        ImageView imageView = (ImageView)convertView.findViewById(R.id.imageViewOpponentAvatar);
        Glide.with(context)
                .load(person.getAvatar())
                .bitmapTransform(new CropCircleTransformation(context))
                .into(imageView);

        TextView name = (TextView)convertView.findViewById(R.id.textViewName);
        name.setText(person.getFirstName() + " " + person.getLastName());

        TextView point = (TextView)convertView.findViewById(R.id.textViewRank);
        point.setText(person.getTotal_points());

        return convertView;
    }
}