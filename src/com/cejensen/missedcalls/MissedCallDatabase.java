package com.cejensen.missedcalls;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class MissedCallDatabase {
	public static final String	KEY_ACTIVE								= "active";
	public static final String	KEY_LOGID									= "id";
	public static final String	KEY_LOG_TIME							= "time";
	public static final String	KEY_LOG_TEXT							= "logtext";

	private static final String	DATABASE_NAME							= "missedCallsDatabase";
	private static final String	DATABASE_ACTIVATED_TABLE	= "activated";
	private static final String	DATABASE_LOG_TABLE				= "log";

	private static final String	TABLE_ACTIVATED_CREATE		= "create table " + DATABASE_ACTIVATED_TABLE + "(" + KEY_ACTIVE
																														+ " integer not null default 0);";

	private static final String	TABLE_LOG_CREATE					= "create table " + DATABASE_LOG_TABLE + "(" + KEY_LOGID
																														+ " integer primary key autoincrement, " + KEY_LOG_TIME + " integer not null, "
																														+ KEY_LOG_TEXT + " text not null );";

	private static final int		DATABASE_VERSION					= 4;

	private final Context				m_context;
	private DatabaseHelper			m_DBHelper;
	private SQLiteDatabase			m_db;

	public MissedCallDatabase(Context ctx) {
		m_context = ctx;
		m_DBHelper = new DatabaseHelper(m_context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TABLE_ACTIVATED_CREATE);
			db.execSQL(TABLE_LOG_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			switch (oldVersion) {
			case 3:
				switch (newVersion) {
				case 4:
					Log.w(DATABASE_NAME, "Upgrading database from version " + oldVersion + " to " + newVersion);
					db.execSQL("DROP TABLE " + DATABASE_LOG_TABLE);
					db.execSQL(TABLE_LOG_CREATE);
					break;
				default:
					Log.w(DATABASE_NAME, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
					db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
					onCreate(db);
					break;
				}
				break;
			}
		}
	}

	// ---opens the database---
	public MissedCallDatabase open() throws SQLException {
		m_db = m_DBHelper.getWritableDatabase();

		return this;
	}

	// ---closes the database---
	public void close() {
		m_DBHelper.close();
	}

	public boolean getActive() {
		Cursor c = m_db.query(true, DATABASE_ACTIVATED_TABLE, new String[] { KEY_ACTIVE }, null, null, null, null, null, null);
		c.moveToFirst();
		boolean active = false;
		if (c.isAfterLast() == false) {
			active = c.getInt(c.getColumnIndex(KEY_ACTIVE)) == 0 ? false : true;
		}
		c.close();

		return active;
	}

	public void setActive(boolean active) {
		if (getActiveCount() > 0) {
			m_db.delete(DATABASE_ACTIVATED_TABLE, null, null);
		}
		insertActive(active);
	}

	private int getActiveCount() {
		String sql = "SELECT COUNT(*) FROM " + DATABASE_ACTIVATED_TABLE;
		SQLiteStatement statement = m_db.compileStatement(sql);
		long count = statement.simpleQueryForLong();
		return (int) count;
	}

	private int insertActive(boolean active) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_ACTIVE, active ? 1 : 0);
		int rowId = (int) m_db.insert(DATABASE_ACTIVATED_TABLE, null, initialValues);
		return rowId;
	}

	public int insertLogEntry(LogEntry le) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_LOG_TIME, le.getDate().getTime());
		initialValues.put(KEY_LOG_TEXT, le.getLogText());
		int rowId = (int) m_db.insert(DATABASE_LOG_TABLE, null, initialValues);
		return rowId;
	}

	// ---retrieves all the titles---
	public LogEntry[] getAllLogEntries() {
		List<LogEntry> logs = new ArrayList<LogEntry>();
		Cursor c = m_db.query(true, DATABASE_LOG_TABLE, new String[] { KEY_LOGID, KEY_LOG_TIME, KEY_LOG_TEXT }, null, null, null, null, null, null);
		c.moveToFirst();
		while (c.isAfterLast() == false) {
			Date date = new Date(c.getLong(c.getColumnIndex(KEY_LOG_TIME)));
			LogEntry entry = new LogEntry(c.getLong(c.getColumnIndex(KEY_LOGID)), date, c.getString(c.getColumnIndex(KEY_LOG_TEXT)));
			logs.add(entry);
			c.moveToNext();
		}
		c.close();

		return logs.toArray(new LogEntry[logs.size()]);
	}
}
