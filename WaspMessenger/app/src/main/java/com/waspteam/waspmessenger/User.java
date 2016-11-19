package com.waspteam.waspmessenger;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

public class User {
    @com.google.gson.annotations.SerializedName("id")
    public String mId;
    @com.google.gson.annotations.SerializedName("username")
    String username;
    @com.google.gson.annotations.SerializedName("password")
    String password;
    @com.google.gson.annotations.SerializedName("salt")
    String salt;
    @com.google.gson.annotations.SerializedName("handle")
    String handle;

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    User(String username, byte[] password, byte[] salt) throws UnsupportedEncodingException {
        this.username= username;
        this.password = new String(password, "ISO-8859-1");
        this.salt = new String(salt, "ISO-8859-1");
        this.handle = generateHandle(10);
    }

    private String generateHandle(int n){

        StringBuilder sb = new StringBuilder( n );
        sb.append('$');
        for( int i = 0; i < n-1; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
}
