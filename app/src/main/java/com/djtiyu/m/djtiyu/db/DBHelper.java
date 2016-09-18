package com.djtiyu.m.djtiyu.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.djtiyu.m.djtiyu.util.CommonUtil;

public class DBHelper extends SQLiteOpenHelper {
	/**
	 * table
	 */
	public static final String TABLE_NAME_KEYVALUE = "tb_mapkey";
	private static final String DB_NAME = "djtiyu.com.db";
	/**
	 * version
	 */
	private static final int VERSION = 1;
	/**
	 * SQL for create table
	 */
	private static final String CREATE_TABLE_KEYVALUE = "create table if not exists " + TABLE_NAME_KEYVALUE + "("
			+ BeanPropEnum.KeyValue.tKey + " varchar(40) primary key ," + BeanPropEnum.KeyValue.tValue + " varchar(300) ,"
			+ BeanPropEnum.KeyValue.tType + " varchar(10) )";

	public DBHelper(Context context) {
		super(context, DB_NAME , null, VERSION);
	}

	private static DBHelper dbHelper;

	public synchronized static DBHelper getInstance(Context context) {
		if (dbHelper == null) {
			dbHelper = new DBHelper(context);
		}
		return dbHelper;
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_KEYVALUE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public boolean saveOrUpdateKeyValue(String key, String value) {
		if (CommonUtil.isEmpty(key)) {
			return false;
		}
		SQLiteDatabase db = getReadableDatabase();
		try {
			// check if in
			String sql = " select " + BeanPropEnum.KeyValue.tValue + " from " + DBHelper.TABLE_NAME_KEYVALUE + " where "
					+ BeanPropEnum.KeyValue.tKey + "=?";
			Cursor cursor = db.rawQuery(sql, new String[] { key });

			if (cursor != null && cursor.moveToNext()) {
				ContentValues values = new ContentValues();
				values.put(BeanPropEnum.KeyValue.tValue.toString(), value);
				db.update(DBHelper.TABLE_NAME_KEYVALUE, values, BeanPropEnum.KeyValue.tKey + "=?", new String[] { key });
				cursor.close();
			} else {
				sql = "insert into  " + DBHelper.TABLE_NAME_KEYVALUE + "( " + BeanPropEnum.KeyValue.tKey + ","
						+ BeanPropEnum.KeyValue.tValue + ") values(?,?)";
				db.execSQL(sql, new String[] { key, value });
			}

		} catch (Exception e) {

		} finally {
			db.close();
		}
		return true;
	}
	public String getValue(String keyname) {
		if (CommonUtil.isEmpty(keyname)) {
			return null;
		}
		SQLiteDatabase db = getReadableDatabase();
		String sql = " select " + BeanPropEnum.KeyValue.tValue + " from " + DBHelper.TABLE_NAME_KEYVALUE + " where "
				+ BeanPropEnum.KeyValue.tKey + "=?";
		String value = null;
		try {
			Cursor cursor = db.rawQuery(sql, new String[] { keyname });
			if (cursor != null && cursor.moveToNext()) {
				value = cursor.getString(cursor.getColumnIndex(BeanPropEnum.KeyValue.tValue.toString()));
			}
		} catch (Exception e) {

		} finally {
			db.close();
		}
		return value;
	}
}
