package com.waspteam.waspmessenger;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.content.DialogInterface;
import android.view.LayoutInflater;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.squareup.okhttp.OkHttpClient;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.val;

public class MessagingActivity extends AppCompatActivity {

    private String mUsername;
    private String mToNick;
    private String mToHandle;
    private String mMyNick;
    private String mMyHandle;

    MessageAdapter mAdapter;

    EditText mMessageEdit;
    MobileServiceClient mClient = null;
    MobileServiceTable<Message> messageTable = null;
    MobileServiceTable<Conversation> mConvoTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mMessageEdit = (EditText) findViewById(R.id.messageEdit);
        final Button send = (Button) findViewById(R.id.sendButton);
        FloatingActionButton cNick = (FloatingActionButton) findViewById(R.id.bChangeNick);
        mAdapter = new MessageAdapter(this, R.layout.row_messaging);
        ListView listViewConversation = (ListView) findViewById(R.id.listView_messaging);
        listViewConversation.setAdapter(mAdapter);
        listViewConversation.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        try {
            mClient = new MobileServiceClient("https://waspsmessenger.azurewebsites.net", this);
            messageTable = mClient.getTable(Message.class);
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
        } catch (Exception e) {
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mUsername = bundle.getString("EXTRA_MYUSERNAME");
        mMyHandle = bundle.getString("EXTRA_MYHANDLE");
        mMyNick = bundle.getString("EXTRA_MYNICKNAME");
        mToHandle = bundle.getString("EXTRA_TOHANDLE");
        mToNick = bundle.getString("EXTRA_TONICK");

        setTitle(mToNick);
        LoadItemsFromTable();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(v);
            }
        });

        cNick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = LayoutInflater.from(MessagingActivity.this);
                final View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(MessagingActivity.this);
                builder.setView(promptView);


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText text = (EditText) promptView.findViewById(R.id.edittext);
                        updateNickname(text.getText().toString());
                        dialog.dismiss();
                    }
                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .create().show();
            }
        });
    }

    public void sendMessage(View view) {
        //ROOM FOR IMPROVEMENT HERE!
        //MESSAGE SHOULD DISPLAY "ME" WHEN BINDING MESSAGES FROM THE CURRENTLY-LOGGED USER

        //NOTE: NEED TO GET CURRENT TIME HERE AND IN FORMAT THAT DISPLAYS ELEGANTLY

        //NOTE: CURRENTLY, THE GENERATED MESSAGES AREN'T USING THE BUBBLE GRAPHIC THEY SHOULD BE,
        //THIS PROBABLY NEEDS TO BE MANUALLY SET IN THE ADAPTER CLASS, AS WELL AS THE PADDING.

        final Message newMessage = new Message(mMyHandle, mToHandle, mMessageEdit.getText().toString(), mMyNick,mMyHandle);

        messageTable.insert(newMessage);
        refreshItemsFromTable();
        mMessageEdit.setText("");

    }

    public void refreshItemsFromTable() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<Message> results = refreshItemsFromMessageoTable();


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.clear();
                            for (Message item : results) {
                                mAdapter.add(item);
                            }

                        }
                    });
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        runAsyncTask(task);
    }

    public void LoadItemsFromTable() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<Message> results = refreshItemsFromMessageoTable();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.clear();

                            for (Message item : results) {
                                mAdapter.add(item);
                            }
                        }
                    });
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        runAsyncTask(task);
    }

    /*
    private List<Message> refreshItemsFromMessageoTable() throws ExecutionException, InterruptedException {
        return messageTable.where().field("mFrom").
                eq(val(mMyHandle)).and().field("mTo").eq(val(mToHandle)).or().field("mTo").eq(val(mMyHandle)).and().field("mFrom").eq(val(mToHandle)).execute().get();
    }*/


    private List<Message> refreshItemsFromMessageoTable() throws ExecutionException, InterruptedException {
        return messageTable.where().field("mFrom").
                eq(val(mMyHandle)).or().field("mFrom").eq(val(mUsername)).and().field("mTo").eq(val(mToHandle))
                .or().field("mTo").eq(val(mMyHandle)).or().field("mTo").eq(val(mUsername)).and().field("mFrom").eq(val(mToHandle)).execute().get();
    }
    public void updateNickname(final String nick) {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<Conversation> result = findConvoFromTable();
                    final List<Conversation> resultB = findConvoFromTable2();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (result.size() > 0)//update nickname B
                            {
                                final Conversation c = result.get(0);
                                c.mNicknameB = nick;
                                mConvoTable.update(c);
                                mMyNick = nick;
                            } else {
                                final Conversation c = resultB.get(0);
                                c.mNicknameA = nick;
                                mConvoTable.update(c);
                                mMyNick = nick;
                            }

                        }
                    });
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        runAsyncTask(task);
    }

    private List<Conversation> findConvoFromTable() throws ExecutionException, InterruptedException {
        return mConvoTable.where().field("handleA").eq(mToHandle).and().field("nicknameA").eq(val(mToNick))
                .or().field("handleB").eq(mToHandle).and().field("nicknameA").eq(val(mToNick)).execute().get();
    }

    private List<Conversation> findConvoFromTable2() throws ExecutionException, InterruptedException {
        return mConvoTable.where().field("handleA").eq(mToHandle).and().field("nicknameB").eq(val(mToNick))
                .or().field("handleB").eq(mToHandle).and().field("nicknameB").eq(val(mToNick)).execute().get();
    }

    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if (exception.getCause() != null) {
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }


    private void createAndShowDialog(final String message, final String title) {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

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

    @Override
    protected void onRestart() {
        super.onRestart();
        mAdapter.notifyDataSetChanged();
    }
}
