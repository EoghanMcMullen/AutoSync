package com.eoghanmcmullen.autosync;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by eoghanmcmullen on 27/04/2016.
 */
public class CustomDirectoryChooserAdapter extends ArrayAdapter<String>
{
    private Context mContext;
    private MimeUtils mimeUtils = new MimeUtils();

    CustomDirectoryChooserAdapter(Context context, ArrayList<String> folderList)
    {
        super(context, R.layout.choose_directory_single_item, folderList);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater layout = LayoutInflater.from(getContext());
        View gridItem = layout.inflate(R.layout.choose_directory_single_item, parent, false);

        final String filePath = getItem(position);
        final TextView filePathTextView = (TextView) gridItem.findViewById(R.id.filePathTextView);
        final ImageButton fileImage = (ImageButton) gridItem.findViewById(R.id.image);

        //Check if the item is a directory or a file
        File f = new File(filePath);

        //!!!Need to take away path here!!!

        String pathRemoved = removePath(filePath);
        filePathTextView.setText(pathRemoved);

        //Add content description for screen reader
        fileImage.setContentDescription(pathRemoved);

        //if (f.isDirectory() || FilenameUtils.getExtension(filePath).equals(""))
        if (f.isDirectory())
        {
            fileImage.setImageResource(R.mipmap.ic_directory_img);
            fileImage.setContentDescription(pathRemoved + " folder");
            filePathTextView.setContentDescription(pathRemoved + " folder button");
        }
        //images
        else if (isImage(f))
        {
            Picasso.with(mContext).load(f).resize(200, 200).into(fileImage);
            // Bitmap bmp = BitmapFactory.decodeFile(filePath);
            // fileImage.setImageBitmap(bmp);
            fileImage.setContentDescription("file name: " + pathRemoved);
            //filePathTextView.setContentDescription("file name " + pathRemoved + "button");
        }
        else if (FilenameUtils.getExtension(filePath).equals("pdf"))
        {
            fileImage.setImageResource(R.mipmap.ic_pdf);

            fileImage.setContentDescription("file name: " + pathRemoved);
            //filePathTextView.setContentDescription("file name " + pathRemoved+ "button");
        }
        //Audio files
        else if (mimeUtils.getType(filePath).equals("audio"))
        {
            fileImage.setImageResource(R.mipmap.ic_audio);

            fileImage.setContentDescription("file name: " + pathRemoved);
            //filePathTextView.setContentDescription("file name " + pathRemoved+ "button");
        }
        //Video files
        else if (mimeUtils.getType(filePath).equals("video"))
        {
            fileImage.setImageResource(R.mipmap.ic_video);

            fileImage.setContentDescription("file name: " + pathRemoved);
            //filePathTextView.setContentDescription("file name " + pathRemoved+ "button");
        }
        else
        {
            fileImage.setImageResource(R.mipmap.ic_file);

            fileImage.setContentDescription("file name: " + pathRemoved);
            //filePathTextView.setContentDescription("file name " + pathRemoved+ "button");
        }
        //fix for syncbox browsing
        if(mContext instanceof SyncBoxDirectoryBrowser)
        {
            if(FilenameUtils.getExtension(filePath).equals(""))
            {
                fileImage.setImageResource(R.mipmap.ic_directory_img);
                fileImage.setContentDescription(pathRemoved + " folder");
                filePathTextView.setContentDescription(pathRemoved + " folder button");
            }

        }





        fileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //pass the calling class the filepath that was clicked
                if (mContext instanceof ChooseDirectory)
                {
                    ((ChooseDirectory) mContext).itemClickHandler(filePath);
                }
                else if(mContext instanceof SyncBoxDirectoryBrowser)
                {
                    ((SyncBoxDirectoryBrowser) mContext).itemClickHandler(filePath);
                }
            }
        });


        return gridItem;
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

    //Method to check if file is an image
    public static boolean isImage(File file)
    {
        if (file == null || !file.exists())
        {
            return false;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        //Set this so whole image isn't decoded, just metadata
        options.inJustDecodeBounds = true;
        //sets the images with and height, return false if error
        BitmapFactory.decodeFile(file.getPath(), options);
        return options.outWidth != -1 && options.outHeight != -1;
    }
}
