package com.dancmc.mffrosterparser.database;

import android.content.Context;
import android.provider.BaseColumns;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Daniel on 25/03/2017.
 */

public class GearAssetHelper extends SQLiteAssetHelper {

        private static final String DATABASE_NAME = "mff.db";
        private static final int DATABASE_VERSION = 1;


        //create a global singleton database manager, don't let ppl create another non static instance by making constructor private
        private static GearAssetHelper singleton = null;

        private GearAssetHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            setForcedUpgrade();
        }

        public static GearAssetHelper getInstance(Context context) {
            if (singleton == null) {
                singleton = new GearAssetHelper((context.getApplicationContext()));
            }
            return singleton;
        }


        public class CharacterTable implements BaseColumns {
            public static final String TABLE_NAME = "gears_list";
            public static final String COLUMN_CHAR = "char";
            public static final String COLUMN_CHAR_ALIAS = "char_alias";
            public static final String COLUMN_UNI = "uni";
            public static final String COLUMN_UNI_ALIAS = "uni_alias";
            public static final String COLUMN_GEAR_1 = "gear1";
            public static final String COLUMN_GEAR_2 = "gear2";
            public static final String COLUMN_GEAR_3 = "gear3";
            public static final String COLUMN_GEAR_4 = "gear4";


        }

}
