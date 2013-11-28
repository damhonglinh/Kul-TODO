package model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class StaticVeriableDBAdapter {
	protected static final String TAG = "TODO";
	protected static final String DATABASE_NAME = "todoListStaticVeriable";
	public static final String DATABASE_TABLE = "staticVeriable";
	private static final String CREATE_DB = "create table " + DATABASE_TABLE
			+ " (name text not null primary key, currentId int not null);";
	private static final String KEY_NAME = "name";
	private static final String KEY_CURRENT_ID = "currentId";
	private static final int DB_VERSION = 1;
	private StaticVeriableDatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private Context context;

	private static class StaticVeriableDatabaseHelper extends SQLiteOpenHelper {

		public StaticVeriableDatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_DB);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
		}
	}

	public StaticVeriableDBAdapter(Context context) {
		this.context = context;
	}

	public StaticVeriableDBAdapter open() {
		dbHelper = new StaticVeriableDatabaseHelper(context, DATABASE_NAME,
				null, DB_VERSION);
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	public long addVeriable(String name, long currentId) {
		ContentValues values = new ContentValues();
		values.put(KEY_NAME, name);
		values.put(KEY_CURRENT_ID, currentId);
		return db.insert(DATABASE_TABLE, null, values);
	}

	public int editVeriable(String name, long currentId) {
		ContentValues values = new ContentValues();
		values.put(KEY_CURRENT_ID, currentId);
		return db.update(DATABASE_TABLE, values, KEY_NAME + "='" + name + "'",
				null);
	}

	public Cursor getVeriable(String id) {
		return db.query(DATABASE_TABLE,
				new String[] { KEY_NAME, KEY_CURRENT_ID }, KEY_NAME + "='" + id
						+ "'", null, null, null, null);
	}

	public Cursor getAllRow() {
		return db.query(DATABASE_TABLE,
				new String[] { KEY_NAME, KEY_CURRENT_ID }, null, null, null,
				null, null);
	}
}
