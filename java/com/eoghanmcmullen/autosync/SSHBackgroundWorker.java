package com.eoghanmcmullen.autosync;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eoghanmcmullen on 11/04/2016.
 */
public class SSHBackgroundWorker extends AsyncTask<String, Integer, String>
{
    private ProgressDialog dialog;
    private Context mContext;
    private String username;
    private String ssid;
    private String password;
    private String email;
    private String ip;
    private String ip_password;
    private Boolean taskCompleted = false;
    private String methodToRun = "";
    private String androidFilePath;
    private ArrayList filesForPiBrowser;
    private Boolean cloudMode = false;

    //Use in cleanup function
    private int numFilesDeleted = 0;
    private long mbDeleted = 0l;
    private int numFilesUploaded = 0;

    String usbDriveResult = "";



    public SSHBackgroundWorker (Context context)
    {
        mContext = context;
        dialog = new ProgressDialog(mContext);

    }

    //need to add method to run to contructor for on preExecute
    public SSHBackgroundWorker (Context context, String methodToRun)
    {
        mContext = context;
        dialog = new ProgressDialog(mContext);
        this.methodToRun = methodToRun;

    }

    //need to add method to run to contructor for on preExecute
    public SSHBackgroundWorker (Context context, String methodToRun,String filePathOnAndroid)
    {
        mContext = context;
        dialog = new ProgressDialog(mContext);
        this.methodToRun = methodToRun;
        androidFilePath = filePathOnAndroid;
    }
    //Add a boolean for Cloud mode
    public SSHBackgroundWorker (Context context, String methodToRun, Boolean cloudMode)
    {
        mContext = context;
        dialog = new ProgressDialog(mContext);
        this.methodToRun = methodToRun;
        this.cloudMode = cloudMode;
    }

