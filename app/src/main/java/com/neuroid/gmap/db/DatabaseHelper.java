package com.neuroid.gmap.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Table Name
    public static final String TABLE_NAME = "ROUTES";

    // Table columns
    public static final String _ID = "_id";
    public static final String Origin = "fromAddress";
    public static final String DEST = "destination";
    public static final String Routeid = "routeId";
    public static final String FeedBack = "feedback";
    public static final String DifficultyScore ="dscore";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    // Database Information
    static final String DB_NAME = "travel.DB";

    // database version
    static final int DB_VERSION = 1;

    // Creating table query
    private static final String CREATE_TABLE = "create table " + TABLE_NAME + "(" + _ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Origin + "  TEXT NOT NULL, " + DEST + TEXT_TYPE + COMMA_SEP +
            Routeid +  TEXT_TYPE + COMMA_SEP + FeedBack +  TEXT_TYPE + COMMA_SEP + DifficultyScore + " TEXT NOT NULL "+ ");";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
