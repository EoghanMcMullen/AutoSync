package com.eoghanmcmullen.autosync;

/**
 * Created by eoghanmcmullen on 12/04/2016.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class SQLiteNameStorer extends SQLiteOpenHelper
{
    // Books table name
    private static final String TABLE_USERS = "users";

    // Books Table Columns names
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_IP = "ip";
    private static final String KEY_IP_PASSWORD = "ip_password";


    private static final String[] COLUMNS = {KEY_USERNAME,KEY_EMAIL,KEY_PASSWORD,KEY_IP,KEY_IP_PASSWORD};

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "UserDB";

    public SQLiteNameStorer(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // SQL statement to create user table
        String CREATE_USER_TABLE = "CREATE TABLE users ( " +
                "username TEXT PRIMARY KEY, " +
                "email TEXT, " +
                "password TEXT, " +
                "ip TEXT, " +
                "ip_password TEXT )";

        // create USER table
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS users");

        // create fresh books table
        this.onCreate(db);
    }

    public long addUser(SyncBoxUser aUser)
    {
        //for logging
        Log.d("addUser", aUser.toString());

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_USERNAME, aUser.getUsername());
        values.put(KEY_EMAIL, aUser.getEmail());
        values.put(KEY_PASSWORD, aUser.getPassword());
        values.put(KEY_IP, aUser.getIp());
        values.put(KEY_IP_PASSWORD, aUser.getIp_password());

        // 3. insert
        long rowInserted = db.insert(TABLE_USERS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values



        // 4. close
        db.close();

        return rowInserted;
    }

    public SyncBoxUser getUser(String username)
    {

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_USERS, // a. table
                        COLUMNS, // b. column names
                        " username = ?", // c. selections
                        new String[] { username }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build book object
        SyncBoxUser sbu = new SyncBoxUser();
        sbu.setUsername(cursor.getString(0));
        sbu.setEmail(cursor.getString(1));
        sbu.setPassword(cursor.getString(2));
        sbu.setIp(cursor.getString(3));
        sbu.setIp_password(cursor.getString(4));

        //log
        Log.d("getUSER(" + username + ")", sbu.toString());

        // 5. return book
        return sbu;
    }

    public List<SyncBoxUser> getAllUsers()
    {
        List<SyncBoxUser> users = new LinkedList<SyncBoxUser>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_USERS;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build user and add it to list
        SyncBoxUser user = null;
        if (cursor.moveToFirst())
        {
            do
            {
                user = new SyncBoxUser();
                user.setUsername(cursor.getString(0));
                user.setEmail(cursor.getString(1));
                user.setPassword(cursor.getString(2));
                user.setIp(cursor.getString(3));
                user.setIp_password(cursor.getString(4));

                // Add user to users
                users.add(user);
            } while (cursor.moveToNext());
        }

        Log.d("getAllUsers()", users.toString());

        cursor.close();
        // return books
        return users;
    }

    //check if there are any table entries
    public boolean anyUsersAvailable()
    {
        String query = "Select * from " + TABLE_USERS;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        if(cursor.getCount() <= 0)
        {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    //---deletes a particular title---
    public boolean deleteUser(String user)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_USERS, KEY_USERNAME + "='" + user+"'", null) > 0;
    }

}
