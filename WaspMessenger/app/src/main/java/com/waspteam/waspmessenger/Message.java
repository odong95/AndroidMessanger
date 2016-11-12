package com.waspteam.waspmessenger;

/**
 * Created by Julian on 11/8/2016.
 */

public class Message
{

    private String mFrom;
    private String mText;
    private String mTimestamp;

    public Message()
    {

    }

    public Message(String from, String text, String timestamp)
    {
        mFrom=from;
        mText=text;
        mTimestamp=timestamp;
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