package com.eoghanmcmullen.autosync;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by eoghanmcmullen on 27/04/2016.
 */
public class DirectoryPopulator
{
    DirectoryPopulator()
    {}

    public ArrayList<String> getFileList(String directoryPath)
    {
        //add all files/folders from directory path to the arraylist
        File f = null;
        ArrayList<String> allFiles = new ArrayList<>();
        try
        {
            f = new File(directoryPath);
            allFiles = new ArrayList<String>(Arrays.asList(f.list()));

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        //return arraylist without path added on so need to add the directory path to each
        //e.g. /storage/emulated/0/
        String fullPath = "";
        ArrayList<String> allFullPaths = new ArrayList<>();

        for(String s: allFiles)
        {
            if(s.charAt(0) != '.')
            {
                fullPath = directoryPath + "/" + s;
                allFullPaths.add(fullPath);
            }
        }
        return allFullPaths;
    }
}
