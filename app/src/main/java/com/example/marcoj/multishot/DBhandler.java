package com.example.marcoj.multishot;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by marcoj on 09/06/16.
 */
public class DBhandler extends SQLiteOpenHelper {
    private static final String TAG = "DB";
    private static final int DB_VERSION=1;
    private static final String DB_NAME="photos.db";
    private static final String TABLE_NAME="photos";
    private static final String COLUMN_NAME="filename";
    private static final String COLUMN_LATITUDE="latitude";
    private static final String COLUMN_LONGITUDE="longitude";

    public DBhandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query="CREATE TABLE "+TABLE_NAME+"("+COLUMN_NAME+" TEXT PRIMARY KEY, "+
                COLUMN_LATITUDE+" REAL, "+
                COLUMN_LONGITUDE+" REAL"+
                ");";
        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);
    }

    public void addPhoto(String filename, double latitude, double longitude){
        ContentValues values=new ContentValues();
        values.put(COLUMN_NAME, filename);
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);

        SQLiteDatabase db=getWritableDatabase();
        db.insert(TABLE_NAME,null,values);
        db.close();
    }

    public ArrayList<Photo> getElements(){
        ArrayList<Photo> photo=new ArrayList<Photo>();
        double latitude,longitude;
        SQLiteDatabase db=getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE 1",null);

        if(c.moveToFirst()){
            do{
                String filename=c.getString(0);
                latitude=c.getDouble(1);
                longitude=c.getDouble(2);
                Log.i(TAG, "filename: " + filename + " , latitude: " + latitude + " , longitude: " + longitude);

                photo.add(new Photo(filename, latitude, longitude));
            }while(c.moveToNext());
        }
        c.close();
        db.close();

        return photo;
    }
}
