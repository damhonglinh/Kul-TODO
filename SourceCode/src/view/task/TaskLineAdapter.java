package view.task;

import model.task.Task;
import model.task.TaskModel;
import view.ConfirmDialogFragment;
import vn.edu.rmit.assignment1.R;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TaskLineAdapter extends BaseAdapter {
	private Task[] tasks;
	private Context context;
	private final String groupId;
	private final TaskModel tModel;

	public TaskLineAdapter(TaskModel tModel, Task[] tasks, Context context,
			String groupId) {
		super();
		this.tModel = tModel;
		this.tasks = tasks;
		this.groupId = groupId;
		this.context = context;
	}

	@Override
	public int getCount() {
		return tasks.length;
	}

	@Override
	public Object getItem(int position) {
		return tasks[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final Task t = tasks[position];
		final View taskLine = inflater.inflate(R.layout.task_line, parent,
				false);
		TextView title = (TextView) taskLine.findViewById(R.id.titleTask);
		TextView date = (TextView) taskLine.findViewById(R.id.dateTask);
		ImageView prio = (ImageView) taskLine
				.findViewById(R.id.taskLinePriority);

		title.setText(t.getTitle());
		date.setText(t.getTimeString());

		int temp = 0;
		if (position % 2 == 0) {
			temp = R.color.taskLineBGEven;
		} else {
			temp = R.color.taskLineBGOdd;
		}
		final int idDefaultBGResource = temp;
		setBackgroundSelected(t, taskLine, idDefaultBGResource);

		if (t.isCompleted()) {
			title.setPaintFlags(title.getPaintFlags()
					| Paint.STRIKE_THRU_TEXT_FLAG);
		}

		switch (t.getPriority()) {
		case 1:
			prio.setImageResource(R.drawable.highpriority);
			break;
		case 2:
			prio.setImageResource(R.drawable.mediumpriority);
			break;
		case 3:
			prio.setImageResource(R.drawable.lowpriority);
			break;
		}

		taskLine.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, TaskView.class);
				intent.putExtra("task", t);
				context.startActivity(intent);
			}
		});

		taskLine.setOnTouchListener(new OnTouchListener() {
			private float downX;
			private float MIN_DISTANCE = 120;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					downX = event.getX();
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:

					float upX = event.getX();
					float distance = upX - downX;
					if (Math.abs(distance) > MIN_DISTANCE) {
						if (distance > 0) {// swipe Left-Right
							deleteTask(t);
							return true;
						} else {// swipe Right-Left
							completeTask(t);
							return true;
						}
					}
				}
				return false;
			}
		});

		taskLine.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				t.setSelected(!t.isSelected());
				setBackgroundSelected(t, taskLine, idDefaultBGResource);
				return true;
			}
		});

		prio.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (t.getPriority()) {
				case 1:
					tModel.setPriority(t, 2);
					break;
				case 2:
					tModel.setPriority(t, 3);
					break;
				case 3:
					tModel.setPriority(t, 1);
					break;
				}
			}
		});
		return taskLine;
	}

	private void deleteTask(final Task t) {
		// Log.i("TODO", "Delete task " + t.getTitle());
		Activity act = (Activity) context;
		FragmentTransaction ft = act.getFragmentManager().beginTransaction();
		ConfirmDialogFragment cf = ConfirmDialogFragment.newInstance(
				new ConfirmDialogFragment() {
					@Override
					public void confirm() {
						tModel.deleteTask(t);
						notifyDataSetInvalidated();
						notifyDataSetChanged();
					}

					@Override
					public void cancel() {
					}
				}, context.getString(R.string.deleteQuestion),
				context.getString(R.string.deleteTaskQuestion));
		cf.show(ft, "deleteConfirm");
	}

	private void completeTask(final Task t) {
		// Log.i("TODO", "Complete task " + t.getTitle());
		tModel.completeTask(t, !t.isCompleted());
		notifyDataSetInvalidated();
		notifyDataSetChanged();
	}

	private void setBackgroundSelected(Task t, View taskLine,
			int idDefaultBGColor) {
		if (t.isSelected()) {
			taskLine.setBackgroundResource(R.color.taskLineBGSelected);
		} else {
			taskLine.setBackgroundResource(idDefaultBGColor);
		}
	}
}
