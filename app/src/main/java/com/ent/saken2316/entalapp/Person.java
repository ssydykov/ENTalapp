package com.ent.saken2316.entalapp;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Saken2316 on 12.11.2015.
 */
public class Person {

    @SerializedName("game_id")
    private String gameId;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("status")
    private boolean status;

    @SerializedName("opponent_name")
    private String opponentName;

    @SerializedName("opponent_points")
    private String opponentPoints;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("date")
    private String date;

    @SerializedName("position")
    private int position;

    @SerializedName("isYou")
    private boolean isYou;

    @SerializedName("total_points")
    private int total_points;

    @SerializedName("state")
    private String state;

    @SerializedName("full_name")
    private String full_name;

    @SerializedName("success")
    private boolean success;

    @SerializedName("opponent_avatar")
    private String opponentAvatar;

    @SerializedName("id")
    private int id;

    @SerializedName("city")
    private String city;

    @SerializedName("user_id")
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getId() {
        return Integer.toString(id);
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOpponentAvatar() {
        return opponentAvatar;
    }

    public void setOpponentAvatar(String opponentAvatar) {
        this.opponentAvatar = opponentAvatar;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isStatus() {
        return status;
    }

    public String getOpponentPoints() {
        return opponentPoints;
    }

    public void setOpponentPoints(String opponentPoints) {
        this.opponentPoints = opponentPoints;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPosition() {
        return Integer.toString(position);
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isYou() {
        return isYou;
    }

    public void setIsYou(boolean isYou) {
        this.isYou = isYou;
    }

    public String getTotal_points() {
        return Integer.toString(total_points);
    }

    public void setTotal_points(int total_points) {
        this.total_points = total_points;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }


    @Override
    public String toString() {
        return "Person{" +
                "gameId='" + gameId + '\'' +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", status=" + status +
                ", opponentName='" + opponentName + '\'' +
                ", opponentPoints='" + opponentPoints + '\'' +
                ", avatar='" + avatar + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", date='" + date + '\'' +
                ", position=" + position +
                ", isYou=" + isYou +
                ", total_points=" + total_points +
                ", state='" + state + '\'' +
                ", full_name='" + full_name + '\'' +
                ", success=" + success +
                ", opponentAvatar='" + opponentAvatar + '\'' +
                '}';
    }
}
