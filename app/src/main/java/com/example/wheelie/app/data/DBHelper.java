package com.example.wheelie.app.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

import com.example.wheelie.app.data.models.Carpark;
import com.example.wheelie.app.data.models.Event;


public class DBHelper extends SQLiteOpenHelper{

    // Database name
    public static final String DATABASE_NAME = "Wheelie.db";

    // Table name
    public static final String TABLE_EVENTS = "events";
    public static final String TABLE_CARPARK = "carpark";

    // Common column names
    public static final String KEY_LAT = "lat";
    public static final String KEY_LNG = "lng";
    public static final String KEY_SLOT = "slot";

    // Events table - column names
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IMG = "img";
    public static final String KEY_URL = "url";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_DATE_START = "date_start";
    public static final String KEY_DATE_END = "date_end";

    // Carpark table - column names
    public static final String KEY_CARPARK_NO = "carpark_no";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create events table
        db.execSQL(
                "create table " +  TABLE_EVENTS + "(" +
                        KEY_ID + " INTEGER PRIMARY KEY," +
                        KEY_NAME + " TEXT," +
                        KEY_DESCRIPTION + " TEXT," +
                        KEY_IMG + " TEXT," +
                        KEY_URL + " TEXT," +
                        KEY_ADDRESS + " TEXT," +
                        KEY_LAT + " DOUBLE," +
                        KEY_LNG + " DOUBLE," +
                        KEY_DATE_START + " TEXT," +
                        KEY_DATE_END + " TEXT," +
                        KEY_SLOT + " INTEGER)"
        );

        // Create carpark table
        db.execSQL(
                "create table " +  TABLE_CARPARK + "(" +
                        KEY_CARPARK_NO + " INTEGER PRIMARY KEY," +
                        KEY_LAT + " DOUBLE," +
                        KEY_LNG + " DOUBLE," +
                        KEY_SLOT + " INTEGER)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARPARK);

        // Create new table
        onCreate(db);
    }


    /*
        Event CRUD
     */
    public void addEvents(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, event.getName());
        values.put(KEY_DESCRIPTION, event.getDescription());
        values.put(KEY_IMG, event.getImg());
        values.put(KEY_URL, event.getUrl());
        values.put(KEY_ADDRESS, event.getAddress());
        values.put(KEY_LAT, event.getLat());
        values.put(KEY_LNG, event.getLng());
        values.put(KEY_DATE_START, event.getDatetimeStart());
        values.put(KEY_DATE_END, event.getDatetimeEnd());
        values.put(KEY_SLOT, event.getSlot());

        // Insert events data into database
        db.insert(TABLE_EVENTS, null, values);
    }

    public Event getEvents(int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_EVENTS + " WHERE "
                + KEY_ID + " = " + eventId;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null) {
            c.moveToFirst();
        }

        Event event = new Event();
        event.setName(c.getString(c.getColumnIndex(KEY_NAME)));
        event.setDescription(c.getString(c.getColumnIndex(KEY_DESCRIPTION)));
        event.setImg(c.getString(c.getColumnIndex(KEY_IMG)));
        event.setUrl(c.getString(c.getColumnIndex(KEY_URL)));
        event.setAddress(c.getString(c.getColumnIndex(KEY_ADDRESS)));
        event.setLat(c.getDouble(c.getColumnIndex(KEY_LAT)));
        event.setLng(c.getDouble(c.getColumnIndex(KEY_LNG)));
        event.setDatetimeStart(c.getString(c.getColumnIndex(KEY_DATE_START)));
        event.setDatetimeEnd(c.getString(c.getColumnIndex(KEY_DATE_END)));
        event.setSlot(c.getInt(c.getColumnIndex(KEY_SLOT)));

        return event;
    }

    public Carpark getCarpark(int carparkId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_CARPARK + " WHERE "
                + KEY_ID + " = " + carparkId;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null) {
            c.moveToFirst();
        }

        Carpark carpark = new Carpark();
        carpark.setCarparkNo(c.getInt(c.getColumnIndex(KEY_CARPARK_NO)));
        carpark.setLat(c.getInt(c.getColumnIndex(KEY_LAT)));
        carpark.setLng(c.getInt(c.getColumnIndex(KEY_LNG)));
        carpark.setSlot(c.getInt(c.getColumnIndex(KEY_SLOT)));

        return carpark;
    }


    /*
        Carpark CRUD
    */
    public void addCarpark(Carpark carpark) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CARPARK_NO, carpark.getCarparkNo());
        values.put(KEY_LAT, carpark.getLat());
        values.put(KEY_LNG, carpark.getLng());
        values.put(KEY_SLOT, carpark.getSlot());

        // Insert carpark data into database
        db.insert(TABLE_CARPARK, null, values);
    }
}
