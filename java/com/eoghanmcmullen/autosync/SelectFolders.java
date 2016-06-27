package com.eoghanmcmullen.autosync;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SelectFolders extends AppCompatActivity
{
    private ArrayList<String> fList;
    private ArrayList<String> directoriesWithoutPath;
    private CustomFolderRowAdapter adapter;
    private String username;
    private String chosenDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_folders);

        //back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get username from previous activity
        username = getIntent().getExtras().getString("username");

        //Set up dynamic arraylist for adding directories
        fList = readDirectoryList();
        directoriesWithoutPath = removeFullPathName(fList);

        //Custom adapter to display rows in listview
        adapter = new CustomFolderRowAdapter(this,directoriesWithoutPath);

        //get references to items on screen
        ListView listOfFolders = (ListView) findViewById(R.id.folderListView);
        Button selectFolderButton = (Button) findViewById(R.id.selectFolderButton);
        Button doneButton = (Button) findViewById(R.id.doneButton);

        //add items to listview onload
        listOfFolders.setAdapter(adapter);

        //add functionality for selecting a directory
        selectFolderButton.setOnClickListener(new OnClickListener()
        {
            String m_chosen = "";
            @Override
            public void onClick(View v)
            {
                //Now we start an activity for a result
                Intent intent = new Intent(SelectFolders.this, ChooseDirectory.class);
                //Add in whether we want directoryMode or fileMode
                intent.putExtra("selectionMode", "directoryMode");
                intent.putExtra("callingFunction", "SelectFolders");
                startActivityForResult(intent, 2);

            }
        });

        //when clicked adds the list to a file so can be read later in app
        doneButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                writeDirectoriesToFile(MODE_APPEND);
                Toast.makeText(SelectFolders.this," Folders updated successfully", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(SelectFolders.this, SyncedDeviceMenu.class);
                intent.putExtra("username", username);
                v.getContext().startActivity(intent);
            }
        });
    }


    //method to write an arraylist to the directory_paths file
    void writeDirectoriesToFile(int writeMode)
    {
        FileOutputStream fos = null;
        String Directories_filename = username + getResources().getString(R.string.directory_paths_filename);
        String directory_path = "";

        try
        {
            fos = openFileOutput(Directories_filename, writeMode);
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

    ArrayList<String> readDirectoryList()
    {
        String result = "";
        ArrayList<String> DirectoryInfo = new ArrayList<>();
        Boolean firstIteration = true;

        //get directory paths from storage
        String Directories_filename = username + getResources().getString(R.string.directory_paths_filename);

        try
        {
            FileInputStream fis = openFileInput(Directories_filename);
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

    String removeFullPathString(String s)
    {
        int lastLetterIndex = s.length() - 1;
        char lastLetter = s.charAt(lastLetterIndex);
        String lastL = lastLetter + "";

        StringBuilder sb = new StringBuilder();

        for(int i = lastLetterIndex; !lastL.equals("/"); i--)
        {
            lastL = s.charAt(i) + "";
            sb.append(lastL);
        }

        return (sb.reverse().toString().substring(1));
    }

    //run from custom adapter, works for individual delete buttons
    void removeFolderButton(int position, String folderName)
    {
        String folderPath = fList.get(position);

        //remove from file and arrays
        directoriesWithoutPath.remove(position);
        fList.remove(position);

        //Mode_private overwrites previous file
        writeDirectoriesToFile(MODE_PRIVATE);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed()
    {
        //go back to connected sync box screen

        Intent intent = new Intent(this, SyncedDeviceMenu.class);
        intent.putExtra("username", username);
        this.startActivity(intent);
    }

    //Retrieve the result from the folder chooser
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==2)
        {
            String directoryPath = data.getStringExtra("directoryPath");

            //If nothing returned then was cancelled so do nothing
            if(directoryPath.equals(""))
            {

            }
            else
            {
                chosenDirectory = directoryPath;

                if (isDirectoryInFile(chosenDirectory) || fList.contains(chosenDirectory))
                {
                    Toast.makeText(SelectFolders.this, chosenDirectory +
                            " has already been added to the list", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    fList.add(chosenDirectory);
                    directoriesWithoutPath.add(removeFullPathString(chosenDirectory));
                    adapter.notifyDataSetChanged();
                }
            }
        }
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
                startActivity(new Intent(SelectFolders.this, HomeScreen.class));
                return true;
            //actionbar up button
            case android.R.id.home:
                Intent intent = new Intent(this, SyncedDeviceMenu.class);
                intent.putExtra("username", username);
                this.startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
