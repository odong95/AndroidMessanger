package com.waspteam.waspmessenger;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.squareup.okhttp.OkHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.util.concurrent.Semaphore;

public class LoginActivity extends AppCompatActivity {
    Semaphore loginSem = new Semaphore(0);

    MobileServiceClient mClient = null;
    MobileServiceClient mClientAPI = null;
    MobileServiceTable<User> userTable = null;

    boolean loginResult;
    byte[] userSalt;

    byte[] pepper = {8, -52, -61, 86, -55, -75, -94, 14, 99, -36, 100, 118, 74, 20, 101, 9, 49, 118, -62, 27, 121, -14, -97, -24, 45, -113, 107, 126, 94, -48, -81, 36, -55, -92, -34, -11};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Initial setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final EditText etUsername = (EditText) findViewById(R.id.etUsername);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
        final Button Register = (Button) findViewById(R.id.bRegister);
        final Button bLogin = (Button) findViewById(R.id.bLogin);

        //Used to track asynchronous login
        loginResult = false;
        userSalt = null;

        //Setup Azure services access
        try {
            mClient = new MobileServiceClient("https://waspsmessenger.azurewebsites.net", this);
            mClientAPI = new MobileServiceClient("https://waspsmessenger.azurewebsites.net/api", this);
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
        } catch (Exception e) {
        }

        if (mClient == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("Client")
                    .setNegativeButton("Try again", null)
                    .create().show();
        }

        //Listeners for buttons
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etUsername.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty()) {
                    return;
                }
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();
                AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {

                    @Override
                    protected Void doInBackground(String[] objects) {
                        if (register(objects[0], objects[1])) {
                            runOnUiThread(new Runnable() {
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
                                    builder.setMessage("That username or password is invalid/already taken")
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
        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etUsername.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty()) {
                    return;
                } else if (!isValidPassword(etPassword.getText().toString()) || !isValidLogin(etUsername.getText().toString())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage("Login Failed")
                            .setNegativeButton("Try again", null)
                            .create().show();
                    return;
                }
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();

                AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {

                    @Override
                    protected Void doInBackground(String[] objects) {
                        if (login(objects[0], objects[1])) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(LoginActivity.this, ConversationActivity.class);
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

    //Performs registration
    public boolean register(String username, String password) {
        try {

            //if username has special characters, its invalid
            if (!isValidLogin(username)) {
                return false;
            }

            //if the password doesn't meet criteria, its invalid
            if (!isValidPassword(password)) {
                return false;
            }
            //check to see if username is available
            if (!isAvailable(username))
                return false;
            //Generate salt and hash the password
            byte[] salt = generateSalt();
            String salt1 = new String(salt, "ISO-8859-1");
            byte[] salt2 = salt1.getBytes("ISO-8859-1");
            byte[] hash = hashPassword(password, salt);
            //insert new user into table
            User user = new User(username, hash, salt);
            userTable.insert(user);
            return true;
        } catch (Exception e) {
            createAndShowDialog(e, "Error");
        }
        return false;
    }

    /**
     * @param username
     * @param password
     * @return true on success </br> false on failure
     */

    public boolean login(String username, String password) {
        loginResult = false;

        UserCredentials credentials = new UserCredentials();
        credentials.username = username;
        credentials.password = "";

        //Fetch JUST the salt value for this username (without seeing the table)
        try {
            ListenableFuture<ReturnSalt> saltResult = mClientAPI.invokeApi("getSalt", credentials, ReturnSalt.class);

            Futures.addCallback(saltResult, new FutureCallback<ReturnSalt>() {
                @Override
                public void onFailure(Throwable e) {
                    //No valid salt found
                    try {
                        loginSem.release();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onSuccess(ReturnSalt result) {

                    try {
                        userSalt = result.salt.getBytes("ISO-8859-1");
                        loginSem.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //attemptingLogin=false;
                }

            });


        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            loginSem.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //Salt the password to send to the server
        try {
            credentials.password = new String(hashPassword(password, userSalt), "ISO-8859-1");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Check login and then return results

        try {
            ListenableFuture<LoginAttempt> saltResult = mClientAPI.invokeApi("login", credentials, LoginAttempt.class);

            Futures.addCallback(saltResult, new FutureCallback<LoginAttempt>() {
                @Override
                public void onFailure(Throwable e) {
                    //Invalid login credentials
                    try {
                        loginSem.release();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onSuccess(LoginAttempt result) {
                    Snackbar.make(findViewById(R.id.bLogin), "Successful return of password!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    loginResult = true;
                    try {
                        loginSem.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });


        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            loginSem.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return loginResult;
    }

    /**
     * Generates a random 32 byte salt for passwords
     *
     * @return a random 32 byte array
     */
    private byte[] generateSalt() {
        SecureRandom rand = new SecureRandom();
        byte[] salt = new byte[36];
        rand.nextBytes(salt);
        return salt;
    }

    private byte[] hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(salt);
        outputStream.write(pepper);
        byte saltPepper[] = outputStream.toByteArray();
        PBEKeySpec key = new PBEKeySpec(password.toCharArray(), saltPepper, 100, 256);
        SecretKeyFactory keyGen = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return keyGen.generateSecret(key).getEncoded();
    }

    private boolean isAvailable(final String username) throws ExecutionException, InterruptedException {

        try {

            final List<User> results = userTable.where()
                    .field("username").eq(username)
                    .execute().get();
            //results.add(new User("assas", "asasa".getBytes(),"asasas".getBytes()));
            if (results.size() == 0)
                return true;
        } catch (final Exception e) {

            createAndShowDialogFromTask(e, "Error");
        }
        return false;
    }

    //checks if username is valid
    private boolean isValidLogin(String login) {
        Pattern p = Pattern.compile("[^A-Za-z0-9 ]");
        Matcher m = p.matcher(login);
        boolean b = m.find();
        if (b) {
            return false;
        }
        return true;
    }

    //checks if password is valid
    private boolean isValidPassword(String password) {
        Pattern pattern = Pattern.compile("['$<>^]");
        Pattern pattern2 = Pattern.compile("[!@#_^]");
        Matcher matcher = pattern.matcher(password); //must not contain any of these characters
        Matcher matcher2 = pattern2.matcher(password);//must contain these special characters


        if (password.length() < 8 || password.length() > 40) {
            return false;
        } else if (matcher.find()) {
            return false;
        } else if (!matcher2.find()) {
            return false;
        }
        return true;
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

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        this.finish();
    }
}
