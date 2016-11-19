package com.waspteam.waspmessenger;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Message
{

    private String mFrom;
    private String mText;
    private String mTimestamp;
    private String mTo;
    private String nick;
    @com.google.gson.annotations.SerializedName("id")
    private String id;
    private String handle;
    @com.google.gson.annotations.SerializedName("digest")
    private String mDigest;
    public Message()
    {

    }

    public Message(String from, String to, String text, String nick,String handle, String digest)
    {
        mFrom=from;
        mTo=to;
        mText=text;
        this.nick = nick;
        this.handle = handle;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a");
        mTimestamp=sdf.format(cal.getTime());;
        mDigest=digest;
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

    public void setDigest(String digestIn) {mDigest=digestIn;}

    public String getText()
    {
        return mText;
    }

    public String getFrom()
    {
        return nick;
    }
    public String getHandle()
    {
        return handle;
    }

    public String getTime()
    {
        return mTimestamp;
    }

    public String getmDigest() {return mDigest; }

}