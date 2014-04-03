package com.teusoft.grillngo.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static String dbFileName = "dishes.db";

	private static final int VERSION = 1;

	// DIARY
	public static final String TABLE_DISHES = "dishes";
	public static final String ID = "_ID";
	public static final String TITLE = "TITLE";
	public static final String OPEN_TIMESTAMP = "OPEN_TIMESTAMP";
	public static final String LOCATION = "LOCATION";
	public static final String IMAGE_NAME = "IMAGE_NAME";
	public static final String LONGITUDE = "LONGITUDE";
	public static final String LATITUDE = "LATITUDE";

	private static final String CREATE_TABLE_DIARY = "CREATE TABLE "
			+ TABLE_DISHES + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ TITLE + " TEXT," + OPEN_TIMESTAMP + " LONG," + LOCATION
			+ " TEXT," + IMAGE_NAME + " TEXT," + LONGITUDE + " REAL,"
			+ LATITUDE + " REAL)";

	SQLiteDatabase db;

	public DatabaseHelper(Context context) {
		super(context, dbFileName, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		this.db = db;
		db.execSQL(CREATE_TABLE_DIARY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS" + TABLE_DISHES);
	}

	@Override
	public synchronized void close() {
		if (db != null) {
			db.close();
			super.close();
		}
	}
}
