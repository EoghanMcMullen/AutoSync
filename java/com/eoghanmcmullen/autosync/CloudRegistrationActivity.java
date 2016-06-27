package com.eoghanmcmullen.autosync;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CloudRegistrationActivity extends AppCompatActivity
{
    private String emailString;
    private String passwordString;
    private String userString;
    private String ip;
    private String ipPassword;
    private String ssid;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cloud_registration);

        //Add back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText email = (EditText) findViewById(R.id.emailEditText);
        final EditText user = (EditText) findViewById(R.id.userEditText);
        final EditText password = (EditText) findViewById(R.id.passwordEditText);
        final Button done = (Button) findViewById(R.id.cloudRegiterDoneButton);
        final SQLiteNameStorer db = new SQLiteNameStorer(this);

        //Cloud not being used atm so turn off email and password creation
        email.setVisibility(View.GONE);
        password.setVisibility(View.GONE);



        ip = getIntent().getExtras().getString("ip");
        ipPassword = getIntent().getExtras().getString("password");
        ssid = getIntent().getExtras().getString("ssid");


        //add functionality for selecting a directory
        done.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /*emailString = email.getText().toString();
                passwordString = password.getText().toString();
                userString = user.getText().toString();*/

                //Cloud has been disabled so disable these fields for now
                emailString = "eoghanmcm@hotmail.com";
                passwordString = "098764";


                userString = user.getText().toString();


                Boolean validationCheck = true;

                //email validation
                if(emailString == null || !isEmailValid(emailString))
                {
                    //Toast.makeText(CloudRegistrationActivity.this, "Invalid E-mail address!", Toast.LENGTH_LONG).show();
                    email.setError("Invalid email address");
                    validationCheck = false;
                }
                //----------------------------------

                //Username validation
                else if(userString == null || userString == "" ||userString.length() < 3 || userString.length() > 8)
                {
                    user.setError("Username must be 3 to 8 characters long");
                    validationCheck = false;
                }
                if (validationCheck == true)
                {
                    for (int i = 0; i < userString.length(); i++)
                    {
                        if (!Character.isLetterOrDigit(userString.charAt(i)))
                        {
                            //Toast.makeText(CloudRegistrationActivity.this, "Password must only contain letters and numbers!", Toast.LENGTH_LONG).show();
                            user.setError("Username must only contain letters and numbers!");
                            validationCheck = false;
                        }
                    }
                }
                //---------------------------------

                //password validation
                if(validationCheck == true)
                {
                    if(passwordString == null || passwordString == "" ||passwordString.length() < 5 || passwordString.length() > 8)
                    {
                        //Toast.makeText(CloudRegistrationActivity.this, "Password must be at least 5 characters long!", Toast.LENGTH_LONG).show();
                        password.setError("Password must be between 5 and 8 characters");
                        validationCheck = false;
                    }
                }
                if(validationCheck == true)
                {

                    for (int i = 0; i < passwordString.length(); i++)
                    {
                        if (!Character.isLetterOrDigit(passwordString.charAt(i)))
                        {
                            //Toast.makeText(CloudRegistrationActivity.this, "Password must only contain letters and numbers!", Toast.LENGTH_LONG).show();
                            password.setError("Password must only contain letters and numbers!");
                            validationCheck = false;
                        }
                    }
                }
                //------------------------------


                if(validationCheck == true)
                {
                    //write the new user name to local database
                    //this will be used to set a folder name on the pi

                    SyncBoxUser sbu = new SyncBoxUser(userString,emailString,passwordString,ip,ipPassword);

                    long rowInserted = db.addUser(sbu);

                    //SyncBoxUser sbu2 = db.getUser(userString);


                    if(rowInserted == -1)
                        Toast.makeText(CloudRegistrationActivity.this, "Username taken, try another!" , Toast.LENGTH_LONG).show();
                    else
                    {
                        //should try send username to pi
                        //==================================

                        SSHBackgroundWorker sshWorker = new SSHBackgroundWorker(v.getContext(),"createUserDirectory");
                        sshWorker.execute("createUserDirectory", userString, ssid);

                    }
                }

            }
        });
    }

    public boolean isEmailValid(CharSequence email)
    {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onBackPressed()
    {
        //go back to connected sync box screen

        Intent intent = new Intent(this, HomeScreen.class);
        this.startActivity(intent);
    }

    //Set up home button
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.action_bar_layout, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_home:
                startActivity(new Intent(CloudRegistrationActivity.this, HomeScreen.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
