package model.task;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import model.Contact;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;
import exception.AuthorizeException;

public class TaskSync {

	private static final String ID_TAG = "id";
	private static final String TITLE_TAG = "title";
	private static final String NOTE_TAG = "notes";
	private static final String DATE_TAG = "due";
	private static final String COMPLETED_TAG = "status";
	private static final String ARRAY_TAG = "items";

	public static Task[] receiveTasksFromGoogle(String token, String listId,
			String clientId, String clientSecret, String APIKey)
			throws AuthorizeException {
		HttpURLConnection connect = null;
		try {
			URL url = new URL("https://www.googleapis.com/tasks/v1/lists/"
					+ listId + "/tasks?key=" + APIKey);
			connect = (HttpURLConnection) url.openConnection();
			connect.addRequestProperty("client_id", clientId);
			connect.addRequestProperty("client_secret", clientSecret);
			connect.setRequestProperty("Authorization", "OAuth " + token);
			connect.setReadTimeout(15000);
			connect.setConnectTimeout(15000);

			int statusCode = connect.getResponseCode();
			switch (statusCode) {
			case HttpURLConnection.HTTP_OK:
				break;
			case 401:
				throw new AuthorizeException();
			default:
				Log.i("TODO", "Invalid Response Code when downloading tasks: "
						+ statusCode);
				return null;
			}

			JSONObject jObj = new JSONObject(getJSONString(connect));
			return jsonToTasks(jObj, listId);

		} catch (AuthorizeException ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			Log.i("TODO", "Exception caught: " + ex.toString());
			ex.printStackTrace();
		} finally {
			if (connect != null) {
				connect.disconnect();
			}
		}
		return null;
	}

