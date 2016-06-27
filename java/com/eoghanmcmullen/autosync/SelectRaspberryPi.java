package com.eoghanmcmullen.autosync;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SelectRaspberryPi extends AppCompatActivity
{
    private SwipeRefreshLayout swipeContainer;
    private WifiManager wifimanager;
    private ArrayList<String> wifiSSIDs;
    private CustomWifiRowAdapter wifiRowAdapter;
    private ListView listOfWifiHotspots;

    private String connectedSsidName ="";
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_raspberry_pi);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listOfWifiHotspots = (ListView) findViewById(R.id.wifiListview);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);


        wifimanager = (WifiManager) getSystemService(WIFI_SERVICE);
        //ensure wifi is on
        turnOnWifi(wifimanager);
        wifiSSIDs = generateWifiSSIDs(wifimanager);

        wifiRowAdapter = new CustomWifiRowAdapter(this,wifiSSIDs);
        listOfWifiHotspots.setAdapter(wifiRowAdapter);

        //Allow swipe down for refresh
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {

                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                startActivity(intent);

            }
        });
    }
    @Override
    protected void onRestart()
    {
        super.onRestart();
        this.recreate();
    }

    public static ArrayList<String> generateWifiSSIDs(WifiManager wifiManager)
    {
        List<ScanResult> results = wifiManager.getScanResults();
        ArrayList<String> wifiSSIDs = new ArrayList<>();

        for(ScanResult s: results)
        {
            wifiSSIDs.add(s.SSID);
        }

        return wifiSSIDs;
    }

    public void turnOnWifi(WifiManager wifimanager)
    {
        if(!wifimanager.isWifiEnabled())
        {
            Toast.makeText(getApplicationContext(), "Wifi is disabled, turning Wifi on..."
                    ,Toast.LENGTH_LONG).show();
            wifimanager.setWifiEnabled(true);
        }
    }

    public void wasSyncboxClicked(String ssid)
    {
        if(!ssid.equals("EoghansPi") && !ssid.equals("SyncBox") && !ssid.equals("SyncBoxClosed"))
        {
            Toast.makeText(getApplicationContext(), "Whoops, that's not a Syncbox, Please choose the Syncbox!"
                    ,Toast.LENGTH_LONG).show();
        }
        else
        {
            //need to connect to the syncbox
            showInputDialog(false, ssid);
        }
    }

    protected void showInputDialog(Boolean connectionFailed,final String ssid)
    {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(SelectRaspberryPi.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SelectRaspberryPi.this);
        alertDialogBuilder.setView(promptView);

        TextView dialogHeader = (TextView) promptView.findViewById(R.id.inputDialogTextView);
        final EditText passwordEntry = (EditText) promptView.findViewById(R.id.inputDialogHint);

        if(connectionFailed == true)
        {
            dialogHeader.setText("Wrong password, try again!");
        }
        else
            dialogHeader.setText("Enter Syncbox Password");
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                try
                {
                    password = passwordEntry.getText().toString();

                    /*//forget networks
                    List<WifiConfiguration> list = wifimanager.getConfiguredNetworks();
                    for(WifiConfiguration i : list)
                    {
                        wifimanager.removeNetwork(i.networkId);
                        wifimanager.saveConfiguration();
                    }*/
                    wifimanager.disconnect();

                    //try to connect to the syncbox
                    BackgroundDialog task = new BackgroundDialog(SelectRaspberryPi.this, password, ssid);
                    task.execute();


                } catch (Exception e)
                {
                }


                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener()
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

    //Asynch task will return the result to this method
    public void checkConnectionSuccessful(Boolean successfulConnection, String ssid)
    {
        if(successfulConnection)
        {
            @SuppressWarnings("deprecation")
            //pass ip to next activity and password
            final DhcpInfo dhcp = wifimanager.getDhcpInfo();
            final String address = Formatter.formatIpAddress(dhcp.gateway);


            //start the next activity
            Intent goToLogin = new Intent(this,CloudRegistrationActivity.class);
            goToLogin.putExtra("ip",address);
            goToLogin.putExtra("password", password);
            goToLogin.putExtra("ssid", ssid);

            startActivity(goToLogin);
        }
        else
        {
            //reload the dialog with wrong password message
            showInputDialog(true, ssid);
        }
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
                startActivity(new Intent(SelectRaspberryPi.this, HomeScreen.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}


