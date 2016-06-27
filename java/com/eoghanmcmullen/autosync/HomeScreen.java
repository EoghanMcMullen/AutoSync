package com.eoghanmcmullen.autosync;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class HomeScreen extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        //Set the actionbar icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_white_syncbox);

        ImageButton syncNewDeviceButton = (ImageButton) findViewById(R.id.newSyncBoxButton);
        ImageButton cloudButton = (ImageButton) findViewById(R.id.cloudLoginButton);
        ImageButton syncedDevices = (ImageButton) findViewById(R.id.syncedDevicesButton);
        ImageButton instructionsButton = (ImageButton) findViewById(R.id.instructionsButton);


        syncNewDeviceButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(HomeScreen.this, SelectRaspberryPi.class));
            }
        });

        syncedDevices.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                //Check if there any syncboxes if not show a toast no devices synced
                final SQLiteNameStorer db = new SQLiteNameStorer(HomeScreen.this);

                if(!db.anyUsersAvailable())
                {
                    Toast.makeText(HomeScreen.this, "No Devices Synced yet", Toast.LENGTH_LONG).show();
                }
                else
                    startActivity(new Intent(HomeScreen.this, SyncedDeviceChooser.class));
            }
        });

        cloudButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(HomeScreen.this, SettingsActivity.class));
            }
        });

        instructionsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                //Start the webview instructions page
                startActivity(new Intent(HomeScreen.this, InstructionsWebView.class));
            }
        });
    }

    //Retrieve the result from the folder chooser
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==2)
        {
            String directoryPath = data.getStringExtra("directoryPath");

            directoryPath = "";
        }
    }

    @Override
    public void onBackPressed()
    {
        //do nothing
        this.moveTaskToBack(true);
    }
}
