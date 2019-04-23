package com.neuroid.gmap.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.neuroid.gmap.model.Review;

import java.util.ArrayList;
import java.util.List;

public class DBManager {

    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DBManager(Context c) {
        context = c;
    }

    public DBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String origin,String dest,String Rid,String feedback, String score) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.Origin, origin);
        contentValue.put(DatabaseHelper.DEST, dest);
        contentValue.put(DatabaseHelper.Routeid, Rid);
        contentValue.put(DatabaseHelper.FeedBack, feedback);
        contentValue.put(DatabaseHelper.DifficultyScore,score);

        database.insert(DatabaseHelper.TABLE_NAME, null, contentValue);
    }

    public Cursor fetch() {
        String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.Origin, DatabaseHelper.DEST,DatabaseHelper.Routeid,DatabaseHelper.FeedBack,DatabaseHelper.DifficultyScore };
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            if(cursor.moveToFirst()){
                do {

                    Log.d("rows", cursor.getString(cursor.getColumnIndex("feedback")));
                    // cursor.moveToNext();

                } while (cursor.moveToNext());
            }

        }
        return cursor;
    }

    public List<Review> fetchData(String orgin, String dest , String rid){

        List<Review> reviewList  = new ArrayList<>();
        String and =" and ";
        String query = "fromAddress=?" + and + "destination=?" +  and + "routeId=?";

        String[] conditions = {orgin,dest,rid};

        String[] columns = new String[] { DatabaseHelper._ID, DatabaseHelper.Origin, DatabaseHelper.DEST,DatabaseHelper.Routeid,DatabaseHelper.FeedBack,DatabaseHelper.DifficultyScore };
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, columns, query, conditions, null, null, null);
        if (cursor != null) {
            if(cursor.moveToFirst()){
                do {

                    Log.d("rows", cursor.getString(cursor.getColumnIndex("feedback")));
                    // cursor.moveToNext();
                    Review review = new Review(cursor.getString(cursor.getColumnIndex("feedback")));
                    reviewList.add(review);

                } while (cursor.moveToNext());
            }
        }
        return reviewList;
    }

    public int update(long _id, String origin,String dest,String Rid,String feedback, String score) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.Origin, origin);
        contentValue.put(DatabaseHelper.DEST, dest);
        contentValue.put(DatabaseHelper.Routeid, Rid);
        contentValue.put(DatabaseHelper.FeedBack, feedback);
        contentValue.put(DatabaseHelper.DifficultyScore,score);

        int i = database.update(DatabaseHelper.TABLE_NAME, contentValue, DatabaseHelper._ID + " = " + _id, null);
        return i;
    }

    public void delete(long _id) {
        database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper._ID + "=" + _id, null);
    }

}