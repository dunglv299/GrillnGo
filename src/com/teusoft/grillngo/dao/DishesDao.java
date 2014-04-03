package com.teusoft.grillngo.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.teusoft.grillngo.entity.MyDishes;

public class DishesDao {
	private SQLiteDatabase mDB;
	DatabaseHelper mHelper = null;
	private MyDishes dishes;

	public DishesDao(Context context) {
		mHelper = new DatabaseHelper(context);
		this.mDB = mHelper.getWritableDatabase();
	}

	public long insert(MyDishes dishes) {
		long rowID = -1;
		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.TITLE, dishes.getTitle());
		contentValues.put(DatabaseHelper.OPEN_TIMESTAMP, dishes.getTimeStamp());
		contentValues.put(DatabaseHelper.LOCATION, dishes.getLocation());
		contentValues.put(DatabaseHelper.IMAGE_NAME, dishes.getImageName());
		contentValues.put(DatabaseHelper.LONGITUDE, dishes.getLongitude());
		contentValues.put(DatabaseHelper.LATITUDE, dishes.getLatitude());
		rowID = mDB.insert(DatabaseHelper.TABLE_DISHES, null, contentValues);
		return rowID;
	}

	public void update(MyDishes dishes) {
		String whereClause = DatabaseHelper.ID + "=" + dishes.getId();
		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.TITLE, dishes.getTitle());
		contentValues.put(DatabaseHelper.OPEN_TIMESTAMP, dishes.getTimeStamp());
		contentValues.put(DatabaseHelper.LOCATION, dishes.getLocation());
		contentValues.put(DatabaseHelper.IMAGE_NAME, dishes.getImageName());
		contentValues.put(DatabaseHelper.LONGITUDE, dishes.getLongitude());
		contentValues.put(DatabaseHelper.LATITUDE, dishes.getLatitude());
		mDB.update(DatabaseHelper.TABLE_DISHES, contentValues, whereClause,
				null);
		mDB.close();
	}

	public void delete(MyDishes dishes) {
		String whereClause = DatabaseHelper.ID + "=" + dishes.getId();
		mDB.delete(DatabaseHelper.TABLE_DISHES, whereClause, null);
	}

	public List<MyDishes> getAllMyDishes() {
		List<MyDishes> listDishes = new ArrayList<MyDishes>();

		String selectQuery = "SELECT * FROM " + DatabaseHelper.TABLE_DISHES
				+ " ORDER BY " + DatabaseHelper.OPEN_TIMESTAMP + " DESC";
		Cursor cursor = mDB.rawQuery(selectQuery, null);
		try {
			if (cursor.moveToFirst()) {
				do {
					dishes = new MyDishes();
					dishes.setId(cursor.getInt(0));
					dishes.setTitle(cursor.getString(1));
					dishes.setTimeStamp(cursor.getLong(2));
					dishes.setLocation(cursor.getString(3));
					dishes.setImageName(cursor.getString(4));
					dishes.setLongitude(cursor.getDouble(5));
					dishes.setLatitude(cursor.getDouble(6));
					listDishes.add(dishes);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
		} finally {
			cursor.close();
		}
		return listDishes;
	}

	public void close() {
		if (mDB != null) {
			mDB.close();
		}
	}
}
