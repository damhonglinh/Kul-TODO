package model.group;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

class GroupDBAdapter {
	static final String TAG = "TODO";
	static final String DATABASE_NAME = "todolistMGroup";
	private static final String KEY_ID = "_id";
	private static final String KEY_NAME = "name";
	private static final String KEY_TO_SYNC = "toSync";
	static final String DATABASE_TABLE = "mGroup";
	private static final String CREATE_DB = "create table " + DATABASE_TABLE
			+ " (_id text primary key, name text not null, toSync int);";
	private static final int DB_VERSION = 1;
	private GroupDatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private Context context;

	private static class GroupDatabaseHelper extends SQLiteOpenHelper {

		GroupDatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_DB);
			// Log.i(TAG, "Database group created");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
		}
	}

	GroupDBAdapter(Context context) {
		this.context = context;
		// Log.i(TAG, "group DBAdapter constructed");
	}

	GroupDBAdapter open() {
		if (dbHelper != null) {
			dbHelper.close();
		}
		dbHelper = new GroupDatabaseHelper(context, DATABASE_NAME, null,
				DB_VERSION);
		db = dbHelper.getWritableDatabase();
		return this;
	}

	void close() {
		dbHelper.close();
	}

	long addGroup(Group group) {
		ContentValues value = new ContentValues();
		value.put(KEY_ID, group.getId());
		value.put(KEY_NAME, group.getName());
		value.put(KEY_TO_SYNC, group.getToSync());
		return db.insert(DATABASE_TABLE, null, value);
	}

	long replaceGroup(Group group) {
		ContentValues value = new ContentValues();
		value.put(KEY_ID, group.getId());
		value.put(KEY_NAME, group.getName());
		value.put(KEY_TO_SYNC, group.getToSync());
		return db.replace(DATABASE_TABLE, null, value);
	}

	int editGroup(Group group) {
		ContentValues value = new ContentValues();
		value.put(KEY_NAME, group.getName());
		value.put(KEY_TO_SYNC, group.getToSync());
		return db.update(DATABASE_TABLE, value, KEY_ID + "='" + group.getId()
				+ "'", null);
	}

	int deleteGroup(String groupId) {
		return db.delete(DATABASE_TABLE, KEY_ID + "='" + groupId + "'", null);
	}

	Cursor getGroup(String id) {
		return db
				.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_NAME,
						KEY_TO_SYNC }, KEY_ID + "='" + id + "'", null, null,
						null, null);
	}

	Cursor getGroupByName(String name) {
		return db.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_NAME,
				KEY_TO_SYNC }, KEY_NAME + "='" + name + "'", null, null, null,
				null);
	}

	Cursor getAllGroup() {
		return db.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_NAME,
				KEY_TO_SYNC }, KEY_TO_SYNC + " != '" + Group.DELETE + "'",
				null, null, null, null);
	}

	Cursor getAllGroupWhenSync() {
		return db.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_NAME,
				KEY_TO_SYNC }, null, null, null, null, null);
	}
}
