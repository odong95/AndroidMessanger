package com.waspteam.waspmessenger;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Julian on 11/8/2016.
 */

public class MessageAdapter extends ArrayAdapter<Message>
{
    Context mContext;

    int mLayoutID;

    //Setup both parent ArrayAdapter and ConversationAdapter variables
    public MessageAdapter(Context context, int layoutID)
    {
        super(context, layoutID);

        mContext=context;
        mLayoutID=layoutID;
    }

    //Override method in ArrayAdapter to handle our unique object data
    @Override
    public View getView(int layoutPos, View alterView, ViewGroup parent)
    {
        View row = alterView;

        final Message bindMessage = getItem(layoutPos);

        if(row == null)
        {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutID, parent, false);
        }

        row.setTag(bindMessage);
        final TextView messageText = (TextView) row.findViewById(R.id.messageText);
        messageText.setText(bindMessage.getFrom() + ": " + bindMessage.getText());
        messageText.setEnabled(true);

        final TextView timeText = (TextView) row.findViewById(R.id.messageTimestamp);
        timeText.setText(bindMessage.getTime());
        timeText.setEnabled(true);

        /*
        //Give each item an onClickListener to allow it to load Messaging Activities

        View.OnClickListener clickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Start a new Messaging Activity between user and " + convText.getText().toString() + ".", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        };
        convText.setOnClickListener(clickListener);

        View.OnLongClickListener longClickListener = new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                Snackbar.make(view, "Delete the " + convText.getText().toString() + " conversation.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                return true;
            }
        };
        convText.setOnLongClickListener(longClickListener);
        */

        return row;
    }

}
