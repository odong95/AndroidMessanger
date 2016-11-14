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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.graphics.Color;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.security.SecureRandom;

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
import static com.waspteam.waspmessenger.R.string.generate_handle;

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
import android.content.DialogInterface;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.net.MalformedURLException;


public class ConversationActivity extends AppCompatActivity
{
    private EditText mConversationName, mConversationCode;
    private MobileServiceClient mClient;
    private MobileServiceTable<Conversation> mConvoTable;
    private MobileServiceTable<User> mUserTable;
    private String mUsername;
    //DUMMY HANDLE CODE
    private String mHandle = "XXX";
    ConversationAdapter mAdapter;

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();
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
            mUserTable = mClient.getTable(User.class);
            mClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });

            refreshItemsFromTable();

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
        if (id == R.id.generate_handle) {
            handleGeneration();
        }
        else if(id == R.id.view_handle)
        {
            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Handle Code: " + mHandle, Snackbar.LENGTH_LONG);
                    snackbar.setAction("Close", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    })
                    .setActionTextColor(Color.WHITE).setDuration(100000).show();
        }
        else if(id == R.id.logout)
        {
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void handleGeneration()
    {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>()
        {
            protected Void doInBackground(Void... objects) {
                try {
                    final List<User> user = mUserTable.where().field("username").eq(mUsername).execute().get();

                    final User u = user.get(0);
                    final List<Conversation> convosA = userConversations(mHandle);
                    final List<Conversation> convosB = userConversationsB(mHandle);
                    System.out.println("size " + convosA.size());
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {

                            AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity.this);
                            String newHandle = generateHandle(10);
                            u.handle = newHandle;
                            mUserTable.update(u);

                            for (Conversation item : convosA) {
                                item.mHandleA = newHandle;
                                mConvoTable.update(item);
                            }
                            for (Conversation item2 : convosB) {
                                item2.mHandleB = newHandle;
                                mConvoTable.update(item2);
                            }
                            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "New Handle Code: " + newHandle, Snackbar.LENGTH_LONG);
                            snackbar.setAction("Close", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            })
                                    .setActionTextColor(Color.WHITE).setDuration(100000).show();
                        }
                    });
                } catch (final Exception e){
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        }.execute();
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


    private void addToTable(final Conversation addConversation, final String handle, final String nick)
    {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<User> results = checkUser(handle);

                    if (results.size()>0) {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                final User u = results.get(0);
                                mAdapter.clear();
                                addConversation.mHandleB = handle;
                                addConversation.mHandleA = mHandle;
                                addConversation.mNicknameA = nick;
                                addConversation.mNicknameB = u.username;
                                mConvoTable.insert(addConversation);
                                mAdapter.add(addConversation);
                                refreshItemsFromTable();
                            }
                        });

                    } else
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity.this);
                                builder.setMessage("Invalid Handle ID")
                                        .setNegativeButton("Try again", null)
                                        .create().show();
                            }
                        });

                    return null;


                } catch (final Exception e){
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }

        };
        runAsyncTask(task);
    }

    public void refreshItemsFromTable() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<User> user = mUserTable.where().field("username").eq(mUsername).execute().get();
                    final User u = user.get(0);
                    mHandle = u.handle;
                    final List<Conversation> results = refreshItemsFromConvoTable();
                    final List<Conversation> results2 = refreshItemsFromConvoTable2();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mAdapter.clear();
                            for (Conversation item : results) {
                                item.isExist = true;
                                mAdapter.add(item);
                            }
                            for (Conversation item2 : results2) {
                                item2.isExist = false;
                                mAdapter.add(item2);
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
        return mConvoTable.where().field("handleA").eq(val(mHandle)).execute().get();
    }

    private List<Conversation> refreshItemsFromConvoTable2() throws ExecutionException, InterruptedException {
        return mConvoTable.where().field("handleB").eq(val(mHandle)).execute().get();
    }

    private List<User> checkUser(String h) throws ExecutionException, InterruptedException
    {
        return mUserTable.where().field("handle").eq(val(h)).execute().get();
    }

    private List<Conversation> userConversations(String h) throws ExecutionException, InterruptedException
    {
        return mConvoTable.where().field("handleA").eq(val(h)).execute().get();
    }

    private List<Conversation> userConversationsB(String h) throws ExecutionException, InterruptedException
    {
        return mConvoTable.where().field("handleB").eq(val(h)).execute().get();
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

    private boolean validInput(android.text.Editable field)
    {
        //Peculiarly, isEmpty returns true when the field is filled
        return TextUtils.isEmpty(field);
    }



    public void startMessaging(View view, Conversation c)
    {
        Intent intent = new Intent(view.getContext(), MessagingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("EXTRA_MYUSERNAME",mUsername);
        bundle.putString("EXTRA_MYHANDLE",mHandle);
        if(c.isExist) {
            bundle.putString("EXTRA_MYNICKNAME", c.getNicknameA());
            bundle.putString("EXTRA_TOHANDLE",c.mHandleB);
            bundle.putString("EXTRA_TONICK",c.getNicknameB());
        }
        else
        {
            bundle.putString("EXTRA_MYNICKNAME", c.getNicknameB());
            bundle.putString("EXTRA_TOHANDLE",c.mHandleA);
            bundle.putString("EXTRA_TONICK",c.getNicknameA());
        }
        //TONICK must be established by a call to the database
        //It won't even exist until the other party accepts, so this logic will require adjusting

        intent.putExtras(bundle);
        view.getContext().startActivity(intent);
    }

    private String generateHandle(int n){

        StringBuilder sb = new StringBuilder( n );
        sb.append('$');
        for( int i = 0; i < n-1; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
}





