package com.waspteam.waspmessenger;

/**
 * Created by jsb140330 on 11/10/2016.
 */

public class User {
    @com.google.gson.annotations.SerializedName("id")
    public String mId;
    String username;
    String password;
    String salt;
    User(String username, byte[] password, byte[] salt)
    {
        this.username= username;
        this.password = new String(password);
        this.salt = new String(salt);
    }
}
