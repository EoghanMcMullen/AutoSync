package com.eoghanmcmullen.autosync;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 * Created by eoghanmcmullen on 13/04/2016.
 */
public class CommunicatorSSH
{
    private String ipAddress;
    private String ipPassword;
    private String ssid;
    private int portNum;
    private String username;
    private Context context;

    private Session session;
    private Channel channel = null;
    private ChannelSftp sftp;

    private String usbDrivePath;

    private int totalFileCount;
    private Double percentageToAddToProgress = 0.00;
    private Double backgroundPercentageTotal = 0.00;

    private long mbDeleted = 0L;
    private int numFilesDeleted = 0;

    private long mbUploaded = 0L;
    private int numFilesUploaded;

    private ArrayList filesForCloudBrowser;


    CommunicatorSSH(String ipAddress,String ipPassword, int portNum,
                    String username,Context context)
    {
        this.ipAddress = ipAddress;
        this.ipPassword = ipPassword;
        this.portNum = portNum;
        this.username = username;
        this. context = context;
    }

    //Method to open ssh communication
    private Boolean openSshCommunication()
    {
        Boolean connectionEstablished = true;
        try
        {
            //open up the ssh connection
            JSch jsch = new JSch();
            session = jsch.getSession("pi", ipAddress, portNum);
            session.setPassword(ipPassword);

            // Avoid asking for key confirmation
            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no");
            session.setConfig(prop);
            session.connect();


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
            //channel.setOutputStream(baos);
        } catch (JSchException e)
        {
            e.printStackTrace();
            connectionEstablished = false;
        }

        return connectionEstablished;
    }
    //method to close open connection
    private void closeConnection()
    {
        sftp.exit();
        session.disconnect();
        channel.disconnect();
    }

    //method to try create a user file on the raspberry pi
    public Boolean createUserDirectory(String username)
    {
        Boolean directoryCreated = false;
        Boolean checkConnection = openSshCommunication();

        if(checkConnection)
        {
            try
            {
                Boolean getUsb = getUsbDrivePath();

                if(getUsb)
                {
                    //this needs to be made on the usb storage
                    sftp.mkdir("/media/pi/" + usbDrivePath + "/" + username);
                    directoryCreated = true;
                    //closeConnection();
                }
            } catch (SftpException e)
            {
                //e.printStackTrace();
                directoryCreated = false;
            }
        }
        if(directoryCreated)
        {
            createDirectoryFile(username);
        }
        return directoryCreated;
    }
    //method to try create a user file on the raspberry pi
    //when we don't need to open and close ssh
    public Boolean createUserDirectorySafe(String username)
    {
        Boolean directoryCreated = false;
        try
        {
            Boolean getUsb = getUsbDrivePath();

            if(getUsb)
            {
                //this needs to be made on the usb storage
                sftp.mkdir("/media/pi/" + usbDrivePath + "/" + username);
                directoryCreated = true;
            }
        } catch (SftpException e)
        {
            directoryCreated = false;
        }
        if(directoryCreated)
        {
            createDirectoryFile(username);
        }
        return directoryCreated;
    }

