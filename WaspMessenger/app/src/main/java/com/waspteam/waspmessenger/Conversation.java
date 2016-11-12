package com.waspteam.waspmessenger;

/**
 * Created by Julian on 11/6/2016.
 */

public class Conversation
{

    @com.google.gson.annotations.SerializedName("id")
    private String mId;
    @com.google.gson.annotations.SerializedName("nickname")
    private String mNickname;
    @com.google.gson.annotations.SerializedName("handle")
    private String mHandle;

    public Conversation()
    {

    }

    public Conversation(String name, String handle)
    {
        this.setNickname(name);
        this.setHandle(handle);
    }

    @Override
    public String toString()
    {
        return getNickname();
    }

    public String getHandle()
    {
        return mHandle;
    }

    public String getNickname()
    {
        return mNickname;
    }

    public void setHandle(String n)
    {
        mHandle=n;
    }

    public void setNickname(String id)
    {
        mNickname=id;
    }




}
