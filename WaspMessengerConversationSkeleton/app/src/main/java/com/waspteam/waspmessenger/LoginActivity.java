package com.waspteam.waspmessenger;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.squareup.okhttp.OkHttpClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.val;

public class LoginActivity extends AppCompatActivity {
    MobileServiceClient mClient = null;
    MobileServiceTable<User> userTable = null;
    byte[] pepper ={8,-52,-61,86,-55,-75,-94,14,99,-36,100,118,74,20,101,9,49,118,-62,27,121,-14,-97,-24,45,-113,107,126,94,-48,-81,36,-55,-92,-34,-11};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final EditText etUsername = (EditText) findViewById(R.id.etUsername);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
        final Button Register = (Button) findViewById(R.id.bRegister);
        final Button bLogin = (Button) findViewById(R.id.bLogin);
        try {
            mClient = new MobileServiceClient("http://waspsmessenger.azurewebsites.net", this);
            userTable = mClient.getTable(User.class);
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
        catch(Exception e){}

        if(mClient==null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("Client")
                    .setNegativeButton("Try again", null)
                    .create().show();
        }


        Register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();
                AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>()
                {

                    @Override
                    protected Void doInBackground(String[] objects) {
                        if (register(objects[0], objects[1])) {
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                    builder.setMessage("Successful Register!")
                                            .setNegativeButton("Continue", null)
                                            .create().show();
                                }
                            });

                        } else
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                    builder.setMessage("That username is already taken")
                                            .setNegativeButton("Try agan", null)
                                            .create().show();
                                }
                            });

                        return null;
                    }
                };
                task.execute(username, password);
                etUsername.setText("");
                etPassword.setText("");
            }
        });
        bLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();

                AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>()
                {

                    @Override
                    protected Void doInBackground(String[] objects) {
                        if (login(objects[0], objects[1])) {
                            runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("username", username);
                                    LoginActivity.this.startActivity(intent);
                                }
                            });

                        } else
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                    builder.setMessage("Login Failed")
                                            .setNegativeButton("Try again", null)
                                            .create().show();
                                }
                            });

                        return null;
                    }
                };
                task.execute(username, password);
                etUsername.setText("");
                etPassword.setText("");
            }
        });

    }

    public boolean register(String username, String password)
    {
        try{
            //check to see if username is available

           if(!isAvailable(username))
               return false;
            //Generate salt and hash the password
            byte[] salt = generateSalt();
            String salt1 = new String (salt, "ISO-8859-1");
            byte[] salt2 = salt1.getBytes("ISO-8859-1");
            byte[] hash = hashPassword(password, salt);
            //insert new user into table
            User user = new User(username, hash, salt);
            userTable.insert(user);
            return true;
        }
        catch(Exception e)
        {
            createAndShowDialog(e, "error");
        }
        return false;
    }

    /**
     *
     * @param username
     * @param password
     * @return true on success </br> false on failure
     */

    public boolean login(String username, String password) {
        try{
            List<User> result = userTable.where()
                    .field("username").eq(username)
                    .execute().get();
            User user = result.get(0);
            if(checkPassword(password, result.get(0).password, result.get(0).salt.getBytes("ISO-8859-1")))
            {
                return true;
            }


            return false;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Generates a random 32 byte salt for passwords
     *
     * @return a random 32 byte array
     */
    private byte[] generateSalt()
    {
        SecureRandom rand = new SecureRandom();
        byte[] salt = new byte[36];
        rand.nextBytes(salt);
        return salt;
    }

    private byte[] hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write( salt );
        outputStream.write( pepper );
        byte saltPepper[] = outputStream.toByteArray( );
        PBEKeySpec key = new PBEKeySpec(password.toCharArray(), saltPepper, 100, 256);
        SecretKeyFactory keyGen = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return keyGen.generateSecret(key).getEncoded();
    }
    private boolean checkPassword(String password,String hash, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        byte[] hashPass = hashPassword(password,salt);
        boolean result = new String(hashPass, "ISO-8859-1").equals(hash);
        return result;
    }
    private boolean isAvailable(final String username) throws ExecutionException, InterruptedException {

        try {

            final List<User> results = userTable.where()
                    .field("username").eq(username)
                    .execute().get();
            //results.add(new User("assas", "asasa".getBytes(),"asasas".getBytes()));
            if (results.size()==0)
                return true;
        } catch (final Exception e) {

            createAndShowDialogFromTask(e, "Error");
        }
        return false;
    }

    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }
    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
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
}
