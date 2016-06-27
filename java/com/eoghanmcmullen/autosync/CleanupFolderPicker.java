package com.eoghanmcmullen.autosync;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CleanupFolderPicker extends AppCompatActivity
{
    private String username;
    private WifiManager wifimanager;
    private WifiInfo wifiInfo;
    private TextView folderName;

    private ImageButton calendarButton;
    private Calendar cal;
    private int day;
    private int month;
    private int year;
    private TextView date;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cleanup_folder_picker);

        //back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button AddFolderButton = (Button) findViewById(R.id.chooseClenupFolderButton);
        Button cancelButton = (Button) findViewById(R.id.cancelFolderCleanupButton);
        Button cleanupNowButton = (Button) findViewById(R.id.startFolderCleanupButton);
        folderName = (TextView) findViewById(R.id.cleanupFolderText);

        //Set up date picker
        calendarButton = (ImageButton) findViewById(R.id.calendarImageButton);
        cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);
        date = (TextView) findViewById(R.id.dateEditText);

        //get username from previous activity
        username = getIntent().getExtras().getString("username");



        //Get the folder name to clean and add to the text view

        //add functionality for selecting a directory
        AddFolderButton.setOnClickListener(new View.OnClickListener()
        {
            String m_chosen = "";


            public void onClick(View v)
            {
                //Now we start an activity for a result
                Intent intent = new Intent(CleanupFolderPicker.this, ChooseDirectory.class);
                //Add in whether we want directoryMode or fileMode
                intent.putExtra("selectionMode", "directoryMode");
                intent.putExtra("callingFunction", "SelectFolders");
                startActivityForResult(intent, 2);

            }
        });

        //handle date picker button
        calendarButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                showDialog(0);
            }
        });



        //Cancel Button
        cancelButton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                //Launch Cleanup activity
                Intent intent = new Intent(CleanupFolderPicker.this, SyncedDeviceMenu.class);
                intent.putExtra("username", username);
                v.getContext().startActivity(intent);

                Toast.makeText(v.getContext(), "Cleanup Cancelled", Toast.LENGTH_LONG).show();
            }

        });

        //Cleanup Now Button
        cleanupNowButton.setOnClickListener(new View.OnClickListener()
        {
            String folder;
            String theDate;
            ArrayList<String> directoryList = new ArrayList<String>();
            @Override
            public void onClick(View v)
            {
                folder = (String)folderName.getText();
                theDate = (String)date.getText();

                //check if foldername is there!
                if(folder.equals(""))
                {
                    Toast.makeText(v.getContext(), "No folder choosen, please choose a folder!", Toast.LENGTH_LONG).show();
                }
                else if(theDate.equals(""))
                {
                    Toast.makeText(v.getContext(), "Please select a date for cleanup!", Toast.LENGTH_LONG).show();
                }
                else
                {
                    String dateInMilis = getDateInMilis(theDate);
                    //returns storage in use, need "/" on end
                    String pathBeforeFolders = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
                    final String ssid = checkWifiConnection();
                    //add the foldername to a file which is name username+cleanupFile
                    String cleanFile = username + "CleanupFile";
                    directoryList.add(folder.replace(pathBeforeFolders,""));
                    UsersDirectoryChoosingHandler fileMaker = new UsersDirectoryChoosingHandler(v.getContext(), cleanFile);
                    fileMaker.writeDirectoriesToFile(Context.MODE_PRIVATE, directoryList);


                    //Now file is created, start the sftp communicator
                    SSHBackgroundWorker sshWorker = new SSHBackgroundWorker(v.getContext(),"cleanupFolders");
                    sshWorker.execute("cleanupFolders", username, ssid, dateInMilis);
                }

                //call backup user chosen directories with true for cleanup
            }

        });
    }
    public String checkWifiConnection()
    {
        wifimanager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiInfo = wifimanager.getConnectionInfo();
        String ssid = wifiInfo.getSSID().replace("\"", "");

        return ssid;
    }

    public String getDateInMilis(String dateString)
    {
        //Make a date from the date string
        //remove the spaces
        String temp = dateString.replaceAll("\\s+","");

        //split into individual strings
        String[] dates = temp.split("/");

        int y = Integer.parseInt(dates[2]);
        int m = Integer.parseInt(dates[1]) - 1;//starts at 0
        int d = Integer.parseInt(dates[0]);

        GregorianCalendar gc = new GregorianCalendar(y, m, d);

        Long time = gc.getTimeInMillis();

        String value = String.valueOf(time);

        return value;
    }

    //Set up date dialog
    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id)
    {
        DatePickerDialog dpd = new DatePickerDialog(this, datePickerListener, year, month, day);
        dpd.getDatePicker().setMaxDate(new Date().getTime());

        return dpd;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener()
    {
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            date.setText(selectedDay + " / " + (selectedMonth + 1) + " / "
                    + selectedYear);
            date.setContentDescription(selectedDay + "of the" + (selectedMonth + 1) + " " + selectedYear);
        }
    };

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
            if(!directoryPath.equals(""))
            {
                folderName.setText(directoryPath);
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
                startActivity(new Intent(CleanupFolderPicker.this, HomeScreen.class));
                return true;
            //actionbar up button
            case android.R.id.home:
                //Launch Cleanup activity
                Intent intent = new Intent(CleanupFolderPicker.this, SyncedDeviceMenu.class);
                intent.putExtra("username", username);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
