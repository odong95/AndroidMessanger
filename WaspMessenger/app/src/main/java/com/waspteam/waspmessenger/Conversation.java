package com.waspteam.waspmessenger;

/**
 * Created by Julian on 11/6/2016.
 */

public class Conversation
{

    @com.google.gson.annotations.SerializedName("id")
    public String mId;
    @com.google.gson.annotations.SerializedName("handleB")
    public String mHandleB;
    @com.google.gson.annotations.SerializedName("nicknameA")
    public String mNicknameA;
    @com.google.gson.annotations.SerializedName("handleA")
    public String mHandleA;
    @com.google.gson.annotations.SerializedName("nicknameB")
    public String mNicknameB;

    public boolean isExist = false;
    public Conversation()
    {

    }

    public Conversation(String handle, String nick)
    {
        this.setHandle(handle);
        this.setNick(nick);
    }

    public String getHandle(){ return mHandleB; }
    public String getNicknameA() { return mNicknameA; }
    public String getNicknameB() { return mNicknameB; }
    public void setHandle(String n) { mHandleB=n;}

    public void setNick(String n) { mNicknameA=n;}




}
