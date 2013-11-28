package model.task;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import model.Contact;

public class Task implements Serializable {
	public static String ADD_TASK = "0";
	public static String UPDATE_TASK = "1";
	public static String DO_NOTHING = "z";
	public static String DELETE = "y";
	private String title;
	private Date date;
	private boolean allDay;
	private String note;
	private String groupId;
	private int priority;
	private boolean completed;
	private ArrayList<Contact> contacts;
	private String id;
	private boolean selected;
	private String toSync;// z means nothing, 0 means all fields.
	private static long currentId = 0;

	public Task(String title, Date date, boolean allDay, String note,
			String groupId, ArrayList<Contact> contacts, int priority) {
		this.id = "T" + currentId;
		this.title = title;
		this.date = date;
		this.note = note;
		this.groupId = groupId;
		this.contacts = contacts;
		this.allDay = allDay;
		this.priority = priority;
		this.toSync = ADD_TASK;
		currentId++;
	}

	public Task(String title, Date date, boolean allDay, String note,
			String groupId, boolean completed, ArrayList<Contact> contacts,
			String id, boolean selected, int priority, String toSync) {
		this.title = title;
		this.date = date;
		this.allDay = allDay;
		this.note = note;
		this.groupId = groupId;
		this.completed = completed;
		this.contacts = contacts;
		this.priority = priority;
		this.id = id;
		this.selected = selected;
		this.toSync = toSync;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getToSync() {
		return toSync;
	}

	public void setToSync(String toSync) {
		this.toSync = toSync;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getTimeString() {
		if (allDay) {
			SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
			String s = format.format(date);
			s += " All day";
			return s;
		} else {
			SimpleDateFormat format = new SimpleDateFormat(
					"dd MMM yyyy - HH:mm");
			String s = format.format(date);
			return s;
		}
	}

	public String getTimeFormatString() {
		SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
		return format.format(date);
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public static long getCurrentId() {
		return currentId;
	}

	public static void setCurrentId(long currentId) {
		Task.currentId = currentId;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isAllDay() {
		return allDay;
	}

	public void setAllDay(boolean allDay) {
		this.allDay = allDay;
	}

	public ArrayList<Contact> getContacts() {
		return contacts;
	}

	public void setContacts(ArrayList<Contact> contacts) {
		this.contacts = contacts;
	}
}
