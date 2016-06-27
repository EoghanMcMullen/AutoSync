package com.eoghanmcmullen.autosync;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by eoghanmcmullen on 13/04/2016.
 */
public class CustomSyncedDevicesRowAdapter extends ArrayAdapter<String>
{
    private Context mContext;

    CustomSyncedDevicesRowAdapter(Context context, ArrayList<String> folderList)
    {
        super(context, R.layout.synced_devices_row, folderList);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater layout = LayoutInflater.from(getContext());
        View customRow = layout.inflate(R.layout.synced_devices_row, parent, false);

        final String singleRow = getItem(position);

        final TextView syncBoxName = (TextView) customRow.findViewById(R.id.syncBoxNameText);
        final ImageView syncBoxIcon = (ImageView) customRow.findViewById(R.id.syncBoxImageview);

        syncBoxName.setText(singleRow + "'s SyncBox");

        String colourCode = getColourcode(position);
        syncBoxName.setBackgroundColor(Color.parseColor(colourCode));
        syncBoxIcon.setBackgroundColor(Color.parseColor(colourCode));
        customRow.setBackgroundColor(Color.parseColor(colourCode));

        syncBoxName.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LinearLayout rl = (LinearLayout) v.getParent();

                //pass the username to the calling class
                if (mContext instanceof SyncedDeviceChooser)
                {
                    Intent intent = new Intent(v.getContext(), SyncedDeviceMenu.class);
                    intent.putExtra("username", singleRow);
                    v.getContext().startActivity(intent);
                }
                else if (mContext instanceof CloudUserChooser)
                {
                    ((CloudUserChooser) mContext).itemClickHandler(singleRow);
                }
            }
        });

        syncBoxName.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {

                if (mContext instanceof SyncedDeviceChooser)
                {
                    //pass back username
                    ((SyncedDeviceChooser) mContext).longClickHandler(singleRow);
                }

                return false;
            }
        });

        return customRow;
    }

    private String getColourcode(int position)
    {
        //set up the colour changer for every fourth item
        String colourCode = "";

        //check is it a position 0,1,2 or 3
        if(colourCode.equals(""))
        {
            for (int i = 0; i <= position; i += 3)
            {
                if (i == position)
                {
                    //its in zero position
                    colourCode = "#98C9A3";
                }
            }
        }
        //check is it a position 1
        if(colourCode.equals(""))
        {
            for (int i = 1; i <= position; i += 3)
            {
                if (i == position)
                {
                    //its in position 1
                    colourCode = "#DDE7C7";
                }
            }
        }
        if(colourCode.equals(""))
        {
            //check is it a position 2
            for (int i = 2; i <= position; i += 3)
            {
                if (i == position)
                {
                    colourCode = "#BFB8BD";
                }
            }
        }
        return colourCode;
    }
}
