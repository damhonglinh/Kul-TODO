package view.group;

import model.group.Group;
import view.task.TaskListView;
import vn.edu.rmit.assignment1.R;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GroupAdapter extends BaseAdapter {

	private final Group[] groups;
	private final GroupActivity context;

	public GroupAdapter(Group[] groups, GroupActivity context) {
		this.context = context;
		this.groups = groups;
	}

	@Override
	public int getCount() {
		return groups.length;
	}

	@Override
	public Group getItem(int position) {
		return groups[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowGrid = inflater.inflate(R.layout.group_sticker, parent, false);

		TextView tv = (TextView) rowGrid.findViewById(R.id.groupName);
		tv.setText(groups[position].getName());
		tv.setLongClickable(false);

		rowGrid.setLongClickable(true);
		rowGrid.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, TaskListView.class);
				intent.putExtra("groupid", groups[position].getId());
				intent.putExtra("groupName", groups[position].getName());
				context.startActivity(intent);
			}
		});
		return rowGrid;
	}
}
