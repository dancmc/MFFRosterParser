package com.dancmc.mffrosterparser.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Daniel on 24/03/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "MFF";
    static final int DATABASE_VERSION = 1;

    DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



}
