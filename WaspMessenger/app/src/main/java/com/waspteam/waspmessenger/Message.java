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
    private String nick;
    @com.google.gson.annotations.SerializedName("id")
    private String id;
    public Message()
    {

    }

    public Message(String from, String to, String text, String nick)
    {
        mFrom=from;
        mTo=to;
        mText=text;
        this.nick = nick;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a");
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
    public void setTo(String to){mTo = to;}

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
        return nick;
    }

    public String getTime()
    {
        return mTimestamp;
    }

}