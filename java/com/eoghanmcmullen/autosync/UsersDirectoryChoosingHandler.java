package com.eoghanmcmullen.autosync;

import android.content.Context;
import android.os.Environment;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by eoghanmcmullen on 15/04/2016.
 */
public class UsersDirectoryChoosingHandler
{
    //pass username so can create a file with that name and read from it

    private String username;
    private Context context;
    private ArrayList<String> fList;

    UsersDirectoryChoosingHandler(Context context, String username)
    {
        this.context = context;
        this.username = username;
    }

    //method to write an arraylist to the directory_paths file
    void writeDirectoriesToFile(int writeMode, ArrayList<String> directories)
    {
        fList = directories;
        FileOutputStream fos = null;
        String Directories_filename = username + context.getResources().getString(R.string.directory_paths_filename);
        String directory_path = "";

        try
        {
            fos = context.openFileOutput(Directories_filename, writeMode);
            //write the directory path to the file if it's not currently in it
            for(String folder:fList)
            {
                if(isDirectoryInFile(folder) == false)
                {
                    directory_path = folder + "%nextDirectory%";
                    fos.write(directory_path.getBytes());
                }
            }
            if(fos != null)
                fos.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //Checks if directory is in the internal file already
    Boolean isDirectoryInFile(String directory)
    {
        Boolean inFile = false;
        ArrayList directoryPaths = readDirectoryList();

        if(directoryPaths.contains(directory))
        {
            inFile = true;
        }
        return inFile;
    }

    //method to read all user chosen directories
    ArrayList<String> readDirectoryList()
    {
        String result = "";
        ArrayList<String> DirectoryInfo = new ArrayList<>();
        Boolean firstIteration = true;

        //get directory paths from storage
        String Directories_filename = username + context.getResources().getString(R.string.directory_paths_filename);

        try
        {
            FileInputStream fis = context.openFileInput(Directories_filename);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            //read to end of file
            while ((line = bufferedReader.readLine()) != null)
            {
                sb.append(line).append("");
            }
            result = sb.toString();

            fis.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        //split into individual strings
        String[] allPaths = result.split("%nextDirectory%");

        //ensure no space at start, some default of the file?

        for(String s:allPaths)
        {
            if(firstIteration == true)
            {
                if(!s.equals(""))
                {
                    if(!DirectoryInfo.contains(s))
                    {
                        DirectoryInfo.add(s);
                    }
                }
            }
            else
            {
                if(!DirectoryInfo.contains(s))
                {
                    DirectoryInfo.add(s);
                }
            }
            firstIteration = false;
        }

        return DirectoryInfo;
    }

    //remove all path except last folder
    ArrayList<String> removeFullPathName(ArrayList<String> fList)
    {
        ArrayList<String> listOfFolders = new ArrayList<>();
        int lastLetterIndex = 0;

        if(!fList.isEmpty())
        {
            for (String s : fList)
            {
                lastLetterIndex = s.length() - 1;
                char lastLetter = s.charAt(lastLetterIndex);
                String lastL = lastLetter + "";

                StringBuilder sb = new StringBuilder();

                for (int i = lastLetterIndex; !lastL.equals("/"); i--)
                {
                    lastL = s.charAt(i) + "";
                    sb.append(lastL);
                }

                //add folder name without "/"s
                listOfFolders.add(sb.reverse().toString().substring(1));
            }
        }

        return listOfFolders;
    }

    //method to remove all unnessesary child directories

    public ArrayList<String> removeChildDirectories(ArrayList<String> userChosenDirectories)
    {
        //gets global user selected directories

        //returns storage in use, need "/" on end
        String pathBeforeFolders = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        ArrayList<String> cleanedDirectories = new ArrayList<>();

        for (String s : userChosenDirectories)
        {
            //remove Environment path
            cleanedDirectories.add(s.replace(pathBeforeFolders,""));
        }

        String [] directorySplit;
        String [] otherDirectorySplit;
        Boolean aParent = true;

        ArrayList<String> theRemoveables = new ArrayList<>();

        for(String directory : cleanedDirectories)
        {
            directorySplit = directory.split("\\/");

            for(String otherDirectory:cleanedDirectories)
            {
                if (!theRemoveables.contains(otherDirectory) && !directory.equals(otherDirectory))
                {
                    otherDirectorySplit = otherDirectory.split("\\/");

                    //are they in the same base folder
                    if(directorySplit[0].equals(otherDirectorySplit[0]))
                    {
                        if(directorySplit.length < otherDirectorySplit.length)
                        {
                            aParent = true;
                            for(int i = 1; i < directorySplit.length;i++)
                            {
                                if(directorySplit[i].equals(otherDirectorySplit[i]))
                                {

                                }
                                else aParent = false;
                            }
                            if(aParent = true)
                            {
                                if(!theRemoveables.contains(otherDirectory))
                                {
                                    theRemoveables.add(otherDirectory);
                                }
                            }

                        }
                    }
                }

            }
        }
        //remove all entries in removables from cleanedDirectories
        for(String s : theRemoveables)
        {
            if(cleanedDirectories.contains(s))
            {
                cleanedDirectories.remove(cleanedDirectories.indexOf(s));
            }
        }

        //add the full path back to each filename
        for(String s : cleanedDirectories)
        {
            cleanedDirectories.set(cleanedDirectories.indexOf(s), pathBeforeFolders + s);
        }

        return cleanedDirectories;
    }
    //Create a file for new users
    public void createDirectoryFile()
    {
        FileOutputStream fos = null;
        String Directories_filename = username + context.getResources().getString(R.string.directory_paths_filename);
        String directory_path = "";

        try
        {
            fos = context.openFileOutput(Directories_filename, Context.MODE_APPEND);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }


        if(fos != null)
            try
            {
                fos.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }


    }
}