    //method to return the name of the drive that is currently connected to the raspberry pi
    private Boolean getUsbDrivePath()
    {
        Vector<ChannelSftp.LsEntry> fileList;

        try
        {
            //get the directories in the /media/pi/
            //this is where usb gets mounted to
            fileList = sftp.ls("/media/pi/");

            for(ChannelSftp.LsEntry ls:fileList)
            {
                if(!ls.getFilename().contains("SETTINGS")
                        &&!ls.getFilename().equals(".")
                        &&!ls.getFilename().equals(".."))
                {
                    usbDrivePath = ls.getFilename();
                }
            }

        } catch (SftpException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //pass an open channel
    public static void createDirectory(ChannelSftp sftp,String directory)
    {
        try
        {
            sftp.mkdir(directory);
        } catch (SftpException e)
        {
            //e.printStackTrace();
        }
    }

    //copy from and copyTo must be full path i.e. include filename at end
    public static void copyOverFile(ChannelSftp sftp, String copyFrom, String copyTo)
    {
        try
        {
            sftp.put(copyFrom, copyTo);
        } catch (SftpException e)
        {
            e.printStackTrace();
        }
    }

    //copy from and copyTo must be full path i.e. include filename at end
    public static void removeFile(ChannelSftp sftp, String fileToRemovePath)
    {
        try
        {
            sftp.rm(fileToRemovePath);
        } catch (SftpException e)
        {
            e.printStackTrace();
        }
    }

    //method to create parent directories
    public static void createParentFolders(ChannelSftp sftp, String directory,String usbDrivePath,String username)
    {
        //need to create nested folders manually
        //-------------------------------------------------
        String pathBeforeFolders = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        String path = (directory.replace(pathBeforeFolders,""));
        String [] pathSplit = path.split("\\/");
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < pathSplit.length;i++)
        {
            sb.append("/" + pathSplit[i]);
            createDirectory(sftp, "/media/pi/" + usbDrivePath + "/" + username + sb.toString());
        }
        //--------------------------------------------------
    }

    //Method to begin transferring folders chosen by user
    //Need the date chosen too
    //return a count of how much memory freed up/files deleted
    //Time in milis is for cleanup date
    //Add in boolean to check if we are doing cleanup
    public Boolean backUpUserChosenDirectories(SSHBackgroundWorker backgroundWorker,
                                               Boolean cleanupMode, String timeInMilisString)
    {
        Boolean taskComplete = false;
        UsersDirectoryChoosingHandler directoryHandler;
        Long timeInMilis = 0L;

        //FOR CLEANUP can change the filename here to be the cleanup file with an if statement

        if(cleanupMode)
        {
            //get the directories from the directory file for the user
            directoryHandler =
                    new UsersDirectoryChoosingHandler(context,username + "CleanupFile");

            timeInMilis = Long.parseLong(timeInMilisString);
        }
        else
        {
            //get the directories from the directory file for the user
            directoryHandler = new UsersDirectoryChoosingHandler(context,username);
        }
        //populate directory path array
        ArrayList<String> directoryPaths = directoryHandler.readDirectoryList();
        //remove all unnecessary child directories
        ArrayList<String> cleanedDirectoryPaths = directoryHandler.removeChildDirectories(directoryPaths);

        //open up the sftp connection
        Boolean connectionEstablised = openSshCommunication();

        if(connectionEstablised)
        {
            //for each directory traverse and do appropriate actions
            File file;

            String [] pathSplit;
            String path;

            //Should ensure user directory is created
            createUserDirectorySafe(username);

            //get the total number of files to upload.

            for(String s:cleanedDirectoryPaths)
            {
                file = new File(s);
                countFiles(file);
            }
            percentageToAddToProgress = 100.00/totalFileCount;

            //get the usb drive path to use
            if(getUsbDrivePath())
            {
                //If no folders set up yet
                if(cleanedDirectoryPaths.size() == 0)
                    return false;
                //traverse each parent directory and add files to device
                for (String directory : cleanedDirectoryPaths)
                {
                    createParentFolders(sftp, directory,usbDrivePath,username);
                    file = new File(directory);
                    traverse(sftp, file,usbDrivePath,backgroundWorker,cleanupMode,timeInMilis);
                }
            }
            taskComplete = true;
            closeConnection();
        }

        return taskComplete;
    }

    //traverse each directory in the directory backup list
    //main worker function
    //creates directories on pi where needed
    //copies over files when appropriate
    //checks if updates needed
    public void traverse (ChannelSftp sftp, File dir, String usbDrivePath,
                          SSHBackgroundWorker backgroundWorker,Boolean cleanupMode, Long timeInMilis)
    {
        String EnvironmentPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        SftpATTRS fileAttributes = null;
        Vector<ChannelSftp.LsEntry> fileList;
        String pathBeforeFolders = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

        //Long for getting file size
        long fileSizeInMB;

        if (dir.exists())
        {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                File file = files[i];
                if (file.isDirectory())
                {
                    try
                    {
                        String s = file.getCanonicalPath();
                        String folderPath = s.replace(EnvironmentPath,"");
                        //create the directory on the pi
                        createDirectory(sftp, "/media/pi/" + usbDrivePath + "/" + username + "/" + folderPath);
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    //recursively move into the next directory
                    traverse(sftp, file, usbDrivePath, backgroundWorker, cleanupMode, timeInMilis);
                }
                //or else its a file
                else
                {

                    try
                    {
                        //remove filename from path
                        String filePathOnAndroid = file.getCanonicalPath();
                        String folderPathOnAndroid = filePathOnAndroid.substring(0,
                                filePathOnAndroid.lastIndexOf(File.separator));
                        String folderPathOnPi = folderPathOnAndroid.replace(pathBeforeFolders, "");
                        String fileNameOnly = FilenameUtils.getName(file.toString());


                        try
                        {
                            //check if file exists on pi
                            fileList = sftp.ls("/media/pi/" +usbDrivePath + "/" + username + "/" + folderPathOnPi);

                            //check file size against current file
                            Boolean inPiStorage = false;
                            String lsFileName ="";

                            for(ChannelSftp.LsEntry ls: fileList)
                            {
                                lsFileName = ls.getFilename();

                                if(lsFileName.equals(fileNameOnly))
                                {
                                    inPiStorage = true;
                                    fileAttributes = ls.getAttrs();
                                    break;
                                }

                            }
                            if(inPiStorage)
                            {
                                //check file for update
                                long piFileSize = fileAttributes.getSize();
                                long androidFileSize = file.length();

                                if(piFileSize != androidFileSize)
                                {
                                    //remove  old file, write new one
                                    removeFile(sftp, "/media/pi/" + usbDrivePath
                                            + "/" + username + "/" + folderPathOnPi + "/" + fileNameOnly);
                                    copyOverFile(sftp, filePathOnAndroid, "/media/pi/" + usbDrivePath
                                            + "/" + username + "/" + folderPathOnPi + "/" + fileNameOnly);

                                }

                            }
                            else if(!inPiStorage)
                            {
                                //add the file
                                copyOverFile(sftp, filePathOnAndroid, "/media/pi/" + usbDrivePath
                                        + "/" + username + "/" + folderPathOnPi + "/" + fileNameOnly);

                            }

                            //CLEANUP MODE DELETE HERE
                            if(cleanupMode)
                            {
                                Date lastModDate = new Date(file.lastModified());
                                Long dateModified = file.lastModified();

                                if(dateModified < timeInMilis)
                                {
                                    //get file size
                                    fileSizeInMB = file.length() / (1024 * 1024);
                                    //Delete the file
                                    deleteFile(filePathOnAndroid);

                                    mbDeleted += fileSizeInMB;
                                    numFilesDeleted++;

                                }
                            }

                            //update progress here
                            backgroundPercentageTotal += percentageToAddToProgress;
                            backgroundWorker.doProgress((int) Math.ceil(backgroundPercentageTotal));

                            //Increse number of files handled
                            numFilesUploaded++;


                        } catch (Exception e)
                        {
                            //e.printStackTrace();
                        }
                    } catch (IOException e)
                    {
                       // e.printStackTrace();
                    }

                }
            }
        }
    }
    //Count number of files in directories
    public int countFiles (File dir)
    {
        if (dir.exists())
        {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                File file = files[i];
                if (file.isDirectory())
                {
                    //recursively move into the next directory
                    countFiles(file);
                }
                //or else its a file
                else
                {
                    totalFileCount++;
                }
            }
        }
        return totalFileCount;
    }

