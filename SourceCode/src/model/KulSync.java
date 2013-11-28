package model;

import model.group.Group;
import model.group.GroupModel;
import model.group.GroupSync;
import model.task.Task;
import model.task.TaskModel;
import model.task.TaskSync;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import exception.AuthorizeException;

public class KulSync extends AsyncTask<Void, Void, Void> {
	private String token;
	private Activity from;
	private Class<? extends Activity> to;
	private final String CLIENT_ID;
	private final String CLIENT_SECRECT;
	private final String API_KEY;
	public static final String AUTHORIZE_FAIL = "Authorizing your account fails!"
			+ " Please authorize again!";

	public KulSync(String token, String clientId, String clientSecret,
			String APIKey, Activity from, Class<? extends Activity> to) {
		this.token = token;
		this.from = from;
		this.to = to;
		this.CLIENT_ID = clientId;
		this.CLIENT_SECRECT = clientSecret;
		this.API_KEY = APIKey;
	}

	@Override
	public Void doInBackground(Void... params) {
		sync();
		return null;
	}

	private void sync() {
		GroupModel gModel = new GroupModel(from);
		TaskModel tModel = new TaskModel(from);
		try {
			gModel.openDB();
			tModel.openDB();
			Group[] gGroups;
			Task[][] gTasks;
			Group[] mGroups = gModel.getAllGroupsWhenSync();
			if (mGroups == null) {
				Log.i("TODO", "mGroup = null in KulSync");
				mGroups = new Group[0];
			}
			Task[][] mTasks = new Task[mGroups.length][];

			Log.i("TODO",
					"check mGroup content before upload to google: mGroups[0]"
							+ mGroups[0].getName());

			GroupSync.uploadGroup(gModel, tModel, mGroups, token, CLIENT_ID,
					CLIENT_SECRECT, API_KEY);
			for (int i = 0; i < mGroups.length; i++) {
				Log.i("TODO", "upload tasks in list: " + mGroups[i].getName());
				mTasks[i] = tModel.getAllTaskRelatedToGroupWhenSync(mGroups[i]
						.getId());
				if (isCancelled()) {
					return;
				}
				TaskSync.uploadTask(tModel, mTasks[i], mGroups[i].getId(),
						token, CLIENT_ID, CLIENT_SECRECT, API_KEY);
			}

			if (isCancelled()) {
				return;
			}

			gGroups = GroupSync.receiveGroupsFromGoogle(token, CLIENT_ID,
					CLIENT_SECRECT, API_KEY);
			gTasks = new Task[gGroups.length][];

			for (int i = 0; i < gGroups.length; i++) {
				if (isCancelled()) {
					return;
				}
				String groupId = gGroups[i].getId();
				Log.i("TODO", "in for loop to get task from Google : "
						+ groupId);
				Task[] tasks = TaskSync.receiveTasksFromGoogle(token, groupId,
						CLIENT_ID, CLIENT_SECRECT, API_KEY);
				if (tasks != null) {
					gTasks[i] = tasks;
					tModel.addTask(gTasks[i]);
				}
			}

			gModel.addGroup(gGroups);
			nextActivity("Syncing successfully");
		} catch (AuthorizeException e) {
			nextActivity(AUTHORIZE_FAIL);
		} catch (Exception e) {
			nextActivity("Syncing fails. Please check your network connection!");
		} finally {
			gModel.closeDB();
			tModel.closeDB();
		}
	}

	private void nextActivity(String text) {
		if (from != null && to != null) {
			Intent intent = new Intent(from, to);
			intent.putExtra("text", text);
			from.startActivity(intent);
			from.finish();
		}
	}
}
