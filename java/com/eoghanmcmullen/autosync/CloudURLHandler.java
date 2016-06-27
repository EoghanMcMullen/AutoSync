package com.eoghanmcmullen.autosync;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by eoghanmcmullen on 19/05/2016.
 */
public class CloudURLHandler
{
    //pass username so can create a file with that name and read from it

    private String username;
    private Context context;
    private String url;
    private ArrayList<String> fList;

    CloudURLHandler(Context context, String username)
    {
        this.context = context;
        this.username = username;
    }

    //For when we are adding the url
    CloudURLHandler(Context context, String username, String url)
    {
        this.context = context;
        this.username = username;
        this.url = url;
    }

    //Write url to it's file
    void writeUrlToUserFile()
    {
        FileOutputStream fos = null;
        String urlFilename = username + context.getResources().getString(R.string.user_URL_path);

        try
        {
            //MODE_PRIVATE overwrites
            fos = context.openFileOutput(urlFilename, Context.MODE_PRIVATE);
            fos.write(url.getBytes());

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
    //method to read all user chosen directories
    String readURL()
    {
        String url = "";

        //get directory paths from storage
        String urlFilename = username + context.getResources().getString(R.string.user_URL_path);

        try
        {
            FileInputStream fis = context.openFileInput(urlFilename);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            //read to end of file
            while ((line = bufferedReader.readLine()) != null)
            {
                sb.append(line).append("");
            }
            url = sb.toString();

            fis.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return url;
    }
}