    //Back up a single file
    public Boolean backUpSingleFile(String fileWithAndroidPath)
    {
        //open connection
        Boolean connectionEstablised = openSshCommunication();

        if(connectionEstablised)
        {
            //create user directory just in case
            //Also gets usbDrivePath
            createUserDirectorySafe(username);

            String[] foldersAndFile = breakupAndroidPath(fileWithAndroidPath);

            //Create directorys where necessary, we know last is the file
            int count = foldersAndFile.length;

            //while the count != length-1 we need folders
            StringBuilder sb = new StringBuilder();

            //create directory from there
            //including user folder
            for (int i = 0; i < count - 1; i++)
            {
                sb.append(foldersAndFile[i] + "/");

                createDirectory(sftp, "/media/pi/" + usbDrivePath
                        + "/" + username + "/" + sb.toString());
            }

            String filename = foldersAndFile[foldersAndFile.length - 1];

            //copy over file
            copyOverFile(sftp, fileWithAndroidPath, "/media/pi/" + usbDrivePath
                    + "/" + username + "/" + sb.toString() + filename);

            closeConnection();

        }
        else return false;

        return true;
    }
    public String[] breakupAndroidPath(String fileWithAndroidPath)
    {
        //remove storage/Emu.....
        String pathBeforeFolders = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        String path = (fileWithAndroidPath.replace(pathBeforeFolders,""));

        String[] allPaths = path.split("/");
        return  allPaths;
    }


    //Method to backup to the shared folder

