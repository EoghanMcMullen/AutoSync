package com.eoghanmcmullen.autosync;

/**
 * Created by eoghanmcmullen on 27/04/2016.
 */
// MIME checker
public class MimeUtils {
    //Gets the mimetype of a file
    public static String getType(final String filename)
    {

        int pos = filename.lastIndexOf('.');
        if (pos != -1) {
            String ext = filename.substring(filename.lastIndexOf('.') + 1,
                    filename.length());

            if (ext.equalsIgnoreCase("tif"))
                return "image";
            if (ext.equalsIgnoreCase("gif"))
                return "image";
            if (ext.equalsIgnoreCase("png"))
                return "image";
            if (ext.equalsIgnoreCase("jpg"))
                return "image";
            if (ext.equalsIgnoreCase("mp3"))
                return "audio";
            if (ext.equalsIgnoreCase("aac"))
                return "audio";
            if (ext.equalsIgnoreCase("wav"))
                return "audio";
            if (ext.equalsIgnoreCase("ogg"))
                return "audio";
            if (ext.equalsIgnoreCase("mid"))
                return "audio";
            if (ext.equalsIgnoreCase("midi"))
                return "audio";
            if (ext.equalsIgnoreCase("wma"))
                return "audio";
            if (ext.equalsIgnoreCase("flac"))
                return "audio";

            if (ext.equalsIgnoreCase("mp4"))
                return "video";
            if (ext.equalsIgnoreCase("avi"))
                return "video";
            if (ext.equalsIgnoreCase("wmv"))
                return "video";
            if (ext.equalsIgnoreCase("xml"))
            return "text/xml";
            if (ext.equalsIgnoreCase("txt"))
                return "text/plain";
            if (ext.equalsIgnoreCase("cfg"))
                return "text/plain";
            if (ext.equalsIgnoreCase("csv"))
                return "text/plain";
            if (ext.equalsIgnoreCase("conf"))
                return "text/plain";
            if (ext.equalsIgnoreCase("rc"))
                return "text/plain";
            if (ext.equalsIgnoreCase("htm"))
                return "text/html";
            if (ext.equalsIgnoreCase("html"))
                return "text/html";
            if (ext.equalsIgnoreCase("pdf"))
                return "application/pdf";
            if (ext.equalsIgnoreCase("apk"))
                return "apk";

            // Additions and corrections are welcomed.
        }
        return "other";
    }

}
