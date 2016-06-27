package com.eoghanmcmullen.autosync;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Switch notificationSwitch = (Switch) findViewById(R.id.notificationSwitch);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        Boolean switchPosition = prefs.getBoolean("switchBoolean", true);

        notificationSwitch.setChecked(switchPosition);


        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
            {
                SharedPreferences.Editor editor = prefs.edit();


                if(isChecked)
                {
                    //Turn Broadcast reciver on
                    PackageManager pm  = SettingsActivity.this.getPackageManager();
                    ComponentName componentName = new ComponentName(SettingsActivity.this, NetworkChangeReceiver.class);
                    pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);

                    editor.putBoolean("switchBoolean", true).commit();


                    Toast toast = Toast.makeText(SettingsActivity.this, "Notifications enabled!", Toast.LENGTH_LONG);
                    toast.show();
                }
                else
                {
                    //Turn Broadcast reciver off
                    PackageManager pm  = SettingsActivity.this.getPackageManager();
                    ComponentName componentName = new ComponentName(SettingsActivity.this, NetworkChangeReceiver.class);
                    pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);

                    editor.putBoolean("switchBoolean", false).commit();


                    Toast toast = Toast.makeText(SettingsActivity.this, "Notifications disabled!", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

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
            case R.id.action_home:
                startActivity(new Intent(SettingsActivity.this, HomeScreen.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
