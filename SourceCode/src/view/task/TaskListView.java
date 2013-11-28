package view.task;

import java.util.Observable;
import java.util.Observer;

import model.task.Task;
import model.task.TaskModel;
import view.ConfirmDialogFragment;
import vn.edu.rmit.assignment1.R;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class TaskListView extends Activity implements Observer {
	private TaskModel model;
	private Task[] tasks;
	private String groupId;
	private String groupName;
	private ListView taskList;
	private boolean isSortedByDate;
	private boolean isSortedByPrio;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		groupId = intent.getStringExtra("groupid");
		groupName = intent.getStringExtra("groupName");
		setTitle(groupName);

		model = new TaskModel(this);
		model.addObserver(this);
		setContentView(R.layout.task_list_view);
		taskList = (ListView) findViewById(R.id.taskList);
	}

	@Override
	protected void onResume() {
		super.onResume();
		model.openDB();
		resetTaskList();
	}

	@Override
	protected void onPause() {
		super.onPause();
		model.closeDB();
	}

	@Override
	protected void onStop() {
		super.onStop();
		model.closeDB();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		model.closeDB();
	}

	private void resetTaskList() {
		tasks = model.getAllTaskRelatedToGroup(groupId, null, false);
		taskList.setAdapter(new TaskLineAdapter(model, tasks, this, groupId));
	}

	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof String) {
			resetTaskList();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.task_list_view_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.taskListMenuAdd:
			showTaskView(null);
			break;
		case R.id.taskListMenuDelete:
			deleteSelected();
			break;
		case R.id.taskListMenuSelectAll:
			selectAll(item);
			break;
		case R.id.taskListMenuCompleteTask:
			completeTask();
			break;
		case R.id.taskListSortByPrio:
			sortByPrio();
			break;
		case R.id.taskListSortByDate:
			sortByDate();
			break;
		case R.id.taskListSortByAdded:
			sortByAdded();
			break;
		}
		return true;
	}

	private void showTaskView(Task t) {
		Intent intent = new Intent(this, TaskView.class);
		intent.putExtra("task", t);
		intent.putExtra("groupId", groupId);
		startActivity(intent);
	}

	private void deleteSelected() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ConfirmDialogFragment cf = ConfirmDialogFragment.newInstance(
				new ConfirmDialogFragment() {
					@Override
					public void confirm() {
						model.deleteSelectTask(tasks);
					}

					@Override
					public void cancel() {
					}
				}, getString(R.string.deleteQuestion),
				getString(R.string.deleteSelectedTaskQuestion));
		cf.show(ft, "delete seleted");
	}

	private void selectAll(MenuItem item) {
		if (model.selectAllTasks(tasks)) {
			item.setTitle(getString(R.string.deselectAll));
		} else {
			item.setTitle(getString(R.string.selectAll));
		}
		resetTaskList();
	}

	private void completeTask() {
		model.completeSelectedTasks(tasks);
	}

	private void sortByDate() {
		if (isSortedByDate) {
			tasks = model.getAllTaskRelatedToGroup(groupId,
					TaskModel.SORT_BY_DATE, true);
		} else {
			tasks = model.getAllTaskRelatedToGroup(groupId,
					TaskModel.SORT_BY_DATE, false);
		}
		isSortedByDate = !isSortedByDate;
		taskList.setAdapter(new TaskLineAdapter(model, tasks, this, groupId));
	}

	private void sortByPrio() {
		if (isSortedByPrio) {
			tasks = model.getAllTaskRelatedToGroup(groupId,
					TaskModel.SORT_BY_PRIORITY, true);
			Log.i("TODO", "Sort by " + TaskModel.SORT_BY_PRIORITY
					+ " in tasklistView desc= " + true);
		} else {
			tasks = model.getAllTaskRelatedToGroup(groupId,
					TaskModel.SORT_BY_PRIORITY, false);
			Log.i("TODO", "Sort by prio in tasklistView desc= " + false);
		}
		isSortedByPrio = !isSortedByPrio;
		taskList.setAdapter(new TaskLineAdapter(model, tasks, this, groupId));
	}

	private void sortByAdded() {
		tasks = model.getAllTaskRelatedToGroup(groupId,
				TaskModel.SORT_BY_DEFAULT, false);
		Log.i("TODO", "Sort by added in tasklistView");
		taskList.setAdapter(new TaskLineAdapter(model, tasks, this, groupId));
	}
}
