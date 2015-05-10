package com.wrh.assistant.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	public static final String DBNAME = "assistant.db";
	public static final String TABLE_NAME_LOCATIONPOINT = "locationPoint";
	public static final String SQL_CREATE_LOCATIONPOINT = "create table "
			+ TABLE_NAME_LOCATIONPOINT
			+ "(_id integer primary key autoincrement,city text,address text)";
	public static final String _ID = "id";
	public static final String CITY = "city";
	public static final String ADDRESS = "address";
	private static final int VERSION = 1;
	private SQLiteDatabase db;

	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	public DBHelper(Context context, String name, int version) {
		this(context, name, null, version);
	}

	public DBHelper(Context context) {
		this(context, DBNAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_LOCATIONPOINT);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public void insert(ContentValues values) {
		db = getWritableDatabase();
		db.insert(TABLE_NAME_LOCATIONPOINT, null, values);
	}

	public int delete() {
		db = getWritableDatabase();
		return db.delete(TABLE_NAME_LOCATIONPOINT, null, null);
	}

	// 查询全部
	public Cursor query() {
		db = getReadableDatabase();
		Cursor c = db.query(TABLE_NAME_LOCATIONPOINT, null, null, null, null,
				null, null);
		return c;
	}
	
	public Cursor query(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		db = getReadableDatabase();
		Cursor c = db.query(TABLE_NAME_LOCATIONPOINT, columns, selection,
				selectionArgs, groupBy, having, orderBy);
		return c;
	}

	public void Close() {
		if (db != null) {
			db.close();
		}
	}

}
