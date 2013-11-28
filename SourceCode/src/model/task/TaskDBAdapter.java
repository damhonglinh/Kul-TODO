package model.task;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class TaskDBAdapter {
	protected static final String TAG = "TODO";
	protected static final String DATABASE_NAME = "todolistTask";
	static final String KEY_ID = "_id";
	static final String KEY_TITLE = "title";
	static final String KEY_NOTE = "note";
	static final String KEY_GROUP_ID = "groupId";
	static final String KEY_DATE = "dueDate";
	static final String KEY_ALL_DAY = "allDay";
	static final String KEY_COMPLETED = "completed";
	static final String KEY_IS_SELECTED = "isSelected";
	static final String KEY_HAS_CONTACT = "hasContact";
	static final String KEY_PRIORITY = "priority";
	static final String KEY_TO_SYNC = "toSync";
	static final int COLUMN_ID = 0;
	static final int COLUMN_TITLE = 1;
	static final int COLUMN_NOTE = 2;
	static final int COLUMN_GROUP_ID = 3;
	static final int COLUMN_DATE = 4;
	static final int COLUMN_ALL_DAY = 5;
	static final int COLUMN_COMPLETED = 6;
	static final int COLUMN_IS_SELECTED = 7;
	static final int COLUMN_HAS_CONTACT = 8;
	static final int COLUMN_PRIORITY = 9;
	static final int COLUMN_TO_SYNC = 10;

	static final String DATABASE_TABLE = "task";
	private static final String CREATE_DB = "create table "
			+ DATABASE_TABLE
			+ " (_id text primary key, title text, note text, groupId text, dueDate"
			+ " int, allDay int, completed int, isSelected int, hasContact int,"
			+ " priority int, toSync text);";
	private static final int DB_VERSION = 1;
	private TaskDatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private Context context;

	private static class TaskDatabaseHelper extends SQLiteOpenHelper {

		public TaskDatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_DB);
			// Log.i(TAG, "Database task created");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
		}
	}

	TaskDBAdapter(Context context) {
		this.context = context;
		// Log.i(TAG, "task DBAdapter constructed");
	}

	TaskDBAdapter open() {
		if (dbHelper != null) {
			dbHelper.close();
		}
		dbHelper = new TaskDatabaseHelper(context, DATABASE_NAME, null,
				DB_VERSION);
		db = dbHelper.getWritableDatabase();
		return this;
	}

	void close() {
		dbHelper.close();
	}

	long addTask(Task task) {
		ContentValues values = taskToValuesNoId(task);
		values.put(KEY_ID, task.getId());
		return db.insert(DATABASE_TABLE, null, values);
	}

	long replaceTask(Task task) {
		ContentValues values = taskToValuesNoId(task);
		values.put(KEY_ID, task.getId());
		return db.replace(DATABASE_TABLE, null, values);
	}

	int editTask(Task task) {
		ContentValues values = taskToValuesNoId(task);
		return db.update(DATABASE_TABLE, values, KEY_ID + "='" + task.getId()
				+ "'", null);
	}

	int deleteTask(String taskId) {
		return db.delete(DATABASE_TABLE, KEY_ID + "='" + taskId + "'", null);
	}

	Cursor getTask(String id) {
		return db.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_TITLE,
				KEY_NOTE, KEY_GROUP_ID, KEY_DATE, KEY_ALL_DAY, KEY_COMPLETED,
				KEY_IS_SELECTED, KEY_HAS_CONTACT, KEY_PRIORITY, KEY_TO_SYNC },
				KEY_ID + "='" + id + "'", null, null, null, null);
	}

	Cursor getAllTask(String sortBy, boolean desc) {
		if (sortBy != null) {
			if (desc) {
				sortBy += " DESC";
			} else {
				sortBy += " ASC";
			}
		}
		return db.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_TITLE,
				KEY_NOTE, KEY_GROUP_ID, KEY_DATE, KEY_ALL_DAY, KEY_COMPLETED,
				KEY_IS_SELECTED, KEY_HAS_CONTACT, KEY_PRIORITY, KEY_TO_SYNC },
				KEY_TO_SYNC + " != '" + Task.DELETE + "'", null, null, null,
				sortBy);
	}

	Cursor getAllTaskRelatedToGroup(String groupId, String sortBy, boolean desc) {
		if (sortBy != null) {
			if (desc) {
				sortBy += " DESC";
			} else {
				sortBy += " ASC";
			}
		}
		Log.i("TODO", "TaskDB: sort by: " + sortBy);
		return db
				.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_TITLE,
						KEY_NOTE, KEY_GROUP_ID, KEY_DATE, KEY_ALL_DAY,
						KEY_COMPLETED, KEY_IS_SELECTED, KEY_HAS_CONTACT,
						KEY_PRIORITY, KEY_TO_SYNC }, KEY_GROUP_ID + "='"
						+ groupId + "' and " + KEY_TO_SYNC + " != '"
						+ Task.DELETE + "'", null, null, null, sortBy);
	}

	Cursor getAllTaskWhenSync() {
		return db.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_TITLE,
				KEY_NOTE, KEY_GROUP_ID, KEY_DATE, KEY_ALL_DAY, KEY_COMPLETED,
				KEY_IS_SELECTED, KEY_HAS_CONTACT, KEY_PRIORITY, KEY_TO_SYNC },
				null, null, null, null, null);
	}

	Cursor getAllTaskRelatedToGroupWhenSync(String groupId) {
		return db.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_TITLE,
				KEY_NOTE, KEY_GROUP_ID, KEY_DATE, KEY_ALL_DAY, KEY_COMPLETED,
				KEY_IS_SELECTED, KEY_HAS_CONTACT, KEY_PRIORITY, KEY_TO_SYNC },
				KEY_GROUP_ID + "='" + groupId + "'", null, null, null, null);
	}

	private ContentValues taskToValuesNoId(Task task) {
		ContentValues value = new ContentValues();
		value.put(KEY_TITLE, task.getTitle());
		value.put(KEY_NOTE, task.getNote());
		value.put(KEY_GROUP_ID, task.getGroupId());
		value.put(KEY_DATE, task.getDate().getTime());
		value.put(KEY_ALL_DAY, task.isAllDay() ? 1 : 0);
		value.put(KEY_COMPLETED, task.isCompleted() ? 1 : 0);
		value.put(KEY_IS_SELECTED, task.isSelected() ? 1 : 0);
		value.put(KEY_HAS_CONTACT, task.getContacts().size());
		value.put(KEY_PRIORITY, task.getPriority());
		value.put(KEY_TO_SYNC, task.getToSync());
		return value;
	}
}
