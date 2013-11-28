package model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ContactDBAdapter {
	static final String TAG = "TODO";
	static final String DATABASE_NAME = "todolistContact";
	private static final String KEY_CONTACT_ID = "contactId";
	private static final String KEY_TASK_ID = "taskId";
	private static final String KEY_NAME = "name";
	private static final String KEY_EMAIL = "email";
	private static final String KEY_PHONE = "phone";
	static final String DATABASE_TABLE = "contact";
	public final static int COLUMN_CONTACT_ID = 0;
	public final static int COLUMN_TASK_ID = 1;
	public final static int COLUMN_NAME = 2;
	public final static int COLUMN_EMAIL = 3;
	public final static int COLUMN_PHONE = 4;
	private static final String CREATE_DB = "create table " + DATABASE_TABLE
			+ " (contactId text, taskId text, name text not null, email text,"
			+ " phone text, primary key(contactId, taskId));";
	private static final int DB_VERSION = 1;
	private ContactDatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private Context context;

	private static class ContactDatabaseHelper extends SQLiteOpenHelper {

		ContactDatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_DB);
			Log.i(TAG, "Database contact created");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
		}
	}

	public ContactDBAdapter(Context context) {
		this.context = context;
		Log.i(TAG, "contact DBAdapter constructed");
	}

	public ContactDBAdapter open() {
		dbHelper = new ContactDatabaseHelper(context, DATABASE_NAME, null,
				DB_VERSION);
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	public long addContact(Contact contact, String taskId) {
		ContentValues value = new ContentValues();
		value.put(KEY_CONTACT_ID, contact.getId());
		value.put(KEY_NAME, contact.getName());
		value.put(KEY_EMAIL, contact.getEmail());
		value.put(KEY_PHONE, contact.getPhone());
		value.put(KEY_TASK_ID, taskId);
		return db.insert(DATABASE_TABLE, null, value);
	}

	public long replaceContact(Contact contact, String taskId) {
		ContentValues value = new ContentValues();
		value.put(KEY_CONTACT_ID, contact.getId());
		value.put(KEY_NAME, contact.getName());
		value.put(KEY_EMAIL, contact.getEmail());
		value.put(KEY_PHONE, contact.getPhone());
		value.put(KEY_TASK_ID, taskId);
		return db.replace(DATABASE_TABLE, null, value);
	}

	public int deleteContactRelatedToContact(String contactId) {
		return db.delete(DATABASE_TABLE, KEY_CONTACT_ID + "='" + contactId,
				null);
	}

	public int deleteContactRelatedToTask(String taskId) {
		return db.delete(DATABASE_TABLE, KEY_TASK_ID + " ='" + taskId + "'",
				null);
	}

	public Cursor getContactRelatedToTask(String taskId) {
		return db.query(DATABASE_TABLE, new String[] { KEY_CONTACT_ID,
				KEY_TASK_ID, KEY_NAME, KEY_EMAIL, KEY_PHONE }, KEY_TASK_ID
				+ " ='" + taskId + "'", null, null, null, null);
	}

	public Cursor getAllGroup() {
		return db.query(DATABASE_TABLE, new String[] { KEY_CONTACT_ID,
				KEY_TASK_ID, KEY_NAME, KEY_EMAIL, KEY_PHONE }, null, null,
				null, null, null);
	}
}
