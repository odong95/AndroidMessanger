package com.waspteam.waspmessenger;

public class ConversationKey {
    @com.google.gson.annotations.SerializedName("id")
    String mId;
    @com.google.gson.annotations.SerializedName("convCreatedAt")
    String mDate;
    @com.google.gson.annotations.SerializedName("pubKeyA")
    String mKeyA;
    @com.google.gson.annotations.SerializedName("pubKeyB")
    String mKeyB;
}
