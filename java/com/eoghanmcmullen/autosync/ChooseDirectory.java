package com.eoghanmcmullen.autosync;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class ChooseDirectory extends AppCompatActivity
{
    private ArrayList<String> allFiles;
    private String rootDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
    private CustomDirectoryChooserAdapter adapter;
    private DirectoryPopulator dp = new DirectoryPopulator();
    private TextView currentDirectoryText;
    private TextView currentFolderText;
    private Button acceptChoice;
    private Button cancelButton;
    private String selectionMode;
    private String functionThatCalledChooser;

    private String pathToReturn = rootDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_choose_directory_list);

        //Get the mode, directory or file from previous intent
        selectionMode = getIntent().getExtras().getString("selectionMode");

        //back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(selectionMode.equals("fileMode"))
            setTitle("Choose a file");
        else
            setTitle("Choose a folder");

        //Get the function that called the chooser so we can pass back to it
        functionThatCalledChooser = getIntent().getExtras().getString("callingFunction");

        //References to on screen items
        GridView gridFiles = (GridView) findViewById(R.id.filesGridView);
        currentDirectoryText = (TextView) findViewById(R.id.currentDirectoryText);
        acceptChoice = (Button) findViewById(R.id.acceptChooserButton);
        cancelButton = (Button) findViewById(R.id.cancelChooserButton);

        currentFolderText = (TextView) findViewById(R.id.CurrentFolderLabelTextView);
        currentDirectoryText.setContentDescription("Current folder Home");

        allFiles = dp.getFileList(rootDirectory);

        //set to home instead of Enviroment, easier for user
        currentDirectoryText.setText("Home");

        //Allow fastscroll
        gridFiles.setFastScrollEnabled(true);

        //alphabetically sort the arraylist
        Collections.sort(allFiles, String.CASE_INSENSITIVE_ORDER);

        //Custom adapter to display rows in GridView
        adapter = new CustomDirectoryChooserAdapter(this,allFiles);

        //add items to gridview onload
        gridFiles.setAdapter(adapter);





        //Accept button should accept a directory and return it to calling class
        //when clicked adds the list to a file so can be read later in app
        acceptChoice.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                //only allow file to be returned if selctionMode if filemode

                File f = new File(pathToReturn);
                Intent intent = new Intent();

                //Deal with fileMode
                if(selectionMode.equals("fileMode"))
                {
                    if(f.isFile())
                    {
                        intent.putExtra("directoryPath", pathToReturn);
                        intent.putExtra("callingFunction", functionThatCalledChooser);
                        setResult(2,intent);
                        finish();
                    }
                    else Toast.makeText(ChooseDirectory.this,
                            "Please select a file", Toast.LENGTH_LONG).show();
                }
                //Deal with Directory mode
                else if(selectionMode.equals("directoryMode"))
                {
                    if(f.isDirectory())
                    {
                        intent.putExtra("directoryPath", pathToReturn);
                        intent.putExtra("callingFunction", functionThatCalledChooser);
                        setResult(2,intent);
                        finish();
                    }
                    else Toast.makeText(ChooseDirectory.this,
                        "Please select a folder", Toast.LENGTH_LONG).show();
                }


            }
        });

        //Cancel button should return an empty string,
        //Calling class then knows it was a cancellation
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Intent intent = new Intent();
                intent.putExtra("directoryPath","");
                intent.putExtra("callingFunction", functionThatCalledChooser);

                setResult(2,intent);
                finish();
            }
        });
    }


    //Called from the custom adapter, tells us which item was clicked
    public void itemClickHandler(String path)
    {
        //we need to check if a file or directory was picked
        File f = new File(path);

        if(f.isDirectory())
        {
            currentFolderText.setText("Current Folder:");
            currentFolderText.setContentDescription("Current folder label");
            //Need to use a temp as adapter point to the memory location so can't have new arraylist
            //need to clear old one and add to it
            ArrayList<String> tempFileList = dp.getFileList(path);
            allFiles.clear();

            for(String s: tempFileList)
            {
                allFiles.add(s);
            }
            //Sort the list
            Collections.sort(allFiles, String.CASE_INSENSITIVE_ORDER);

            pathToReturn = path;
            currentDirectoryText.setText(path.replace("/storage/emulated/0", "Home"));
            currentDirectoryText.setContentDescription("Current Folder " + path.replace("/storage/emulated/0", "Home"));
            //tell the adapter that new data available
            adapter.notifyDataSetChanged();
        }
        else if(f.isFile())
        {
            String pathRemoved = removePath(path);
            //check if in file chooser mode
            if(selectionMode.equals("fileMode"))
            {
                //add the file to the textview, allow user to accept
                pathToReturn = path;
                currentDirectoryText.setText(path.replace("/storage/emulated/0", "Home"));
                currentDirectoryText.setContentDescription("File selected " + pathRemoved);

                currentFolderText.setText("Selected File:");
                currentFolderText.setContentDescription("Current selected file label");

            }
            else
            {
                //Show a toast asking for a directory
                Toast.makeText(ChooseDirectory.this,
                        "Please select a folder", Toast.LENGTH_LONG).show();
            }


        }

    }

    //Back button should bring to the previous directory
    @Override
    public void onBackPressed()
    {
        String directoryPath = pathToReturn;

        File f = new File(directoryPath);

        if(directoryPath.equals(rootDirectory))
        {
            //Don't do anything, user can use cancel button
        }
        else
        {
            //Need to check for fileMode
            //if filemode deal with filename

            //get previous directory
            String arr[] = directoryPath.split("\\/");

            ArrayList<String> allPaths = new ArrayList<>();

            for(String s : arr)
            {
                if(!s.equals(""))
                {
                    allPaths.add(s);
                }
            }

            StringBuilder sb = new StringBuilder();

            //Check for file selected here
            //if a file do allPaths - 2
            int counter;

            //Added allPaths file size check to see if a user is at the root directory
            if(f.isFile() && allPaths.size() > 4)
            {
                counter = 2;
            }
            else counter = 1;

            for(int i = 0;i < allPaths.size() - counter;i++)
            {
                sb.append("/" + allPaths.get(i));
            }

            itemClickHandler(sb.toString());
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
                startActivity(new Intent(ChooseDirectory.this, HomeScreen.class));
                return true;
            //actionbar up button
            case android.R.id.home:
                Intent intent = new Intent();
                intent.putExtra("directoryPath","");
                intent.putExtra("callingFunction", functionThatCalledChooser);
                setResult(2,intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String removePath(String path)
    {
        String arr[] = path.split("\\/");
        ArrayList<String> allDirs = new ArrayList<>();

        for(String s:arr)
        {
            if(!s.equals(""))
            {
                allDirs.add(s);
            }
        }

        return allDirs.get(allDirs.size()-1);
    }
}
