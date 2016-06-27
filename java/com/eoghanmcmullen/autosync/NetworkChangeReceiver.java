package com.eoghanmcmullen.autosync;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.NotificationCompat;
import android.text.format.Formatter;

import java.util.List;

public class NetworkChangeReceiver extends BroadcastReceiver
{
    private Context mContext;
    private static Boolean firstconnection = true;
    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        mContext = context;

        //Determine if we have a successful connection
        WifiManager wifiManager;
        SupplicantState supState;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        supState = wifiInfo.getSupplicantState();

        //Setup notification dismisser
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);


        //Check if connection is complete
        if (supState.compareTo(SupplicantState.COMPLETED) == 0)
        {
            if (checkConnectedToSyncBox())
            {
                if (firstconnection)
                {
                    //Create Notification to check if we want to sync now
                    createNotification(mContext);
                    firstconnection = false;
                }
            }
            else
            {
                //need this for case of successful connection to another wifi
                firstconnection = true;
                //Turn the notification off
                mNotificationManager.cancel(context.getResources().getInteger(R.integer.autoNotificationID));
            }
        }
        else
        {
            firstconnection = true;
            //Turn the notification off
            mNotificationManager.cancel(context.getResources().getInteger(R.integer.autoNotificationID));
        }

    }

    // Check if connected to a registered syncbox
    private boolean checkConnectedToSyncBox()
    {
        boolean connected = false;
        SQLiteNameStorer sqlDB = new SQLiteNameStorer(mContext);

        //Get connected ipAddress
        WifiManager wifiManager =
                (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        final DhcpInfo dhcp = wifiManager.getDhcpInfo();
        final String currentIpAddress = Formatter.formatIpAddress(dhcp.gateway);

        //Check if there are any users set up
        if(sqlDB.anyUsersAvailable())
        {
            //get all connected syncboxes from database
            List<SyncBoxUser> allUsers = sqlDB.getAllUsers();
            String userIpAddress = "";

            for(SyncBoxUser sbu: allUsers)
            {
                //check if ip address matches any
                //if they do return true
                userIpAddress = sbu.getIp();

                if(userIpAddress.equals(currentIpAddress))
                {
                    connected = true;
                    break;
                }

            }

        }

        return connected;
    }

    public void createNotification(Context context)
    {
        Intent startSync = new Intent(context,AutomaticSyncInProgressActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, startSync,
                PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.mipmap.ic_action_reload);
        mBuilder.setContentTitle("SyncBox detected");
        mBuilder.setContentText("Your SyncBox is in range!");
        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setAutoCancel(true);
        mBuilder.setPriority(Notification.PRIORITY_HIGH);
        mBuilder.addAction(R.mipmap.ic_action_reload,"Sync Now", contentIntent);


        //Send the notification
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        final int notificationID = context.getResources().getInteger(R.integer.autoNotificationID);

        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(notificationID, mBuilder.build());
    }

}
