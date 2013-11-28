package model.group;

import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;

import model.Contact;
import model.StaticVeriableDBAdapter;
import model.task.Task;
import model.task.TaskModel;
import vn.edu.rmit.assignment1.R;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class GroupModel extends Observable {
	private GroupDBAdapter groupDB;
	private StaticVeriableDBAdapter staticDB;
	private Context context;
	private static final String SHARED_GROUP_NAME = "Shared";

	public GroupModel(Context context) {
		this.context = context;
		groupDB = new GroupDBAdapter(context);
		staticDB = new StaticVeriableDBAdapter(context);
		staticDB.open();
		groupDB.open();

		Cursor staticDBCursor = staticDB.getAllRow();
		if (!staticDBCursor.moveToFirst()) {
			initFirstTimeInstall();
		} else {
			// first row is the row of the Group
			Group.setCurrentId(staticDBCursor.getLong(1));
			staticDBCursor.moveToNext();// 2nd row is of the Task
			Task.setCurrentId(staticDBCursor.getLong(1));
			staticDBCursor.moveToNext();// 3rd row is of the Task
			Contact.setCurrentId(staticDBCursor.getLong(1));
		}
		staticDBCursor.close();
		staticDB.close();
		groupDB.close();
	}

	public GroupModel(Context context, boolean flagToInitializeDB) {
		// the flag is just for distinguish from the other constructor
		this.context = context;
		groupDB = new GroupDBAdapter(context);
		staticDB = new StaticVeriableDBAdapter(context);
	}

	private void initFirstTimeInstall() {
		staticDB.addVeriable("group", Group.getCurrentId());
		staticDB.addVeriable("task", Task.getCurrentId());
		staticDB.addVeriable("contact", Contact.getCurrentId());
		addGroup("Work");
		addGroup("Study");
		String groupId = addGroup("Tutorial").getId();
		addGroup(SHARED_GROUP_NAME);

		Date today = new Date();
		TaskModel tModel = new TaskModel(context);
		tModel.openDB();
		tModel.addTask(context.getString(R.string.tuteSwipeTitle),
				context.getString(R.string.tuteSwipeDesc), groupId, today,
				true, new ArrayList<Contact>(), 3);

		tModel.addTask(context.getString(R.string.tuteChangePrioTitle),
				context.getString(R.string.tuteChangePrioDesc), groupId, today,
				true, new ArrayList<Contact>(), 3);

		tModel.addTask(context.getString(R.string.tuteRightClickTitle),
				context.getString(R.string.tuteRightClickDesc), groupId, today,
				true, new ArrayList<Contact>(), 3);
		tModel.closeDB();
	}

	public void openDB() {
		groupDB.open();
	}

	public String getSharedGroupId() {
		Cursor cur = groupDB.getGroupByName(SHARED_GROUP_NAME);
		if (cur.moveToFirst()) {
			Group group = new Group(cur.getString(0), cur.getString(1),
					cur.getInt(2));
			cur.close();
			// Log.i("TODO", "line 66 GroupModel getShareGroupId true");
			return group.getId();
		} else {
			Group g = addGroup(SHARED_GROUP_NAME);
			cur.close();
			// Log.i("TODO", "line 66 GroupModel getShareGroupId false");
			return g.getId();
		}
	}

	public Group addGroup(String groupName) {
		Group g = new Group(groupName);
		groupDB.addGroup(g);
		notifyObserverToUpdate();
		return g;
	}

	public void addGroup(Group[] groups) {
		for (int i = 0; i < groups.length; i++) {
			groupDB.replaceGroup(groups[i]);
		}
		notifyObserverToUpdate();
	}

	public void editGroup(String groupId, String groupName, int toSync) {
		groupDB.editGroup(new Group(groupId, groupName, toSync));
		notifyObserverToUpdate();
	}

	// actually delete from DB
	public void deleteGroupWhenSync(String groupId) {
		groupDB.deleteGroup(groupId);
		TaskModel taskModel = new TaskModel(context);
		taskModel.openDB();
		taskModel.deleteTasksRelatedToGroupWhenSync(groupId);
		taskModel.closeDB();
		notifyObserverToUpdate();
	}

	// edit group.getToSync() = DELETE
	public void deleteGroup(Group g) {
		g.setToSync(Group.DELETE);
		groupDB.editGroup(g);
		TaskModel taskModel = new TaskModel(context);
		taskModel.openDB();
		taskModel.deleteTasksRelatedToGroup(g.getId());
		taskModel.closeDB();
		notifyObserverToUpdate();
	}

	public Group getGroup(String gId) {
		Cursor cur = groupDB.getGroup(gId);
		cur.moveToFirst();
		String id = cur.getString(0);
		String name = cur.getString(1);
		int toSync = cur.getInt(2);
		cur.close();
		return new Group(id, name, toSync);
	}

	public void closeDB() {
		staticDB.open();
		staticDB.editVeriable("group", Group.getCurrentId());
		staticDB.editVeriable("task", Task.getCurrentId());
		staticDB.editVeriable("contact", Contact.getCurrentId());
		staticDB.close();
		groupDB.close();
	}

	public Group[] getAllGroups() {
		Cursor cur = groupDB.getAllGroup();
		Group[] groups = new Group[cur.getCount()];

		int i = 0;
		if (!cur.moveToFirst()) {
			return null;
		}

		while (!cur.isAfterLast()) {
			Group g = new Group(cur.getString(0), cur.getString(1),
					cur.getInt(2));
			groups[i] = g;
			cur.moveToNext();
			i++;
		}
		cur.close();
		return groups;
	}

	public Group[] getAllGroupsWhenSync() {

		Cursor cur = groupDB.getAllGroupWhenSync();
		Group[] groups = new Group[cur.getCount()];

		int i = 0;
		if (!cur.moveToFirst()) {
			Log.i("TODO",
					"getAllGroupWhenSync(): return null: cur.moveToFirst = null ");
			return null;
		}

		while (!cur.isAfterLast()) {
			Group g = new Group(cur.getString(0), cur.getString(1),
					cur.getInt(2));
			groups[i] = g;
			cur.moveToNext();
			i++;
		}
		cur.close();
		return groups;
	}

	private void notifyObserverToUpdate() {
		setChanged();
		notifyObservers("Group");
	}
}
