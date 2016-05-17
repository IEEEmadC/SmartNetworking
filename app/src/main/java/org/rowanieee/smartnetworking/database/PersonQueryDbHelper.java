package org.rowanieee.smartnetworking.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.nfc.tech.TagTechnology;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.rowanieee.smartnetworking.model.SavedContact;

import java.util.ArrayList;

/**
 * Created by Nick Felker on 5/16/2016.
 */
public class PersonQueryDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "network.db";
    public static final String TAG = "munch::dbHelper";

    private static final String TEXT_TYPE = " TEXT";
    private static final String LONG_TYPE = " BIGINT";
    private static final String INT_TYPE = " INTEGER";
    private static final String FLOAT_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES = /* IF NOT EXISTS */
            "CREATE TABLE IF NOT EXISTS " + PersonQueryContract.PersonQueryEntry.TABLE_NAME + " (" +
                    PersonQueryContract.PersonQueryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    PersonQueryContract.PersonQueryEntry.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    PersonQueryContract.PersonQueryEntry.COLUMN_EMAIL + TEXT_TYPE + COMMA_SEP +
                    PersonQueryContract.PersonQueryEntry.COLUMN_PHOTO + LONG_TYPE + COMMA_SEP +
                    PersonQueryContract.PersonQueryEntry.COLUMN_QRURL + TEXT_TYPE + COMMA_SEP +
                    PersonQueryContract.PersonQueryEntry.COLUMN_CONNECTIONS + FLOAT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PersonQueryContract.PersonQueryEntry.TABLE_NAME;

    public SQLiteDatabase write;
    public SQLiteDatabase read;


    public PersonQueryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        //Make sure table is deleted first
//        db.execSQL(SQL_DELETE_ENTRIES); //FIXME This may not be a good idea
        Log.d(TAG, "Wave hi");
        db.execSQL(SQL_CREATE_ENTRIES);
        /*insert(new FoodTableEntry(0, new Date().getTime(), "Potato Chips"));
        insert(new FoodTableEntry(1, new Date().getTime(), "Chicken"));
        insert(new FoodTableEntry(2, new Date().getTime(), "Apple"));*/
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    public void insert(SQLiteDatabase db, SavedContact fte) {
        ContentValues values = new ContentValues();
        values.put(PersonQueryContract.PersonQueryEntry.COLUMN_NAME, fte.getName());
        values.put(PersonQueryContract.PersonQueryEntry.COLUMN_EMAIL, fte.getEmail());
        values.put(PersonQueryContract.PersonQueryEntry.COLUMN_PHOTO, fte.getPhotoBase64());
        values.put(PersonQueryContract.PersonQueryEntry.COLUMN_QRURL, fte.getAboutme());
        values.put(PersonQueryContract.PersonQueryEntry.COLUMN_CONNECTIONS, fte.getConnections().toString());

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(
                PersonQueryContract.PersonQueryEntry.TABLE_NAME,
                null,
                values);
        Log.d(TAG, newRowId+" seems successful");
    }
    public ArrayList<SavedContact> readAll(SQLiteDatabase db) {
        read = getReadableDatabase();
        // Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                PersonQueryContract.PersonQueryEntry._ID,
                PersonQueryContract.PersonQueryEntry.COLUMN_NAME,
                PersonQueryContract.PersonQueryEntry.COLUMN_EMAIL,
                PersonQueryContract.PersonQueryEntry.COLUMN_QRURL,
                PersonQueryContract.PersonQueryEntry.COLUMN_PHOTO,
                PersonQueryContract.PersonQueryEntry.COLUMN_CONNECTIONS
        };


        Cursor c = db.query(
                PersonQueryContract.PersonQueryEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                PersonQueryContract.PersonQueryEntry._ID+">?", // The columns for the WHERE clause
                new String[]{"0"},                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        Log.d(TAG, c.getCount()+" entries in readall");
        ArrayList<SavedContact> entries = new ArrayList<>();
        if(c.getCount() == 0) {
            return entries;
        }
        c.moveToFirst();
        while(c.getPosition() < c.getCount()) {
            try {
                entries.add(
                        new SavedContact(
                                c.getString(c.getColumnIndexOrThrow(PersonQueryContract.PersonQueryEntry.COLUMN_NAME)),
                                c.getString(c.getColumnIndexOrThrow(PersonQueryContract.PersonQueryEntry.COLUMN_EMAIL)),
                                c.getString(c.getColumnIndexOrThrow(PersonQueryContract.PersonQueryEntry.COLUMN_PHOTO)),
                                c.getString(c.getColumnIndexOrThrow(PersonQueryContract.PersonQueryEntry.COLUMN_QRURL)),
                                new JSONObject(c.getString(c.getColumnIndexOrThrow(PersonQueryContract.PersonQueryEntry.COLUMN_CONNECTIONS)))
                        )
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(c.getPosition() < c.getCount()) {
                c.moveToNext();
                Log.d(TAG, "Moved to " + c.getPosition() + " / " + c.getCount());
            }
        }
        return entries;
    }

}
