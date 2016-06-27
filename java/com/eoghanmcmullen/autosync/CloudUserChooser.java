package com.eoghanmcmullen.autosync;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CloudUserChooser extends AppCompatActivity
{
    private SQLiteNameStorer db = new SQLiteNameStorer(this);
    private CustomSyncedDevicesRowAdapter adapter;
    private String url ="";
    private String user = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.synced_devices_screen);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView listOfConnectedDevices = (ListView) findViewById(R.id.syncedDevicesListview);

        //get all users from database and populate adapter
        List<SyncBoxUser> users = db.getAllUsers();

        ArrayList<String> allUserNames = new ArrayList<>();

        for(SyncBoxUser sbu: users)
        {
            allUserNames.add(sbu.getUsername());
        }

        //Custom adapter to display rows in listview
        adapter = new CustomSyncedDevicesRowAdapter(this,allUserNames);

        listOfConnectedDevices.setAdapter(adapter);


        //register a click in the adapter and start the setting screen with the info passed in the intent
    }
    public void itemClickHandler(String username)
    {
        SyncBoxUser sbu = db.getUser(username);
        user = username;
        //Check if that username has a url already
        CloudURLHandler urlHandler = new CloudURLHandler(CloudUserChooser.this,username);

        String url = urlHandler.readURL();

        //if(url.equals(""))
        //{
            //Start the enter url dialog
            showInputDialog();

        //}

    }

    //Dialog for url entry
    protected void showInputDialog()
    {
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(CloudUserChooser.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CloudUserChooser.this);
        alertDialogBuilder.setView(promptView);

        TextView dialogHeader = (TextView) promptView.findViewById(R.id.inputDialogTextView);
        final EditText urlEntry = (EditText) promptView.findViewById(R.id.inputDialogHint);
        urlEntry.setHint("URL is on your SyncBox!");

        dialogHeader.setText("Enter Syncbox URL");
        //setup a dialog window
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                try
                {
                    url = urlEntry.getText().toString();
                    //test the URL?


                    //add to user file
                    CloudURLHandler urlHandler = new CloudURLHandler(CloudUserChooser.this,user,url);
                    urlHandler.writeUrlToUserFile();

                    //Test the url
                    Boolean cloudMode = true;
                    SSHBackgroundWorker worker = new SSHBackgroundWorker
                            (CloudUserChooser.this, "createUserDirectory",cloudMode);
                    worker.execute("createUserDirectory", user);


                } catch (Exception e)
                {
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
            });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    public void onBackPressed()
    {
        //go back to connected sync box screen
        startActivity(new Intent(this, HomeScreen.class));
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
                startActivity(new Intent(CloudUserChooser.this, HomeScreen.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
