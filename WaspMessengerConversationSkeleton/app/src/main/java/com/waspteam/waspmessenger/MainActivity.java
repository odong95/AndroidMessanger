package com.waspteam.waspmessenger;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;


public class MainActivity extends AppCompatActivity {

    EditText mConversationName, mConversationCode;

    ConversationAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mConversationName = (EditText) findViewById(R.id.newConversationNickname);
        mConversationCode = (EditText) findViewById(R.id.newConversationHandleCode);

        mAdapter = new ConversationAdapter(this, R.layout.row_conversation);
        ListView listViewConversation = (ListView) findViewById(R.id.listView_conversation);
        listViewConversation.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                LinearLayout editLayout = (LinearLayout) findViewById(R.id.newConversationLayout);

                if(editLayout.getVisibility()==LinearLayout.GONE)
                {
                    editLayout.setVisibility(LinearLayout.VISIBLE);
                }
                else
                {
                    if(validInput(mConversationName.getText()) || validInput(mConversationCode.getText()))
                    {
                        Snackbar.make(view, "Please enter a valid name and contact code.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    else
                    {
                        addConversation(view);

                        mConversationName.setText("");
                        mConversationCode.setText("");
                        editLayout.setVisibility(LinearLayout.GONE);
                        editLayout.requestFocus();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addConversation(View view)
    {
        final Conversation addConversation = new Conversation(mConversationName.getText().toString(),mConversationCode.getText().toString() );

        //Inserting into Azure SQL Happens here, and only add to the adapter once it is inserted
        mAdapter.add(addConversation);


    }

    private boolean validInput(android.text.Editable field)
    {
        //Peculiarly, isEmpty returns true when the field is filled
        return TextUtils.isEmpty(field);
    }

}
