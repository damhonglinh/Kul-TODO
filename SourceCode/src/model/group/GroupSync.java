package model.group;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import model.task.Task;
import model.task.TaskModel;

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

public class GroupSync {

	private static final String ID_TAG = "id";
	private static final String TITLE_TAG = "title";
	private static final String ARRAY_TAG = "items";

	public static Group[] receiveGroupsFromGoogle(String token,
			String clientId, String clientSecret, String APIKey)
			throws AuthorizeException {
		HttpURLConnection connect = null;
		try {
			URL url = new URL(
					"https://www.googleapis.com/tasks/v1/users/@me/lists?key="
							+ APIKey);
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
				Log.i("TODO", "Invalid Response Code when downloading groups: "
						+ statusCode);
				return null;
			}

			JSONObject jObj = new JSONObject(getJSONString(connect));
			return jsonToGroups(jObj);

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
		// Log.i("TODO", "receiveGroup:\n" + sb.toString());
		return sb.toString();
	}

	private static Group[] jsonToGroups(JSONObject jObj) {
		Group[] groups = null;
		try {
			JSONArray jsArray = jObj.getJSONArray(ARRAY_TAG);
			groups = new Group[jsArray.length()];

			for (int i = 0; i < jsArray.length(); i++) {
				JSONObject o = jsArray.getJSONObject(i);
				String id = o.getString(ID_TAG);
				String title = o.getString(TITLE_TAG);

				Group g = new Group(id, title, Group.DO_NOTHING);
				groups[i] = g;
			}
		} catch (Exception e) {
			Log.i("TODO", e.toString() + " EXCEPTION LINE 94 jsonToGroups");
			groups = new Group[0];
		}
		return groups;
	}

	public static void uploadGroup(GroupModel gModel, TaskModel tModel,
			Group[] groups, String token, String clientId, String clientSecret,
			String APIKey) {
		for (int i = 0; i < groups.length; i++) {
			try {
				Log.i("TODO", "In for loop uploadGROUP, groups[" + i
						+ "].getName() = " + groups[i].getName()
						+ " .getToSync() = " + groups[i].getToSync());

				if (groups[i].getToSync() == Group.ADD_GROUP) {
					Log.i("TODO", "Start Add group: " + groups[i].getName());
					String groupId = addGroupToGoogle(groups[i], token,
							clientId, clientSecret, APIKey);

					Log.i("TODO", "groupId before change: " + groups[i].getId());
					Log.i("TODO", "groupId after change: " + groupId);
					// because the groups that contain these tasks have moved to
					// cloud, so the groupId of these task need updating
					Task[] tasks = tModel.getAllTaskRelatedToGroup(
							groups[i].getId(), null, false);
					for (int j = 0; j < tasks.length; j++) {
						Log.i("TODO",
								"Changing groupId of task: "
										+ tasks[j].getTitle()
										+ " to new groupId: " + groupId);
						tasks[j].setGroupId(groupId);
						tModel.editTask(tasks[j]);
					}

					gModel.deleteGroupWhenSync(groups[i].getId());
					Log.i("TODO", "End add group: " + groups[i].getName());
				} else if (groups[i].getToSync() == Group.UPDATE_GROUP) {
					Log.i("TODO", "Start update group: " + groups[i].getName());
					updateGroupToGoogle(groups[i], token, clientId,
							clientSecret, APIKey);
					groups[i].setToSync(Group.DO_NOTHING);
					Log.i("TODO", "End update group: " + groups[i].getName());
				} else if (groups[i].getToSync() == Group.DELETE) {
					deleteGroupFromGoogle(groups[i], token, clientId,
							clientSecret, APIKey);
					gModel.deleteGroupWhenSync(groups[i].getId());
					Log.i("TODO",
							"Del completed: GROUP name: " + groups[i].getName());
				}
			} catch (Exception e) {
				Log.i("TODO", e.toString() + "\n" + e.getStackTrace());
			}
		}
	}

	private static String addGroupToGoogle(Group group, String token,
			String clientId, String clientSecret, String APIKey)
			throws Exception {
		URL url = new URL(
				"https://www.googleapis.com/tasks/v1/users/@me/lists?key="
						+ APIKey);
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost post = new HttpPost(url.toURI());

		post.setHeader("client_id", clientId);
		post.setHeader("client_secret", clientSecret);
		post.setHeader("Authorization", "OAuth " + token);
		post.setHeader("Content-Type", "application/json");

		String jSonString = getJSONStringToAdd(group);
		post.setEntity(new StringEntity(jSonString));
		HttpResponse res = httpClient.execute(post);

		Log.i("TODO", jSonString);

		// get response:
		BufferedReader br = new BufferedReader(new InputStreamReader(res
				.getEntity().getContent()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
			sb.append('\n');
		}
		// Log.i("TODO", "RESPONSE " + sb);
		// done get response

		JSONObject o = new JSONObject(sb.toString());
		String id = o.getString(ID_TAG);
		return id;
	}

	private static void updateGroupToGoogle(Group group, String token,
			String clientId, String clientSecret, String APIKey)
			throws Exception {
		URL url = new URL(
				"https://www.googleapis.com/tasks/v1/users/@me/lists/"
						+ group.getId() + "?key=" + APIKey);
		HttpClient httpClient = new DefaultHttpClient();
		HttpPut put = new HttpPut(url.toURI());

		put.setHeader("client_id", clientId);
		put.setHeader("client_secret", clientSecret);
		put.setHeader("Authorization", "OAuth " + token);
		put.setHeader("Content-Type", "application/json");

		String jSonString = getJSONStringToUpdate(group);
		put.setEntity(new StringEntity(jSonString));
		HttpResponse res = httpClient.execute(put);

		// get response:
		BufferedReader br = new BufferedReader(new InputStreamReader(res
				.getEntity().getContent()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
			sb.append('\n');
		}
		// Log.i("TODO", "RESPONSE " + sb + "\n");
		// done get response

		// Log.i("TODO", jSonString + "\n");
	}

	private static void deleteGroupFromGoogle(Group group, String token,
			String clientId, String clientSecret, String APIKey)
			throws Exception {
		URL url = new URL(
				"https://www.googleapis.com/tasks/v1/users/@me/lists/"
						+ group.getId() + "?key=" + APIKey);
		HttpClient httpClient = new DefaultHttpClient();
		HttpDelete delete = new HttpDelete(url.toURI());

		delete.setHeader("client_id", clientId);
		delete.setHeader("client_secret", clientSecret);
		delete.setHeader("Authorization", "OAuth " + token);
		delete.setHeader("Content-Type", "application/json");

		// String jSonString = getJSONStringToUpdate(group);
		// delete.setEntity(new StringEntity(jSonString));
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

		// Log.i("TODO", jSonString + "\n");
	}

	private static String getJSONStringToAdd(Group g) {
		String jsonString = "";
		String title = g.getName();

		try {
			JSONObject o = new JSONObject();
			o.put(TITLE_TAG, title);
			jsonString = o.toString();
		} catch (Exception e) {
			Log.i("TODO", "JsonException when String to JSON");
			e.printStackTrace();
		}
		return jsonString;
	}

	private static String getJSONStringToUpdate(Group g) {
		String jsonString = "";
		String title = g.getName();

		try {
			JSONObject o = new JSONObject();
			o.put(TITLE_TAG, title);
			o.put(ID_TAG, g.getId());
			jsonString = o.toString();
		} catch (Exception e) {
			Log.i("TODO", "JsonException when String to JSON");
			e.printStackTrace();
		}
		return jsonString;
	}
}
