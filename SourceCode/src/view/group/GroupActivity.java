package view.group;

import java.util.Observable;
import java.util.Observer;

import model.KulSync;
import model.group.Group;
import model.group.GroupModel;
import view.ConfirmDialogFragment;
import view.MainActivity;
import view.group.GroupFormDialogFragment.KulOnGroupFormActionListener;
import vn.edu.rmit.assignment1.R;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.GridView;
import android.widget.Toast;

public class GroupActivity extends Activity implements
		KulOnGroupFormActionListener, Observer {
	private final String TAG = "TODO";
	private GroupModel model;
	private Group[] groups;
	private GridView gridView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Groups");
		model = new GroupModel(this);
		model.addObserver(this);

		Intent intent = getIntent();
		String text = intent.getStringExtra("text");
		if (text != null && !text.equals("")) {
			if (text.equals(KulSync.AUTHORIZE_FAIL)) {
				deauthorize();
			}
			Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		}

		setContentView(R.layout.group_activity);
		gridView = (GridView) findViewById(R.id.gridview);
		registerForContextMenu(gridView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "groupActivity onResume()");
		model.openDB();
		resetGridView();
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.group, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.addGroupMenuItem:
			showGroupFormDialog(null, null, -1);
			break;
		case R.id.groupDeauthorItem:
			deauthorizeConfirm();
		case R.id.groupSync:
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra("sync", true);
			startActivity(intent);
			finish();
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.group_sticker, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info;
		try {
			info = (AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Log.i(TAG, "bad menuInfo", e);
			return false;
		}

		final int position = info.position;
		switch (item.getItemId()) {
		case R.id.editGroupMenuItem:
			showGroupFormDialog(groups[position].getId(),
					groups[position].getName(), position);
			break;
		case R.id.deleteGroupMenuItem:
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ConfirmDialogFragment newFragment = ConfirmDialogFragment
					.newInstance(new ConfirmDialogFragment() {
						@Override
						public void confirm() {
							confirmDelete(position);
						}

						@Override
						public void cancel() {
						}
					}, getString(R.string.delete),
							getString(R.string.deleteGroupQuestion));
			newFragment.show(ft, "confirmDialog");
			break;
		}
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		model.closeDB();
	}

	private void resetGridView() {
		groups = model.getAllGroups();
		gridView.setAdapter(new GroupAdapter(groups, this));
	}

	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof String) {
			resetGridView();
		}
	}

	@Override
	public void ok(String id, String name, int position) {
		if (id == null) {
			// create new group
			model.addGroup(name);
			Toast.makeText(this, "New group is added successfully",
					Toast.LENGTH_SHORT).show();
		} else {
			if (groups[position].getToSync() == Group.ADD_GROUP) {
				model.editGroup(id, name, Group.ADD_GROUP);
			} else {
				model.editGroup(id, name, Group.UPDATE_GROUP);
			}
			Toast.makeText(this, "Change group name successfully",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void confirmDelete(int position) {
		String name = groups[position].getName();
		model.deleteGroup(groups[position]);
		Toast.makeText(this, "Delete group: " + name + " done",
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void cancel() {
	}

	private void showGroupFormDialog(String id, String name, int position) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		GroupFormDialogFragment newFragment = GroupFormDialogFragment
				.newInstance(id, name, position);
		newFragment.show(ft, "dialog");
	}

	private void deauthorizeConfirm() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ConfirmDialogFragment cf = ConfirmDialogFragment.newInstance(
				new ConfirmDialogFragment() {
					@Override
					public void confirm() {
						deauthorize();
					}

					@Override
					public void cancel() {
					}
				}, "Deauthorize?", "After deauthorizing this account, "
						+ "you will need to authorize again if you want"
						+ " to sync with Google Task. This only affects "
						+ "after you restart this application");
		cf.show(ft, "deauthorize");
	}

	private void deauthorize() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(GroupActivity.this);
		Editor edit = preferences.edit();
		edit.putString("token", "");
		edit.commit();
	}
}
