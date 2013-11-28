package model;

import java.io.Serializable;

public class Contact implements Serializable {

	private String name;
	private String id;
	private static long currentId = 0;
	private String email;
	private String phone;

	public Contact(String name, String email, String phone) {
		this.id = "C" + currentId;
		this.name = name;
		this.email = email;
		this.phone = phone;
		currentId++;
	}

	public Contact(String id, String name, String email, String phone) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.phone = phone;
	}

	public static long getCurrentId() {
		return currentId;
	}

	public static void setCurrentId(long currentId) {
		Contact.currentId = currentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
}
