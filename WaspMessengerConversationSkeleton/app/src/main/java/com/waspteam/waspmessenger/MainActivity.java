package com.waspteam.waspmessenger;

import java.net.MalformedURLException;
import android.app.Activity;
import android.app.AlertDialog;
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
public class MainActivity extends AppCompatActivity {

    EditText mConversationName, mConversationCode;
    private MobileServiceClient mClient;
    private MobileServiceTable<Conversation> mConvoTable;

    ConversationAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            // Create the Mobile Service Client instance, using app URL
            mClient = new MobileServiceClient(
                    "https://waspsmessenger.azurewebsites.net",
                    this);

            mConvoTable = mClient.getTable(Conversation.class);

            // Create an adapter to bind the items with the view
            mAdapter = new ConversationAdapter(this, R.layout.row_conversation);
            ListView listViewConversation = (ListView) findViewById(R.id.listView_conversation);
            listViewConversation.setAdapter(mAdapter);




        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e){
            createAndShowDialog(e, "Error");
        }

        mConversationName = (EditText) findViewById(R.id.newConversationNickname);
        mConversationCode = (EditText) findViewById(R.id.newConversationHandleCode);


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

        mConvoTable.insert(addConversation);
        mAdapter.add(addConversation);


    }

    private boolean validInput(android.text.Editable field)
    {
        //Peculiarly, isEmpty returns true when the field is filled
        return TextUtils.isEmpty(field);
    }


    /**
     * Creates a dialog and shows it
     *
     * @param exception
     *            The exception to show in the dialog
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    /**
     * Creates a dialog and shows it
     *
     * @param message
     *            The dialog message
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }
}
