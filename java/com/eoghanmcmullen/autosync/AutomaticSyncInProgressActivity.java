package com.eoghanmcmullen.autosync;

import android.app.NotificationManager;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.view.View;

import java.util.List;

public class AutomaticSyncInProgressActivity extends AppCompatActivity
{
    WifiManager wifimanager;
    WifiInfo wifiInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatic_sync_in_progress);

        //Need to check are we still connected, if not go to home and show toast

        //Get the notification we showed and dismiss it
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

        //Turn the notification off
        mNotificationManager.cancel(this.getResources().getInteger(R.integer.autoNotificationID));

        String ssid = checkWifiConnection();
        String currentIpAddress = getCurrentIP(this);

        SQLiteNameStorer sqlDB = new SQLiteNameStorer(this);

        //Check if there are any users set up
        if(sqlDB.anyUsersAvailable())
        {
            //use to pass an example name
            List<SyncBoxUser> allUsers = sqlDB.getAllUsers();

            SSHBackgroundWorker sshWorker = new SSHBackgroundWorker(this, "backUpAllUsers");
            sshWorker.execute("backUpAllUsers", allUsers.get(0).getUsername(), ssid);
        }
    }
    public String checkWifiConnection()
    {
        wifimanager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiInfo = wifimanager.getConnectionInfo();
        String ssid = wifiInfo.getSSID().replace("\"", "");

        return ssid;
    }
    public String getCurrentIP(Context mContext)
    {
        //Get connected ipAddress
        WifiManager wifiManager =
                (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifiManager.getDhcpInfo();
        String currentIpAddress = Formatter.formatIpAddress(dhcp.gateway);

        return  currentIpAddress;
    }

}
