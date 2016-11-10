package com.waspteam.waspmessenger;

import android.content.Intent;
import android.os.AsyncTask;
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
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class LoginActivity extends AppCompatActivity {
    MobileServiceClient mClient = null;
    MobileServiceTable<User> userTable = null;
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
        }
        catch(Exception e){}

        if(mClient==null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("Client")
                    .setNegativeButton("Try again", null)
                    .create().show();
        }
        /*
        if(userTable==null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("Client")
                    .setNegativeButton("Try again", null)
                    .create().show();
        }*/
        Register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();

                boolean result = register(username, password);
                if(!result)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage("That username is already taken")
                            .setNegativeButton("Try again", null)
                            .create().show();

                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage("Successful Register!")
                            .setNegativeButton("Continue", null)
                            .create().show();
                }
            }
        });
        bLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();

                boolean result = login(username, password);

                if(!result)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage("Login Failed")
                            .setNegativeButton("Try agan", null)
                            .create().show();

                }
                else
                {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("username", username);
                    LoginActivity.this.startActivity(intent);
                }

            }
        });

    }
    public boolean register(String username, String password)
    {
        try{
            //check to see if username is available
            List<User> result = userTable.where()
                    .field("username").eq(false)
                    .execute().get();
            if(!result.isEmpty())
                return false;
            //Generate salt and hash the password
            byte[] salt = generateSalt();
            byte[] hash = hashPassword(password, salt);
            //insert new user into table
            User user = new User(username, hash, salt);
            userTable.insert(user).get();
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
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
                    .field("username").eq(false)
                    .execute().get();
            if(checkPassword(password, result.get(0).password.getBytes(), result.get(0).salt.getBytes()))
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

    private byte[] hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        PBEKeySpec key = new PBEKeySpec(password.toCharArray(), salt, 100, 256);
        SecretKeyFactory keyGen = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return keyGen.generateSecret(key).getEncoded();
    }
    private boolean checkPassword(String password, byte[] hash, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        byte[] hashPass = hashPassword(password,salt);
        return new String(hashPass).equals(new String (hash));
    }
}
