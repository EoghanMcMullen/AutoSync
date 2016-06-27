package com.eoghanmcmullen.autosync;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

/**
 * Created by eoghanmcmullen on 11/04/2016.
 */
public class BackgroundDialog extends AsyncTask<Void, Void, Void>
{
    private ProgressDialog dialog;
    private SelectRaspberryPi sp;
    private static final String TAG = "";
    private List<ScanResult> results;
    private WifiManager wifimanager;
    private String connectedSsidName ="";
    private String password;
    private String ssid;
    private  Boolean successfulConnection = false;

    public BackgroundDialog(SelectRaspberryPi activity, String pass, String ssid)
    {
        dialog = new ProgressDialog(activity);
        sp = activity;
        password = pass;
        this.ssid = ssid;
    }

    @Override
    protected void onPreExecute()
    {

        dialog.setMessage("Connecting to SyncBox...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected void onPostExecute(Void result)
    {
        if (dialog.isShowing())
        {
            dialog.dismiss();
        }
        //return to activity with connection status
        sp.checkConnectionSuccessful(successfulConnection,ssid);

    }

    @Override
    protected Void doInBackground(Void... params)
    {

        wifimanager = (WifiManager) sp.getSystemService(Context.WIFI_SERVICE);

        connectToAP(ssid, password);

        try
        {
            Thread.sleep(10000);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        WifiInfo wifiInfo = wifimanager.getConnectionInfo();
        //check if connected!
        SupplicantState supState;
        supState = wifiInfo.getSupplicantState();


        while (isHandshakeState(supState))
        {
            wifimanager = (WifiManager) sp.getSystemService(Context.WIFI_SERVICE);
            wifiInfo = wifimanager.getConnectionInfo();
            supState = wifiInfo.getSupplicantState();

        }

        //if supState is now equal to COMPLETED we are connected successfully

        if(supState == SupplicantState.COMPLETED)
        {
            String ssid = wifiInfo.getSSID().replace("\"", "");
            if(ssid.equals(ssid))
            {
                successfulConnection = true;
            }
        }

        return null;
    }

    public int connectToAP(String networkSSID, String networkPasskey)
    {
        results = wifimanager.getScanResults();
        for (ScanResult result : results)
        {

            if (result.SSID.equals(networkSSID))
            {

                String securityMode = getScanResultSecurity(result);

                WifiConfiguration wifiConfiguration = createAPConfiguration(networkSSID, networkPasskey, securityMode);

                int res = wifimanager.addNetwork(wifiConfiguration);
                Log.d(TAG, "# addNetwork returned " + res);

                boolean b = wifimanager.enableNetwork(res, true);
                Log.d(TAG, "# enableNetwork returned " + b);

                wifimanager.setWifiEnabled(true);

                boolean changeHappen = wifimanager.saveConfiguration();

                if (res != -1 && changeHappen)
                {
                    Log.d(TAG, "# Change happen");
                    connectedSsidName = networkSSID;
                }
                else
                {
                    Log.d(TAG, "# Change NOT happen");
                }

                return res;
            }
        }

        return -1;
    }

    public String getScanResultSecurity(ScanResult scanResult)
    {

        final String cap = scanResult.capabilities;
        final String[] securityModes = { "WEP", "PSK", "EAP" };

        for (int i = securityModes.length - 1; i >= 0; i--)
        {
            if (cap.contains(securityModes[i]))
            {
                return securityModes[i];
            }
        }

        return "OPEN";
    }

    private WifiConfiguration createAPConfiguration(String networkSSID, String networkPasskey, String securityMode)
    {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();

        wifiConfiguration.SSID = "\"" + networkSSID + "\"";

        if (securityMode.equalsIgnoreCase("OPEN"))
        {

            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        }
        else if (securityMode.equalsIgnoreCase("WEP"))
        {

            wifiConfiguration.wepKeys[0] = "\"" + networkPasskey + "\"";
            wifiConfiguration.wepTxKeyIndex = 0;
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

        }
        else if (securityMode.equalsIgnoreCase("PSK"))
        {

            wifiConfiguration.preSharedKey = "\"" + networkPasskey + "\"";
            wifiConfiguration.hiddenSSID = true;
            wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

        }
        else
        {
            Log.i(TAG, "# Unsupported security mode: "+ securityMode);

            return null;
        }

        return wifiConfiguration;

    }

    //check if the connection is still being established
    public static boolean isHandshakeState(SupplicantState state)
    {
        switch(state)
        {
            case AUTHENTICATING:
            case ASSOCIATING:
            case ASSOCIATED:
            case FOUR_WAY_HANDSHAKE:
            case GROUP_HANDSHAKE:
                return true;
            case COMPLETED:
            case DISCONNECTED:
            case INTERFACE_DISABLED:
            case INACTIVE:
            case SCANNING:
            case DORMANT:
            case UNINITIALIZED:
            case INVALID:
                return false;

            default:
                throw new IllegalArgumentException("Unknown supplicant state");
        }
    }

}