    //Back up a single file
    public Boolean backUpToSharedFolder(String fileWithAndroidPath)
    {
        //open connection
        Boolean connectionEstablised = openSshCommunication();

        if(connectionEstablised)
        {
            //Create Shared directory
            createUserDirectorySafe("Shared");

            String[] foldersAndFile = breakupAndroidPath(fileWithAndroidPath);
            String filename = foldersAndFile[foldersAndFile.length - 1];

            //copy over file
            copyOverFile(sftp, fileWithAndroidPath, "/media/pi/" + usbDrivePath
                    + "/" + "Shared" + "/" + filename);

            closeConnection();

        }
        else return false;

        return true;
    }

    private void deleteFile(String inputPath)
    {
        try
        {
            // delete the original file
            new File(inputPath).delete();


        } catch (Exception e)
        {
            Log.e("tag", e.getMessage());
        }
    }

    public Boolean backUpAllUsers(SSHBackgroundWorker backgroundWorker)
    {
        Boolean taskComplete = true;
        //Get connected ipAddress
        WifiManager wifiManager =
                (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        final DhcpInfo dhcp = wifiManager.getDhcpInfo();
        final String currentIpAddress = Formatter.formatIpAddress(dhcp.gateway);

        SQLiteNameStorer sqlDB = new SQLiteNameStorer(context);

        if(sqlDB.anyUsersAvailable())
        {
            //get all connected syncboxes from database
            List<SyncBoxUser> allUsers = sqlDB.getAllUsers();
            String userIpAddress = "";
            int numUsers = allUsers.size();

            for(SyncBoxUser sbu: allUsers)
            {
                //check if ip address matches any
                //if they do return true
                username = sbu.getUsername();
                userIpAddress = sbu.getIp();
                ipAddress = userIpAddress;
                ipPassword = sbu.getIp_password();
                backgroundWorker.doProgress(0);

                if(userIpAddress.equals(currentIpAddress))
                {
                    //change the username
                    //false for cleanup
                    //empty string for time, we don't need it
                    //if taskcomplete changes to false the sync failed
                    taskComplete = backUpUserChosenDirectories(backgroundWorker,
                            false, "");
                    //reset progress
                }

            }
            backgroundWorker.doProgress(100);

        }

        return taskComplete;
    }
    //Method to add adirectory file for new users when new usb plugged in
    public void createDirectoryFile(String username)
    {
        UsersDirectoryChoosingHandler handler = new UsersDirectoryChoosingHandler(context, username);
        handler.createDirectoryFile();
    }

    public boolean checkGetFilesOnPi(String path)
    {
        ArrayList allFiles = getFilesOnPi(path);

        if(allFiles != null)
        {
            filesForCloudBrowser = allFiles;
            return true;
        }
        return false;
    }

    private ArrayList getFilesOnPi(String path)
    {
        ArrayList allFiles = new ArrayList();
        Vector<ChannelSftp.LsEntry> fileList;

        //Open ssh communication
        Boolean connectionEstablished = openSshCommunication();
        if(connectionEstablished)
        {
            if(getUsbDrivePath())
            {
                try
                {
                    fileList = sftp.ls("/media/pi/" + usbDrivePath + "/" + path);

                    for(ChannelSftp.LsEntry ls: fileList)
                    {
                        allFiles.add(ls.getFilename());
                    }

                } catch (SftpException e)
                {
                    e.printStackTrace();
                }
            }

            closeConnection();
        }


        return allFiles;
    }

    //For single file download
    public boolean downloadFile(String path)
    {
        //Open ssh communication
        Boolean connectionEstablished = openSshCommunication();
        Boolean success = true;
        File dwn = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String downloadsPath = dwn.getAbsolutePath();
        if(connectionEstablished)
        {
            if(getUsbDrivePath())
            {
                try
                {
                    sftp.get("/media/pi/" + usbDrivePath + "/" + path, downloadsPath);
                } catch (SftpException e)
                {
                    success = false;
                    e.printStackTrace();
                }
                closeConnection();
            }
        }

        return success;
    }

    //so we can get the mb deleted
    public long getMbDeleted()
    {
        return mbDeleted;
    }
    //Return count of files deleted
    public int getNumFilesDeleted()
    {
        return numFilesDeleted;
    }

    public int getNumFilesUploaded()
    {
        return numFilesUploaded;
    }

    public ArrayList getFilesForCloudBrowser() { return filesForCloudBrowser; }

    public String getUsbDrivePathString()
    {
        if(usbDrivePath == null)
            return "";
        else return usbDrivePath;
    }
}
