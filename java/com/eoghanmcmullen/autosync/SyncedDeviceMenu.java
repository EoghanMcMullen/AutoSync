package com.eoghanmcmullen.autosync;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class SyncedDeviceMenu extends AppCompatActivity
{
    private String username;
    private WifiManager wifimanager;
    private WifiInfo wifiInfo;
    private String theSSID;
    private final String uploadSingleFile = "uploadSingleFile";
    private final String backUpToSharedFolder = "backUpToSharedFolder";
    private final String fileMode = "fileMode";
    private final String backUpSingleFile = "backUpSingleFile";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connected_syncbox_menu);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageButton folderSelectButton = (ImageButton) findViewById(R.id.editSyncFoldersButton);
        ImageButton syncNowButton = (ImageButton) findViewById(R.id.syncNowButton);
        ImageButton sendFileButton = (ImageButton) findViewById(R.id.sendFileButton);
        ImageButton sharedFolderButton = (ImageButton) findViewById(R.id.sharedFolderButton);
        ImageButton CleanupButton = (ImageButton) findViewById(R.id.cleanupButton);
        ImageButton accessSyncBox = (ImageButton) findViewById(R.id.accessSyncBoxButton);


        //get username from previous activity
        username = getIntent().getExtras().getString("username");

        setTitle(username + "'s SyncBox");


        //start the folder selection screen with the username added in
        folderSelectButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(SyncedDeviceMenu.this, SelectFolders.class);
                intent.putExtra("username", username);
                v.getContext().startActivity(intent);
            }
        });

        syncNowButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Boolean correctIP = checkConnectedToCorrectIP();

                //check connection
                String ssid = checkWifiConnection();

                //NEED TO CHECK IP ADDRESS HERE TOO
                if (correctIP)
                {
                    //execute the sync
                    SSHBackgroundWorker sshWorker = new SSHBackgroundWorker(v.getContext(), "backUpUserChosenDirectories");
                    sshWorker.execute("backUpUserChosenDirectories", username);
                }
                else checkSSIDForWrongSyncBox(ssid, correctIP);
            }
        });

        sendFileButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Check connection
                final String ssid = checkWifiConnection();
                theSSID = ssid;
                final Boolean correctIP = checkConnectedToCorrectIP();

                //Check if connected to a syncbox
                if(correctIP)
                {
                    //Get file

                    //Now we start an activity for a result
                    Intent intent = new Intent(SyncedDeviceMenu.this, ChooseDirectory.class);
                    //Add in whether we want directoryMode or fileMode
                    intent.putExtra("selectionMode", fileMode);
                    intent.putExtra("callingFunction", uploadSingleFile);
                    startActivityForResult(intent, 2);

                }
                else checkSSIDForWrongSyncBox(ssid, correctIP);
            }
        });

        sharedFolderButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Check connection
                final String ssid = checkWifiConnection();
                final Boolean correctIP = checkConnectedToCorrectIP();

                if(correctIP)
                {
                    //Now we start an activity for a result
                    Intent intent = new Intent(SyncedDeviceMenu.this, ChooseDirectory.class);
                    //Add in whether we want directoryMode or fileMode
                    intent.putExtra("selectionMode", fileMode);
                    intent.putExtra("callingFunction", backUpToSharedFolder);
                    startActivityForResult(intent, 2);
                }
                else checkSSIDForWrongSyncBox(ssid,correctIP);
            }
        });

        CleanupButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Check connection
                final String ssid = checkWifiConnection();
                final Boolean correctIP = checkConnectedToCorrectIP();

                if(correctIP)
                {
                    //Launch Cleanup activity
                    Intent intent = new Intent(SyncedDeviceMenu.this, CleanupFolderPicker.class);
                    intent.putExtra("username", username);
                    v.getContext().startActivity(intent);
                }
                else checkSSIDForWrongSyncBox(ssid, correctIP);
            }
        });

        accessSyncBox.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                //Check connection
                final String ssid = checkWifiConnection();
                final Boolean correctIP = checkConnectedToCorrectIP();

                //---------------------------------------------------------
                //Alert to select which folder to access, shared or users
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Access shared or your folder?")
                        .setPositiveButton("My folder", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                if (correctIP)
                                {
                                    //username here represents the currently requested filepath
                                    SSHBackgroundWorker sshWorker = new SSHBackgroundWorker(v.getContext(), "checkGetFilesOnPi", username);
                                    sshWorker.execute("checkGetFilesOnPi", username);
                                }
                                //Check if connected to wrong SyncBox
                                else checkSSIDForWrongSyncBox(ssid,correctIP);
                            }
                        })
                        .setNegativeButton("Shared", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                if(correctIP)
                                {
                                    //Shared here represents the currently requested filepath
                                    SSHBackgroundWorker sshWorker = new SSHBackgroundWorker(v.getContext(), "checkGetFilesOnPi", "Shared");
                                    sshWorker.execute("checkGetFilesOnPi", username);
                                }
                                else checkSSIDForWrongSyncBox(ssid,correctIP);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                //============================================================
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        //go back to connected sync box screen
        startActivity(new Intent(this, SyncedDeviceChooser.class));
    }

    public String checkWifiConnection()
    {
        wifimanager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiInfo = wifimanager.getConnectionInfo();
        String ssid = wifiInfo.getSSID().replace("\"", "");

        return ssid;
    }

    //Retrieve the result from the folder chooser
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //Get file result from ChooseDirectory
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        final Boolean correctIP = checkConnectedToCorrectIP();
        if(requestCode==2)
        {
            theSSID = checkWifiConnection();
            //recheck connection
            if(correctIP)
            {
                String filePath = data.getStringExtra("directoryPath");
                String callingFunction = data.getStringExtra("callingFunction");

                if(filePath.equals(""))
                {
                    //do nothing, cancelled
                    Toast.makeText(SyncedDeviceMenu.this,
                            "Transfer cancelled!", Toast.LENGTH_LONG).show();
                }
                else
                {
                    //Deal with single file upload
                    if (callingFunction.equals(uploadSingleFile))
                    {
                        //execute the remote command on return
                        SSHBackgroundWorker sshWorker = new SSHBackgroundWorker(SyncedDeviceMenu.this, backUpSingleFile, filePath);
                        sshWorker.execute(backUpSingleFile, username);
                    }
                    //deal with clean mode
                    else if (callingFunction.equals(backUpToSharedFolder))
                    {
                        //execute the remote command on return
                        SSHBackgroundWorker sshWorker = new SSHBackgroundWorker(SyncedDeviceMenu.this, backUpToSharedFolder, filePath);
                        sshWorker.execute(backUpToSharedFolder, username);
                    }
                }
            }
            else checkSSIDForWrongSyncBox(theSSID, correctIP);

        }
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
                startActivity(new Intent(SyncedDeviceMenu.this, HomeScreen.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Checks that connected to correct SyncBox
    private boolean checkConnectedToCorrectIP()
    {
        final SQLiteNameStorer db = new SQLiteNameStorer(SyncedDeviceMenu.this);
        SyncBoxUser sbu = db.getUser(username);
        String userIP = "";

        if(sbu != null)
        {
            userIP = sbu.getIp();
        }

        //get current IP
        WifiManager wifiManager =
                (WifiManager) SyncedDeviceMenu.this.getSystemService(Context.WIFI_SERVICE);
        final DhcpInfo dhcp = wifiManager.getDhcpInfo();
        final String currentIpAddress = Formatter.formatIpAddress(dhcp.gateway);

        if(currentIpAddress.equals(userIP))
            return true;
        return false;
    }

    private void checkSSIDForWrongSyncBox(String ssid, Boolean correctIP)
    {
        //Check if connected to wrong SyncBox
        if(!correctIP && ssid.equals("EoghansPi") ||
            !correctIP && ssid.equals("SyncBox") ||
            !correctIP && ssid.equals("SyncBoxClosed"))
        {
            Toast.makeText(SyncedDeviceMenu.this, "Connected to wrong SyncBox!", Toast.LENGTH_LONG).show();
        }
        else
             Toast.makeText(SyncedDeviceMenu.this, "Not connected to SyncBox, please connect!", Toast.LENGTH_LONG).show();
        }
}
