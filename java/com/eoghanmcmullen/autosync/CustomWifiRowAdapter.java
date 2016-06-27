package com.eoghanmcmullen.autosync;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by eoghanmcmullen on 08/04/2016.
 */
public class CustomWifiRowAdapter extends ArrayAdapter<String>
{
    private Context mContext;

    CustomWifiRowAdapter(Context context, ArrayList<String> folderList)
    {
        super(context, R.layout.wifi_single_row, folderList);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater layout = LayoutInflater.from(getContext());
        View customRow = layout.inflate(R.layout.wifi_single_row, parent, false);

        String singleRow = getItem(position);

        final TextView wifiName = (TextView) customRow.findViewById(R.id.wifi_name_textview);
        final ImageView wifiIcon = (ImageView) customRow.findViewById(R.id.wifi_icon);

        wifiName.setText(singleRow);

        if(wifiName.getText().toString().equals("EoghansPi") ||
                wifiName.getText().toString().equals("SyncBox") ||
                wifiName.getText().toString().equals("SyncBoxClosed"))
        {
            wifiIcon.setImageResource(R.mipmap.my_launcher);
        }

        wifiName.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LinearLayout rl = (LinearLayout) v.getParent();
                String ssid = wifiName.getText().toString();

                if (mContext instanceof SelectRaspberryPi)
                {
                    ((SelectRaspberryPi) mContext).wasSyncboxClicked(ssid);
                }
            }
        });

        return customRow;
    }
}
