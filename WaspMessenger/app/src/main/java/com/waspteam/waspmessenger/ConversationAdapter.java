package com.waspteam.waspmessenger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Julian on 11/7/2016.
 */

public class ConversationAdapter extends ArrayAdapter<Conversation>
{
    Context mContext;

    int mLayoutID;

    //Setup both parent ArrayAdapter and ConversationAdapter variables
    public ConversationAdapter(Context context, int layoutID)
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

        final Conversation bindConversation = getItem(layoutPos);

        if(row == null)
        {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutID, parent, false);
        }

        row.setTag(bindConversation);
        final TextView convText = (TextView) row.findViewById(R.id.conversationName);
        convText.setText(bindConversation.getNickname());
        convText.setEnabled(true);

        //Give each item an onClickListener to allow it to load Messaging Activities

        View.OnClickListener clickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
                {
                    Intent intent = new Intent(view.getContext(), MessagingActivity.class);
                    String handle = convText.getText().toString();
                    intent.putExtra(ConversationActivity.START_MESSAGING,handle);
                    view.getContext().startActivity(intent);

                    /*
                    Snackbar.make(view, "Start a new Messaging Activity between user and " + convText.getText().toString() + ".", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    */
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

        return row;
    }

}
