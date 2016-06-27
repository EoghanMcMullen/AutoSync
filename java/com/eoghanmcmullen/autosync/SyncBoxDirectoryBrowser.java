package com.eoghanmcmullen.autosync;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.Collections;

public class SyncBoxDirectoryBrowser extends AppCompatActivity
{
    private ArrayList<String> fileList;
    private String username;

    private TextView currentDirectoryText;
    private Button acceptChoice;
    private Button cancelButton;
    private GridView gridFiles;
    private CustomDirectoryChooserAdapter adapter;
    private String currentPath;

    private WifiManager wifimanager;
    private WifiInfo wifiInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_directory_list);

        //back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get the list that was just generated
        fileList  = getIntent().getExtras().getStringArrayList("fileList");
        username = getIntent().getExtras().getString("username");
        currentPath = getIntent().getExtras().getString("currentPath");

        //References to on screen items
        gridFiles = (GridView) findViewById(R.id.filesGridView);
        currentDirectoryText = (TextView) findViewById(R.id.currentDirectoryText);
        acceptChoice = (Button) findViewById(R.id.acceptChooserButton);
        cancelButton = (Button) findViewById(R.id.cancelChooserButton);

        //hide the buttons
        acceptChoice.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        //Set the path
        currentDirectoryText.setText(currentPath);

        //Allow fastscroll
        gridFiles.setFastScrollEnabled(true);

        ArrayList<String> cleanedFiles = removeSystemFolders(fileList);

        //alphabetically sort the arraylist
        Collections.sort(cleanedFiles, String.CASE_INSENSITIVE_ORDER);

        //Custom adapter to display rows in GridView
        adapter = new CustomDirectoryChooserAdapter(this,cleanedFiles);

        //add items to gridview onload
        gridFiles.setAdapter(adapter);
    }

    public void itemClickHandler(final String filePath)
    {
        //check connection
        final String ssid = checkWifiConnection();

        if(ssid.equals("EoghansPi") || ssid.equals("SyncBox") || ssid.equals("SyncBoxClosed"))
        {

            //Check if a directory is clicked, if so need to re-get the info from syncbox
            if (isDirectory(filePath))
            {
                //username here represents the currently requested filepath
                SSHBackgroundWorker sshWorker = new SSHBackgroundWorker(SyncBoxDirectoryBrowser.this, "checkGetFilesOnPi",
                        currentPath+ "/" + filePath);
                sshWorker.execute("checkGetFilesOnPi", username, ssid);
            }
            //attempt download
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Download " + filePath + " ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                //Ask if user wants to download
                                SSHBackgroundWorker sshWorker = new SSHBackgroundWorker(SyncBoxDirectoryBrowser.this, "downloadFile",
                                        currentPath+ "/" + filePath);
                                sshWorker.execute("downloadFile", username, ssid);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
        else
        {
            //If disconnected go back to synced device menu
            Toast.makeText(SyncBoxDirectoryBrowser.this,
                "SyncBox disconnected, please connect!", Toast.LENGTH_LONG).show();
                //Go to the Synced device menu
            Intent intent = new Intent(SyncBoxDirectoryBrowser.this, SyncedDeviceMenu.class);
            intent.putExtra("username", username);
            SyncBoxDirectoryBrowser.this.startActivity(intent);
        }

    }

    private Boolean isDirectory(String path)
    {
        if(FilenameUtils.getExtension(path).equals(""))
            return true;
        return false;
    }

    public String checkWifiConnection()
    {
        wifimanager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiInfo = wifimanager.getConnectionInfo();
        String ssid = wifiInfo.getSSID().replace("\"", "");

        return ssid;
    }

    private ArrayList removeSystemFolders(ArrayList<String> files)
    {
        ArrayList<String> cleanedFiles = new ArrayList<>();
        for(String f: files)
        {
            if(f.charAt(0) != '.')
            {
                cleanedFiles.add(f);
            }
        }
        return cleanedFiles;
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
            //Actionbar home
            case R.id.action_home:
                startActivity(new Intent(SyncBoxDirectoryBrowser.this, HomeScreen.class));
                return true;
            //actionbar up button
            case android.R.id.home:
                //Launch Cleanup activity
                Intent intent = new Intent(SyncBoxDirectoryBrowser.this, SyncedDeviceMenu.class);
                intent.putExtra("username", username);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
