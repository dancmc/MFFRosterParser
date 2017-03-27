package com.dancmc.mffrosterparser.database;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Daniel on 25/03/2017.
 */

public class GearDataSource {

    private GearAssetHelper mDbHelper;

    public GearDataSource(Context context) {
        mDbHelper = GearAssetHelper.getInstance(context);
    }

    public String getCharAlias(String s) {
        Cursor cursor = mDbHelper.getReadableDatabase().query(
                GearAssetHelper.CharacterTable.TABLE_NAME,
                new String[]{GearAssetHelper.CharacterTable.COLUMN_CHAR_ALIAS},
                GearAssetHelper.CharacterTable.COLUMN_CHAR + "=?",
                new String[]{s},
                null,
                null,
                null);

        cursor.moveToFirst();
        String alias = cursor.getString(0);
        cursor.close();

        return alias;
    }

    public String getUniAlias(String s) {
        Cursor cursor = mDbHelper.getReadableDatabase().query(
                GearAssetHelper.CharacterTable.TABLE_NAME,
                new String[]{GearAssetHelper.CharacterTable.COLUMN_UNI_ALIAS},
                GearAssetHelper.CharacterTable.COLUMN_UNI + "=?",
                new String[]{s},
                null,
                null,
                null);

        cursor.moveToFirst();
        String alias = cursor.getString(0);
        cursor.close();

        return alias;
    }

    public AbstractMap.SimpleEntry<String, Integer> searchGears(String s) {

        int gearNumber = 0;
        Cursor cursor = mDbHelper.getReadableDatabase().query(
                GearAssetHelper.CharacterTable.TABLE_NAME,
                new String[]{GearAssetHelper.CharacterTable.COLUMN_CHAR_ALIAS},
                GearAssetHelper.CharacterTable.COLUMN_GEAR_1 + " like ?",
                new String[]{s},
                null,
                null,
                null);
        if(cursor.getCount()==0) {
            cursor = mDbHelper.getReadableDatabase().query(
                    GearAssetHelper.CharacterTable.TABLE_NAME,
                    new String[]{GearAssetHelper.CharacterTable.COLUMN_CHAR_ALIAS},
                    GearAssetHelper.CharacterTable.COLUMN_GEAR_2 + " like ?",
                    new String[]{s},
                    null,
                    null,
                    null);
            gearNumber = 1;
            if(cursor.getCount()==0) {
                cursor = mDbHelper.getReadableDatabase().query(
                        GearAssetHelper.CharacterTable.TABLE_NAME,
                        new String[]{GearAssetHelper.CharacterTable.COLUMN_CHAR_ALIAS},
                        GearAssetHelper.CharacterTable.COLUMN_GEAR_3 + " like ?",
                        new String[]{s},
                        null,
                        null,
                        null);
                gearNumber = 2;
                if (cursor.getCount() == 0) {
                    cursor = mDbHelper.getReadableDatabase().query(
                            GearAssetHelper.CharacterTable.TABLE_NAME,
                            new String[]{GearAssetHelper.CharacterTable.COLUMN_CHAR_ALIAS},
                            GearAssetHelper.CharacterTable.COLUMN_GEAR_4 + " like ?",
                            new String[]{s},
                            null,
                            null,
                            null);
                    gearNumber = 3;
                    if (cursor.getCount() == 0) {
                        return null;
                    }
                }
            }
        }

        cursor.moveToFirst();
        AbstractMap.SimpleEntry<String, Integer> map = new AbstractMap.SimpleEntry<String, Integer>(cursor.getString(0),gearNumber);
        cursor.close();
        return map;
    }

    public String getRandomUni(String characterAlias){
        Cursor cursor = mDbHelper.getReadableDatabase().query(
                GearAssetHelper.CharacterTable.TABLE_NAME,
                new String[]{GearAssetHelper.CharacterTable.COLUMN_UNI_ALIAS},
                GearAssetHelper.CharacterTable.COLUMN_CHAR_ALIAS + "=?",
                new String[]{characterAlias},
                null,
                null,
                null);


        cursor.moveToFirst();
        String uni = cursor.getString(0);
        cursor.close();
        Log.d("DataSource", "getRandomUni: "+characterAlias+" "+ uni);

        return uni;
    }


}
