package com.waspteam.waspmessenger;


import java.net.MalformedURLException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOperations;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;
import java.net.MalformedURLException;
import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.*;

import android.app.AlertDialog;
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

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.net.MalformedURLException;


public class ConversationActivity extends AppCompatActivity
{
    private EditText mConversationName, mConversationCode;
    private MobileServiceClient mClient;
    private MobileServiceTable<Conversation> mConvoTable;
    private String mUsername;
    //DUMMY HANDLE CODE
    private String mHandle = "XXX";
    ConversationAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mConversationName = (EditText) findViewById(R.id.newConversationNickname);
        mConversationCode = (EditText) findViewById(R.id.newConversationHandleCode);

        //Gets String passed the login activity
        Intent intent = getIntent();
        mUsername= intent.getStringExtra("username");
       // mUsername = "Julian";

        mAdapter = new ConversationAdapter(this, R.layout.row_conversation);
        ListView listViewConversation = (ListView) findViewById(R.id.listView_conversation);
        listViewConversation.setAdapter(mAdapter);

        //Startup Azure Connection
        try
        {
            // Create the Mobile Service Client instance, using app URL
            mClient = new MobileServiceClient(
                    "https://waspsmessenger.azurewebsites.net",
                    this);
            mConvoTable = mClient.getTable(Conversation.class);

            mClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });


        }
        catch (MalformedURLException e)
        {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        }
        catch (Exception e)
        {
            createAndShowDialog(e, "Error");
        }

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
        refreshItemsFromTable();

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
    try {
        final Conversation addConversation = new Conversation();
        String handle = mConversationCode.getText().toString();
        String nick = mConversationName.getText().toString();
        addToTable(addConversation,handle,nick);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean validInput(android.text.Editable field)
    {
        //Peculiarly, isEmpty returns true when the field is filled
        return TextUtils.isEmpty(field);
    }

    //checks if conversation already exists
    private void addToTable(final Conversation addConversation, final String handle, final String nick)
    {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<Conversation> results = existedConversation(handle);
                    if(results.size()> 0) {
                        //System.out.println("Conversation exists, updating table");
                        final Conversation c = results.get(0);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.clear();
                                c.mNicknameB = nick;
                                String temp = c.mHandleB;
                                c.mHandleB = c.mHandleA;
                                c.mHandleA = temp;
                                mConvoTable.update(c);
                                mAdapter.add(c);
                            }
                        });
                    }else
                    {
                        //System.out.println("Adding new conversation to table");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addConversation.mHandleB = handle;
                                addConversation.mHandleA = mUsername;
                                addConversation.mNicknameA = nick;
                                mConvoTable.insert(addConversation);
                                mAdapter.add(addConversation);
                            }
                        });

                    }


                } catch (final Exception e){
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }

        };
        runAsyncTask(task);
    }

    private List<Conversation> existedConversation(String h) throws ExecutionException, InterruptedException
    {
        return mConvoTable.where().field("handleB").eq(val(mUsername)).and().field("handleA").eq(val(h)).execute().get();
    }
    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }


    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }
    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    public void refreshItemsFromTable() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<Conversation> results = refreshItemsFromConvoTable();


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.clear();

                            for (Conversation item : results) {
                                mAdapter.add(item);
                            }
                        }
                    });
                } catch (final Exception e){
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        runAsyncTask(task);
    }

    private List<Conversation> refreshItemsFromConvoTable() throws ExecutionException, InterruptedException {
        return mConvoTable.where().field("handleA").
                eq(val(mUsername)).or().field("handleB").eq(val(mUsername)).execute().get();
    }

    public void startMessaging(View view, String myNewNick, String toHandle)
    {
        Intent intent = new Intent(view.getContext(), MessagingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("EXTRA_MYUSERNAME",mUsername);
        bundle.putString("EXTRA_MYHANDLE",mHandle);
        bundle.putString("EXTRA_MYNICKNAME",myNewNick);
        bundle.putString("EXTRA_TOHANDLE",toHandle);
        //TONICK must be established by a call to the database
        //It won't even exist until the other party accepts, so this logic will require adjusting
        bundle.putString("EXTRA_TONICK",toHandle);
        intent.putExtras(bundle);
        view.getContext().startActivity(intent);
    }

}





