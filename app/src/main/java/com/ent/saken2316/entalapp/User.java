package com.ent.saken2316.entalapp;

import android.app.Application;

public class User extends Application {

    private String firstName;
    private String lastName;
    private String avatar;
    private int point;

    public String getFirstName(){ return firstName; }
    public String getLastName(){ return lastName; }
    public String getAvatar(){ return avatar; }
    public int getPoint(){ return point; }

    public void setFirstName(String _firstName){ this.firstName = _firstName; }
    public void setLastName(String _lastName){ this.lastName = _lastName; }
    public void setAvatar(String _avatar){ this.avatar = _avatar; }
    public void setPoint(int _point){ this.point = _point; }
}
