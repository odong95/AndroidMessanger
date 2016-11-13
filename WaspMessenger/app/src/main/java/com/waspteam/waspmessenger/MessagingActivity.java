package com.waspteam.waspmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

public class MessagingActivity extends AppCompatActivity {

    private String mUsername;
    private String mToNick;
    private String mToHandle;
    private String mMyNick;
    private String mMyHandle;

    MessageAdapter mAdapter;

    EditText mMessageEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMessageEdit = (EditText) findViewById(R.id.messageEdit);

        mAdapter = new MessageAdapter(this, R.layout.row_messaging);
        ListView listViewConversation = (ListView) findViewById(R.id.listView_messaging);
        listViewConversation.setAdapter(mAdapter);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mUsername=bundle.getString("EXTRA_MYUSERNAME");
        mMyHandle=bundle.getString("EXTRA_MYHANDLE");
        mMyNick=bundle.getString("EXTRA_MYNICKNAME");
        mToHandle=bundle.getString("EXTRA_TOHANDLE");
        mToNick=bundle.getString("EXTRA_TONICK");
        setTitle(mToNick);
    }

    public void sendMessage(View view)
    {
        //ROOM FOR IMPROVEMENT HERE!
        //MESSAGE SHOULD DISPLAY "ME" WHEN BINDING MESSAGES FROM THE CURRENTLY-LOGGED USER

        //NOTE: NEED TO GET CURRENT TIME HERE AND IN FORMAT THAT DISPLAYS ELEGANTLY

        //NOTE: CURRENTLY, THE GENERATED MESSAGES AREN'T USING THE BUBBLE GRAPHIC THEY SHOULD BE,
        //THIS PROBABLY NEEDS TO BE MANUALLY SET IN THE ADAPTER CLASS, AS WELL AS THE PADDING.

        final Message newMessage = new Message("ME",mMessageEdit.getText().toString(),"0.0.0");

        //Inserting into Azure SQL Happens here, and only add to the adapter once it is inserted

        mAdapter.add(newMessage);
        mMessageEdit.setText("");
    }

}
