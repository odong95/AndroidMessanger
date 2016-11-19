package com.waspteam.waspmessenger;

public class LoginAttempt
{
    @com.google.gson.annotations.SerializedName("valid")
    public boolean valid;
    @com.google.gson.annotations.SerializedName("handle")
    public String handle;
    @com.google.gson.annotations.SerializedName("key")
    public String key;
}
