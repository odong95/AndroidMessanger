package com.waspteam.waspmessenger;

/**
 * Created by Julian on 11/14/2016.
 */

public class LoginAttempt
{
    @com.google.gson.annotations.SerializedName("valid")
    public boolean valid;
    @com.google.gson.annotations.SerializedName("handle")
    public String handle;
    @com.google.gson.annotations.SerializedName("key")
    public String key;
}
