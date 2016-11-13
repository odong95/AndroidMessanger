package com.waspteam.waspmessenger;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Julian on 11/8/2016.
 */

public class Message
{

    private String mFrom;
    private String mText;
    private String mTimestamp;
    private String mTo;
    @com.google.gson.annotations.SerializedName("id")
    private String id;
    public Message()
    {

    }

    public Message(String from, String to, String text)
    {
        mFrom=from;
        mTo=to;
        mText=text;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, HH:mm a");
        mTimestamp=sdf.format(cal.getTime());;
    }

    @Override
    public String toString()
    {
        return getText();
    }

    public void setText(String text)
    {
       mText=text;
    }

    public void setFrom(String from)
    {
        mFrom=from;
    }

    public void setTime(String time)
    {
        mTimestamp=time;
    }

    public String getText()
    {
        return mText;
    }

    public String getFrom()
    {
        return mFrom;
    }

    public String getTime()
    {
        return mTimestamp;
    }

}