	private static String getJSONString(HttpURLConnection connect)
			throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				connect.getInputStream()));

		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}
		return sb.toString();
	}

	private static Task[] jsonToTasks(JSONObject jObj, String listId) {
		Task[] tasks = null;
		JSONArray jsArray = null;
		try {
			jsArray = jObj.getJSONArray(ARRAY_TAG);
			tasks = new Task[jsArray.length()];

			for (int i = 0; i < jsArray.length(); i++) {
				JSONObject o = null;
				String id = "";
				String title = "";
				String note = "";
				String groupId = listId;
				String dateString = "";
				boolean complete = false;
				Date date = new Date();
				try {
					o = jsArray.getJSONObject(i);
					id = o.getString(ID_TAG);
					title = o.getString(TITLE_TAG);
				} catch (Exception e) {
					Log.i("TODO", "EXCEPTION LINE 102 TaskSync " + e.toString());
					return null;
				}
				try {
					note = o.getString(NOTE_TAG);
				} catch (Exception ex) {
					note = "";
				}
				try {
					dateString = o.getString(DATE_TAG).substring(0, 10);
					date = new SimpleDateFormat("yyyy-mm-dd").parse(dateString);
				} catch (Exception ex) {
					// do nothing
					Log.i("TODO", "EXCEPTION TaskSync line 116" + ex.toString());
				}
				try {
					complete = !(o.getString(COMPLETED_TAG)
							.equals("needsAction"));
				} catch (Exception ex) {
					complete = false;
				}

				Task t = new Task(title, date, false, note, groupId, complete,
						new ArrayList<Contact>(), id, false, 1, "z");
				tasks[i] = t;
			}
		} catch (Exception e) {
			Log.i("TODO", e.toString() + " EXCEPTION LINE 128 TaskSync");
			return null;
		}
		return tasks;
	}

	public static void uploadTask(TaskModel tModel, Task[] tasks,
			String listId, String token, String clientId, String clientSecret,
			String APIKey) {
		for (int i = 0; i < tasks.length; i++) {
			try {
				Log.i("TODO", "In for loop uploadTASK, tasks[" + i
						+ "].getTitle() = " + tasks[i].getTitle()
						+ " .getToSync() = " + tasks[i].getToSync());

				if (tasks[i].getToSync().equals(Task.ADD_TASK)) {
					addTaskToGoogle(tasks[i], listId, token, clientId,
							clientSecret, APIKey);
					Log.i("TODO",
							"gonna delete native task " + tasks[i].getTitle());
					tModel.deleteTaskWhenSync(tasks[i].getId());

				} else if (tasks[i].getToSync().equals(Task.UPDATE_TASK)) {
					updateTaskToGoogle(tasks[i], listId, token, clientId,
							clientSecret, APIKey);
					tasks[i].setToSync(Task.DO_NOTHING);
				} else if (tasks[i].getToSync().equals(Task.DELETE)) {
					deleteTaskFromGoogle(tasks[i], listId, token, clientId,
							clientSecret, APIKey);
					tModel.deleteTaskWhenSync(tasks[i].getId());
					Log.i("TODO",
							"Del completed: TASK name: " + tasks[i].getTitle());
				}
			} catch (Exception e) {
				Log.i("TODO", e.toString() + "\n" + e.getStackTrace());
				Log.i("TODO", "\n task: " + tasks[i].getTitle());
			}
		}
	}

	private static void addTaskToGoogle(Task task, String listId, String token,
			String clientId, String clientSecret, String APIKey)
			throws Exception {
		URL url = new URL("https://www.googleapis.com/tasks/v1/lists/" + listId
				+ "/tasks?key=" + APIKey);
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost post = new HttpPost(url.toURI());

		post.setHeader("client_id", clientId);
		post.setHeader("client_secret", clientSecret);
		post.setHeader("Authorization", "OAuth " + token);
		post.setHeader("Content-Type", "application/json");

		String jSonString = getJSONStringToAdd(task);
		post.setEntity(new StringEntity(jSonString));
		httpClient.execute(post);

		Log.i("TODO", jSonString);
	}

	private static void updateTaskToGoogle(Task task, String listId,
			String token, String clientId, String clientSecret, String APIKey)
			throws Exception {
		URL url = new URL("https://www.googleapis.com/tasks/v1/lists/" + listId
				+ "/tasks/" + task.getId() + "?key=" + APIKey);
		HttpClient httpClient = new DefaultHttpClient();
		HttpPut put = new HttpPut(url.toURI());

		put.setHeader("client_id", clientId);
		put.setHeader("client_secret", clientSecret);
		put.setHeader("Authorization", "OAuth " + token);
		put.setHeader("Content-Type", "application/json");

		String jSonString = getJSONStringToUpdate(task);
		put.setEntity(new StringEntity(jSonString));
		HttpResponse res = httpClient.execute(put);

		// // get response:
		// BufferedReader br = new BufferedReader(new InputStreamReader(res
		// .getEntity().getContent()));
		// StringBuilder sb = new StringBuilder();
		// String line;
		// while ((line = br.readLine()) != null) {
		// sb.append(line);
		// sb.append('\n');
		// }
		// Log.i("TODO", "RESPONSE " + sb + "\n");
		// // done get response
		//
		// Log.i("TODO", jSonString);
	}

	private static void deleteTaskFromGoogle(Task task, String listId,
			String token, String clientId, String clientSecret, String APIKey)
			throws Exception {
		URL url = new URL("https://www.googleapis.com/tasks/v1/lists/" + listId
				+ "/tasks/" + task.getId() + "?key=" + APIKey);
		HttpClient httpClient = new DefaultHttpClient();
		HttpDelete delete = new HttpDelete(url.toURI());

		delete.setHeader("client_id", clientId);
		delete.setHeader("client_secret", clientSecret);
		delete.setHeader("Authorization", "OAuth " + token);
		delete.setHeader("Content-Type", "application/json");

		HttpResponse res = httpClient.execute(delete);

		// get response:
		BufferedReader br = new BufferedReader(new InputStreamReader(res
				.getEntity().getContent()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
			sb.append('\n');
		}
		Log.i("TODO", "RESPONSE " + sb + "\n");
		// done get response

		// Log.i("TODO", jSonString);
	}

	private static String getJSONStringToAdd(Task t) {
		String jsonString = "";
		String title = t.getTitle();
		String note = t.getNote();
		String complete = (t.isCompleted()) ? "completed" : "needsAction";
		String dateString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
				.format(t.getDate());

		try {
			JSONObject o = new JSONObject();
			o.put(NOTE_TAG, note);
			o.put(TITLE_TAG, title);
			o.put(COMPLETED_TAG, complete);
			o.put(DATE_TAG, dateString);
			jsonString = o.toString();
		} catch (Exception e) {
			Log.i("TODO", "JsonException when String to JSON");
			e.printStackTrace();
		}
		return jsonString;
	}

	private static String getJSONStringToUpdate(Task t) {
		String jsonString = "";
		String title = t.getTitle();
		String note = t.getNote();
		String complete = (t.isCompleted()) ? "completed" : "needsAction";
		String dateString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
				.format(t.getDate());

		try {
			JSONObject o = new JSONObject();
			o.put(ID_TAG, t.getId());
			o.put(NOTE_TAG, note);
			o.put(TITLE_TAG, title);
			o.put(COMPLETED_TAG, complete);
			o.put(DATE_TAG, dateString);
			jsonString = o.toString();
		} catch (Exception e) {
			Log.i("TODO", "JsonException when String to JSON");
			e.printStackTrace();
		}
		return jsonString;
	}
}
