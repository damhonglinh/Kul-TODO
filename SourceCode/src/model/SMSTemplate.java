package model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import model.task.Task;
import android.util.Log;

public class SMSTemplate {
	private Task task;
	private String sms;

	public SMSTemplate(Task task) {
		this.task = task;
	}

	public SMSTemplate(String sms) {
		this.sms = sms;
	}

	public Task getTask() {
		try {
			int titleIndex = sms.indexOf("Title: ");
			int noteIndex = sms.indexOf("\nNote: ");
			int prioIndex = sms.indexOf("\nPriority: ");
			int dueIndex = sms.indexOf("\nDue: ");
			int collIndex = sms.indexOf("\nCollaborator:\n");

			if (titleIndex < 0 || noteIndex < 0 || prioIndex < 0
					|| dueIndex < 0 || collIndex < 0) {
				throw new Exception();
			}
			String title = sms.substring(7, noteIndex);
			String note = sms.substring(noteIndex + 7, prioIndex);
			int prio = Integer
					.parseInt(sms.substring(prioIndex + 11, dueIndex));
			String dateString = sms.substring(dueIndex + 6, collIndex);
			String coll = sms.substring(collIndex + 15);

			if (prio < 1 || prio > 3) {
				throw new Exception("Priority less than 0 or greater than 3");
			}

			int allDayIndex = dateString.indexOf(" All day");
			boolean allday = false;
			String pattern = "dd MMM yyyy - HH:mm";

			if (allDayIndex > 0) {
				dateString = dateString.substring(0, allDayIndex);
				pattern = "dd MMM yyyy";
				allday = true;
			}

			Date date = new SimpleDateFormat(pattern).parse(dateString);

			String[] contactString = coll.split("[\\r\\n]+");// split ENTER key
			ArrayList<Contact> contacts = new ArrayList<Contact>(
					contactString.length);
			for (int i = 0; i < contactString.length; i++) {
				String[] contactInfo = contactString[i].split("\\s-\\s");// " - "
				if (contactInfo.length == 2) {
					Contact c = new Contact(contactInfo[0], contactInfo[1], "");
					contacts.add(c);
				} else {
					Contact c = new Contact(contactInfo[0], contactInfo[1],
							contactInfo[2]);
					contacts.add(c);
				}
			}

			task = new Task(title, date, allday, note, "", contacts, prio);
		} catch (Exception e) {
			Log.i("TODO", e.getStackTrace() + " SMS does not in proper format!");
			task = null;
		}
		return task;
	}

	public String getSMS() {
		sms = "Title: " + task.getTitle() + "\n";
		sms += "Note: " + task.getNote() + "\n";
		sms += "Priority: " + task.getPriority() + "\n";
		sms += "Due: " + task.getTimeString() + "\n";
		sms += "Collaborator:\n";
		for (int i = 0; i < task.getContacts().size(); i++) {
			Contact c = task.getContacts().get(i);
			sms += c.getName() + " - " + c.getEmail() + " - " + c.getPhone()
					+ "\n";
		}
		return sms;
	}
}
