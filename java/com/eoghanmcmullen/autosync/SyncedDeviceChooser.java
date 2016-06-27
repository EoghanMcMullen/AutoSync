package com.eoghanmcmullen.autosync;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SyncedDeviceChooser extends AppCompatActivity
{
    private SQLiteNameStorer db = new SQLiteNameStorer(this);
    private CustomSyncedDevicesRowAdapter adapter;
    private ArrayList<String> allUserNames;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.synced_devices_screen);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView listOfConnectedDevices = (ListView) findViewById(R.id.syncedDevicesListview);

        //get all users from database and populate adapter
        List<SyncBoxUser> users = db.getAllUsers();

        allUserNames = new ArrayList<>();

        for(SyncBoxUser sbu: users)
        {
            allUserNames.add(sbu.getUsername());
        }

        //Custom adapter to display rows in listview
        adapter = new CustomSyncedDevicesRowAdapter(this,allUserNames);

        listOfConnectedDevices.setAdapter(adapter);


        //register a click in the adapter and start the setting screen with the info passed in the intent
    }

    public void longClickHandler(final String username)
    {
        //Start dialog asking to delete
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Remove user " + username +"?")
                .setPositiveButton("Remove", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        //delete the user
                        db.deleteUser(username);
                        allUserNames.remove(username);
                        adapter.notifyDataSetChanged();

                        Toast.makeText(SyncedDeviceChooser.this,username + " removed!", Toast.LENGTH_LONG).show();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        //cancel
                    }
                });
        AlertDialog alert = builder.create();
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
                startActivity(new Intent(SyncedDeviceChooser.this, HomeScreen.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