    @Override
    protected void onPreExecute()
    {
        //dialog.setMessage("Connecting to SyncBox");
        //dialog.show();

        if(methodToRun.equals("backUpUserChosenDirectories"))
        {
            dialog.setMessage("Uploading files to SyncBox...");
            dialog.setIndeterminate(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.setProgressNumberFormat(null);
            dialog.setMax(100);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.show();
        }
        else if(methodToRun.equals("createUserDirectory"))
        {
            dialog.setMessage("Creating new user...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }
        else if(methodToRun.equals("backUpSingleFile") || methodToRun.equals("backUpToSharedFolder"))
        {
            dialog.setMessage("Backing up file...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        else if(methodToRun.equals("cleanupFolders"))
        {
            dialog.setMessage("Cleaning up folder...");
            dialog.setIndeterminate(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.setProgressNumberFormat(null);
            dialog.setMax(100);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.show();
        }
        else if(methodToRun.equals("backUpAllUsers"))
        {
            dialog.setMessage("Backing up all users...");
            dialog.setIndeterminate(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.setProgressNumberFormat(null);
            dialog.setMax(100);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.show();
        }
        else if(methodToRun.equals("checkGetFilesOnPi"))
        {
            dialog.setMessage("Getting files on SyncBox...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }
        else if(methodToRun.equals("downloadFile"))
        {
            dialog.setMessage("Downloading...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }

    }

    @Override
    protected void onPostExecute(String result)
    {
        if(usbDriveResult.equals(""))
        {
            Toast.makeText(mContext, "No USB drive detected!", Toast.LENGTH_LONG).show();
            if (dialog.isShowing())
            {
                dialog.dismiss();
            }
            return;
        }
        if (dialog.isShowing())
        {
            dialog.dismiss();
        }
        Toast toast;
        if(methodToRun.equals("createUserDirectory"))
        {
            if(cloudMode)
            {
                if(taskCompleted)
                {
                    toast = Toast.makeText(mContext, "Success, cloud connect activated!", Toast.LENGTH_LONG);
                    //access to cloud menu
                }
                else
                {
                    toast = Toast.makeText(mContext, "Connection failed, try again", Toast.LENGTH_LONG);
                    //delete the url file
                    CloudURLHandler handler = new CloudURLHandler(mContext,username,"");
                    handler.writeUrlToUserFile();
                    //send user to choose Cloud connection
                    mContext.startActivity(new Intent(mContext, CloudUserChooser.class));
                }
                toast.show();

            }
            else
            {
                if (taskCompleted)
                    toast = Toast.makeText(mContext, "SyncBox added successfully", Toast.LENGTH_LONG);
                else
                {
                    final SQLiteNameStorer db = new SQLiteNameStorer(mContext);
                    db.deleteUser(username);
                    toast = Toast.makeText(mContext, "Sync Unsuccessful, try again", Toast.LENGTH_LONG);
                }

                toast.show();
                mContext.startActivity(new Intent(mContext, HomeScreen.class));
            }
        }
        else if(methodToRun.equals("backUpUserChosenDirectories"))
        {
            if(taskCompleted)
            {
                toast = Toast.makeText(mContext, numFilesUploaded + " files uploaded successfully", Toast.LENGTH_LONG);
                toast.show();
            }
        }
        else if(methodToRun.equals("backUpSingleFile") || methodToRun.equals("backUpToSharedFolder"))
        {
            if(taskCompleted)
            {
                toast = Toast.makeText(mContext, "File uploaded successfully", Toast.LENGTH_LONG);
                toast.show();
            }
            else
                toast = Toast.makeText(mContext, "Upload failed, try again!", Toast.LENGTH_LONG);
                toast.show();
        }
        else if(methodToRun.equals("cleanupFolders"))
        {
            if(taskCompleted)
            {
                toast = Toast.makeText(mContext, numFilesDeleted + " files cleaned, totaling " +
                        mbDeleted + "mb", Toast.LENGTH_LONG);
                toast.show();
            }
            else
            {
                toast = Toast.makeText(mContext, "Cleanup failed", Toast.LENGTH_LONG);
                toast.show();
            }
        }
        else if(methodToRun.equals("backUpAllUsers"))
        {
            if(taskCompleted)
            {
                //go to Homescreen
                Intent intent = new Intent(mContext, HomeScreen.class);
                mContext.startActivity(intent);
                toast = Toast.makeText(mContext, "All users synced, " + numFilesUploaded + " files uploaded", Toast.LENGTH_LONG);
                toast.show();
            }
        }
        else if(methodToRun.equals("checkGetFilesOnPi"))
        {
            if(taskCompleted)
            {
                String requestedPath = androidFilePath;
                //Start the new activity with the arraylist passed in
                Intent intent = new Intent(mContext, SyncBoxDirectoryBrowser.class);
                intent.putExtra("username", username);
                intent.putStringArrayListExtra("fileList", filesForPiBrowser);
                intent.putExtra("currentPath", requestedPath);
                mContext.startActivity(intent);
            }
            else
            {
                toast = Toast.makeText(mContext, "Could not connect! Please try again", Toast.LENGTH_LONG);
                toast.show();
            }
        }
        else if(methodToRun.equals("downloadFile"))
        {
            if(taskCompleted)
            {
                toast = Toast.makeText(mContext, "Download complete, check your Downloads folder!", Toast.LENGTH_LONG);
                toast.show();
            }
            else
                toast = Toast.makeText(mContext, "Download failed", Toast.LENGTH_LONG);
                toast.show();
        }
    }
    @Override
    protected String doInBackground(String... params)
    {
        //pass a string as a parameter, depending on that
        //string run the required method from CommunicatorSSH
        //pass the username too, get user info from db

        methodToRun = params[0];
        username = params[1];

        final SQLiteNameStorer db = new SQLiteNameStorer(mContext);
        SyncBoxUser sbu = db.getUser(username);

        if(sbu != null)
        {
            ip = sbu.getIp();
            ip_password = sbu.getIp_password();
        }

        //Check for cloud mode, change IP if is
        if(cloudMode)
        {
            CloudURLHandler cloudURLHandler = new CloudURLHandler(mContext,username);
            String url = cloudURLHandler.readURL();
            ip = url;
        }
        //load values in from sbu
        CommunicatorSSH communicator = new CommunicatorSSH(ip,ip_password,22,username,mContext);


        if(methodToRun.equals("createUserDirectory"))
        {
            taskCompleted = communicator.createUserDirectory(username);

        }

        //test usb drive getter
        //else if(methodToRun.equals("getUsbDrivePath"))
        //{
         //   taskCompleted = communicator.getUsbDrivePath();
        //}

        //Backup all user chosen directories to pi
        else if(methodToRun.equals("backUpUserChosenDirectories"))
        {
            //false here is for cleanup mode
            //empty string is for miliseconds for clean mode only
            taskCompleted = communicator.backUpUserChosenDirectories(this,false, "");
            if(taskCompleted)
            {
                numFilesUploaded = communicator.getNumFilesUploaded();
            }
        }

        else if(methodToRun.equals("backUpSingleFile"))
        {
            taskCompleted = communicator.backUpSingleFile(androidFilePath);
        }

        //Handle shared folder upload
        else if(methodToRun.equals("backUpToSharedFolder"))
        {
            taskCompleted = communicator.backUpToSharedFolder(androidFilePath);
        }

        //handle clean folders
        //Handle shared folder upload
        else if(methodToRun.equals("cleanupFolders"))
        {
            String dateInMilisString = params[3];
            //true here enables the cleanup mode
            //dateinmilis is used to determine what to delete
            taskCompleted = communicator.backUpUserChosenDirectories(this, true, dateInMilisString);

            if(taskCompleted)
            {
                numFilesDeleted = communicator.getNumFilesDeleted();
                mbDeleted = communicator.getMbDeleted();
            }
        }
        else if(methodToRun.equals("backUpAllUsers"))
        {
            taskCompleted = communicator.backUpAllUsers(this);

            if(taskCompleted)
            {
                numFilesUploaded = communicator.getNumFilesUploaded();
            }
        }
        //Add functionality for pi browser
        else if(methodToRun.equals("checkGetFilesOnPi"))
        {
            String requestedPath = androidFilePath;
            //need to pass the currently requested path here
            taskCompleted = communicator.checkGetFilesOnPi(requestedPath);

            if(taskCompleted)
            {
                filesForPiBrowser = communicator.getFilesForCloudBrowser();
            }
        }
        //Add functionality for pi browser
        else if(methodToRun.equals("downloadFile"))
        {
            String requestedPath = androidFilePath;
            taskCompleted = communicator.downloadFile(requestedPath);
        }

        //Check was there a usb inserted
        usbDriveResult = communicator.getUsbDrivePathString();

        //return a string here
        return "methodToRun Completed";
    }


    //Update progress bar from Async
    protected void onProgressUpdate(Integer... progress)
    {
        // setting progress percentage
        dialog.setProgress(progress[0]);
    }

    public void doProgress(int value)
    {
        publishProgress(value);
    }



}


