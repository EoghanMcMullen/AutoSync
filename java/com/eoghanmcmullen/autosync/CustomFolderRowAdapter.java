package com.eoghanmcmullen.autosync;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by eoghanmcmullen on 01/04/2016.
 */

//custom adapter class, needed to allow control of individual button presses in a listview

public class CustomFolderRowAdapter extends ArrayAdapter<String>
{
    private Context mContext;

    CustomFolderRowAdapter(Context context, ArrayList<String> folderList)
    {
        super(context, R.layout.list_image_text_button, folderList);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater layout = LayoutInflater.from(getContext());
        View customRow = layout.inflate(R.layout.list_image_text_button, parent, false);

        String singleRow = getItem(position);
        final TextView folderName = (TextView) customRow.findViewById(R.id.DirectoryName);
        final ImageView folderIcon = (ImageView) customRow.findViewById(R.id.icon);
        ImageButton deleteButton = (ImageButton) customRow.findViewById(R.id.DeleteButton);
        deleteButton.setTag(position);

        folderName.setContentDescription(singleRow + " folder");
        deleteButton.setContentDescription("Remove " + singleRow + "folder");

        //only show the first 20 of the folder name
        if(singleRow.length() >= 25)
        {
            folderName.setText(singleRow.substring(0,22) + "...");
        }
        else folderName.setText(singleRow);

        deleteButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LinearLayout rl = (LinearLayout) v.getParent();
                TextView directoryName = (TextView) rl.findViewById(R.id.DirectoryName);
                int position = (Integer)v.getTag();
                String text = folderName.getText().toString();
                //Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();

                if(mContext instanceof SelectFolders)
                {
                    ((SelectFolders)mContext).removeFolderButton(position, text);
                }
            }
        });


        return customRow;

    }
}
