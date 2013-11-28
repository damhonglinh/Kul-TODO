package model.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;

import model.Contact;
import model.ContactDBAdapter;
import model.StaticVeriableDBAdapter;
import model.group.Group;
import model.group.GroupModel;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class TaskModel extends Observable {
	private TaskDBAdapter taskDB;
	private ContactDBAdapter contactDB;
	private Context context;
	private StaticVeriableDBAdapter staticDB;
	public final static String SORT_BY_PRIORITY = TaskDBAdapter.KEY_PRIORITY;
	public final static String SORT_BY_DATE = TaskDBAdapter.KEY_DATE;
	public final static String SORT_BY_DEFAULT = null;

	public TaskModel(Context context) {
		this.context = context;
		taskDB = new TaskDBAdapter(context);
		contactDB = new ContactDBAdapter(context);
		staticDB = new StaticVeriableDBAdapter(context);
	}

	public void openDB() {
		taskDB.open();
		contactDB.open();
	}

	public Task addTask(String title, String note, String groupId, Date date,
			boolean allDay, ArrayList<Contact> contacts, int priority) {
		Task task = new Task(title, date, allDay, note, groupId, contacts,
				priority);
		taskDB.addTask(task);

		String taskId = task.getId();
		for (int i = 0; i < contacts.size(); i++) {
			Contact contact = contacts.get(i);
			contactDB.addContact(contact, taskId);
		}
		notifyObserverToUpdate();
		return task;
	}

	public Task addSharedTask(Task t) {
		GroupModel gModel = new GroupModel(context);
		gModel.openDB();
		String gId = gModel.getSharedGroupId();
		t.setGroupId(gId);

		taskDB.addTask(t);

		String taskId = t.getId();
		ArrayList<Contact> contacts = t.getContacts();
		for (int i = 0; i < contacts.size(); i++) {
			Contact contact = contacts.get(i);
			contactDB.addContact(contact, taskId);
		}

		Log.i("TODO",
				"tModel addSharedTask(): Task: " + t.getId() + " - "
						+ t.getGroupId() + " - " + t.getTitle());
		notifyObserverToUpdate();
		return t;
	}

	public void addTask(Task[] tasks) {
		for (int i = 0; i < tasks.length; i++) {
			taskDB.replaceTask(tasks[i]);
		}
		notifyObserverToUpdate();
	}

	public void editTask(String id, String title, String note, String groupId,
			Date date, boolean completed, boolean selected, boolean allDay,
			ArrayList<Contact> contacts, int priority, String toSync) {
		Task task = new Task(title, date, allDay, note, groupId, completed,
				contacts, id, selected, priority, toSync);
		taskDB.editTask(task);

		String taskId = task.getId();
		contactDB.deleteContactRelatedToTask(taskId);
		for (int i = 0; i < contacts.size(); i++) {
			Contact contact = contacts.get(i);
			contactDB.addContact(contact, taskId);
		}
		notifyObserverToUpdate();
	}

	public void editTask(Task t) {
		editTask(t.getId(), t.getTitle(), t.getNote(), t.getGroupId(),
				t.getDate(), t.isCompleted(), t.isSelected(), t.isAllDay(),
				t.getContacts(), t.getPriority(), t.getToSync());
	}

	public void deleteTask(Task task) {
		task.setToSync(Task.DELETE);
		editTask(task);
		notifyObserverToUpdate();
	}

	public void deleteTaskWhenSync(String taskId) {
		taskDB.deleteTask(taskId);
		contactDB.deleteContactRelatedToTask(taskId);
		notifyObserverToUpdate();
	}

	public void deleteTasksRelatedToGroup(String groupId) {
		Task[] tasks = getAllTaskRelatedToGroup(groupId, null, false);
		for (int i = 0; i < tasks.length; i++) {
			tasks[i].setToSync(Task.DELETE);
			editTask(tasks[i]);
		}
	}

	public void deleteTasksRelatedToGroupWhenSync(String groupId) {
		Task[] tasks = getAllTaskRelatedToGroup(groupId, null, false);
		for (int i = 0; i < tasks.length; i++) {
			deleteTaskWhenSync(tasks[i].getId());
		}
	}

	public void deleteSelectTask(Task[] tasks) {
		for (int i = 0; i < tasks.length; i++) {
			if (tasks[i].isSelected()) {
				tasks[i].setToSync(Task.DELETE);
				editTask(tasks[i]);
			}
		}
		notifyObserverToUpdate();
	}

	public boolean selectAllTasks(Task[] tasks) {
		boolean allSelected = true;
		for (int i = 0; i < tasks.length; i++) {
			if (!tasks[i].isSelected()) {
				allSelected = false;
				break;
			}
		}

		for (int i = 0; i < tasks.length; i++) {
			tasks[i].setSelected(!allSelected);
			taskDB.editTask(tasks[i]);
		}
		return !allSelected;
	}

	public boolean completeSelectedTasks(Task[] tasks) {
		boolean allCompleted = true;
		for (int i = 0; i < tasks.length; i++) {
			if (!tasks[i].isCompleted()) {
				allCompleted = false;
				break;
			}
		}

		for (int i = 0; i < tasks.length; i++) {
			tasks[i].setCompleted(!allCompleted);
			tasks[i].setToSync(Task.UPDATE_TASK);
			taskDB.editTask(tasks[i]);
		}
		return !allCompleted;
	}

	public boolean completeTask(Task t, boolean completed) {
		t.setCompleted(completed);
		t.setToSync(Task.UPDATE_TASK);
		taskDB.editTask(t);
		return true;
	}

	public boolean setPriority(Task t, int priority) {
		t.setPriority(priority);
		Log.i("TODO", "TaskModel setPriority() for task: " + t.getTitle());

		t.setToSync(Task.UPDATE_TASK);
		taskDB.editTask(t);
		notifyObserverToUpdate();
		return true;
	}

	public void closeDB() {
		staticDB.open();
		staticDB.editVeriable("group", Group.getCurrentId());
		staticDB.editVeriable("task", Task.getCurrentId());
		staticDB.editVeriable("contact", Contact.getCurrentId());
		staticDB.close();
		contactDB.close();
		taskDB.close();
	}

	public Task[] getAllTaskRelatedToGroup(String groupId, String sortBy,
			boolean desc) {
		Log.i("TODO", "TaskModel: sort by: " + sortBy);

		Cursor cur;
		if (groupId == null) {
			cur = taskDB.getAllTask(sortBy, desc);
		} else {
			cur = taskDB.getAllTaskRelatedToGroup(groupId, sortBy, desc);
		}
		return getTaskFromCursor(cur, groupId);
	}

	public Task[] getAllTaskRelatedToGroupWhenSync(String groupId) {
		Cursor cur;
		if (groupId == null) {
			cur = taskDB.getAllTaskWhenSync();
		} else {
			cur = taskDB.getAllTaskRelatedToGroupWhenSync(groupId);
		}
		return getTaskFromCursor(cur, groupId);
	}

	private Task[] getTaskFromCursor(Cursor cur, final String groupId) {
		Task[] tasks = new Task[cur.getCount()];
		cur.moveToFirst();
		int i = 0;
		while (!cur.isAfterLast()) {
			String id = cur.getString(TaskDBAdapter.COLUMN_ID);
			String title = cur.getString(TaskDBAdapter.COLUMN_TITLE);
			Date date = new Date(cur.getLong(TaskDBAdapter.COLUMN_DATE));
			boolean allDay = cur.getInt(TaskDBAdapter.COLUMN_ALL_DAY) == 1;
			boolean completed = cur.getInt(TaskDBAdapter.COLUMN_COMPLETED) == 1;
			boolean isSeleted = cur.getInt(TaskDBAdapter.COLUMN_IS_SELECTED) == 1;
			String note = cur.getString(TaskDBAdapter.COLUMN_NOTE);
			int size = cur.getInt(TaskDBAdapter.COLUMN_HAS_CONTACT);
			String toSync = cur.getString(TaskDBAdapter.COLUMN_TO_SYNC);
			int priority = cur.getInt(TaskDBAdapter.COLUMN_PRIORITY);

			ArrayList<Contact> contacts = new ArrayList<Contact>(size);
			Cursor contactCur = contactDB.getContactRelatedToTask(id);
			contactCur.moveToFirst();
			while (!contactCur.isAfterLast()) {
				String conId = contactCur
						.getString(ContactDBAdapter.COLUMN_CONTACT_ID);
				String conName = contactCur
						.getString(ContactDBAdapter.COLUMN_NAME);
				String conPhone = contactCur
						.getString(ContactDBAdapter.COLUMN_PHONE);
				String conEmail = contactCur
						.getString(ContactDBAdapter.COLUMN_EMAIL);

				Contact contact = new Contact(conId, conName, conEmail,
						conPhone);
				contacts.add(contact);

				contactCur.moveToNext();
			}
			contactCur.close();

			Task t = new Task(title, date, allDay, note, groupId, completed,
					contacts, id, isSeleted, priority, toSync);

			cur.moveToNext();
			tasks[i] = t;
			i++;
		}
		cur.close();
		return tasks;
	}

	private void notifyObserverToUpdate() {
		setChanged();
		notifyObservers("Task");
	}
}
