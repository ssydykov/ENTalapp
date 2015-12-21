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

public class FriendsListAdapter extends ArrayAdapter<Person> {

    List<Person> person;
    Context context;

    public FriendsListAdapter(Context context, List<Person> person) {
        super(context, R.layout.friends_item, person);

        this.person = person;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
        convertView = vi.inflate(R.layout.friends_item, parent, false);

        // Product object
        Person person = getItem(position);
        //
        TextView userName = (TextView) convertView.findViewById(R.id.userName);
        userName.setText(person.getFirstName() + " " + person.getLastName());

        TextView userLevel = (TextView) convertView.findViewById(R.id.userLevel);
        userLevel.setText(person.getCity());

        TextView userPoint = (TextView) convertView.findViewById(R.id.userRank);
        userPoint.setText(person.getTotal_points());

        // show image
        ImageView avatar = (ImageView)convertView.findViewById(R.id.userImage);
        Glide.with(context)
                .load(person.getAvatar())
                .bitmapTransform(new CropCircleTransformation(context))
                .into(avatar);

        return convertView;
    }

}