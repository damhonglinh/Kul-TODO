package model.group;

import java.io.Serializable;

public class Group implements Serializable {
	public static int ADD_GROUP = 0;
	public static int UPDATE_GROUP = 1;
	public static int DO_NOTHING = -1;
	public static int DELETE = 99;
	private static long currentId;
	private String id;
	private String name;
	private int toSync;

	public Group(String name) {
		this.id = "G" + currentId;
		currentId++;
		this.name = name;
		this.toSync = ADD_GROUP;
	}

	public Group(String id, String name, int toSync) {
		this.id = id;
		this.name = name;
		this.toSync = toSync;
	}

	public int getToSync() {
		return toSync;
	}

	public void setToSync(int toSync) {
		this.toSync = toSync;
	}

	public static long getCurrentId() {
		return currentId;
	}

	public static void setCurrentId(long currentId) {
		Group.currentId = currentId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
